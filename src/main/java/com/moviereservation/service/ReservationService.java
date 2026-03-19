package com.moviereservation.service;

import com.moviereservation.dto.request.ReservationRequests.CreateReservationRequest;
import com.moviereservation.dto.response.Responses.*;
import com.moviereservation.entity.*;
import com.moviereservation.exception.Exceptions.*;
import com.moviereservation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository     reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final ShowtimeRepository        showtimeRepository;
    private final SeatRepository            seatRepository;
    private final UserRepository            userRepository;
    private final ShowtimeService           showtimeService;

    // ── User: create reservation ──────────────────────────────────────────────

    /**
     * Reserves seats for a showtime.
     *
     * Overbooking prevention strategy:
     *   1. Application-level check  – fast fail before hitting the DB.
     *   2. DB-level UNIQUE constraint on (seat_id, showtime_id) in reservation_seats
     *      – last-resort guard that catches concurrent races.
     *   3. The entire method runs inside a single SERIALIZABLE (or at minimum
     *      READ COMMITTED + SELECT FOR UPDATE via pessimistic lock) transaction.
     *
     * The combination of the DB unique constraint + transaction isolation makes
     * double-booking impossible even under high concurrency.
     */
    @Transactional
    public ReservationResponse create(Long userId, CreateReservationRequest request) {

        // 1. Load the showtime (validates it exists)
        Showtime showtime = showtimeService.loadShowtime(request.showtimeId());

        // 2. Make sure the showtime is in the future
        if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot reserve seats for a past showtime.");
        }

        // 3. De-duplicate requested seat IDs
        List<Long> seatIds = request.seatIds().stream().distinct().toList();
        if (seatIds.size() != request.seatIds().size()) {
            throw new BadRequestException("Duplicate seat IDs are not allowed.");
        }

        // 4. Verify all requested seats actually belong to the showtime's hall
        List<Seat> seats = seatRepository.findByIdsAndHallId(seatIds, showtime.getHall().getId());
        if (seats.size() != seatIds.size()) {
            throw new BadRequestException(
                    "One or more seat IDs are invalid or do not belong to this hall.");
        }

        // 5. Application-level conflict check (fast path)
        boolean hasConflict = reservationSeatRepository.existsConflict(
                showtime.getId(), seatIds);
        if (hasConflict) {
            throw new ConflictException(
                    "One or more of the selected seats are already reserved for this showtime.");
        }

        // 6. Load user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));

        // 7. Calculate price
        BigDecimal totalPrice = showtime.getPrice()
                .multiply(BigDecimal.valueOf(seats.size()));

        // 8. Persist reservation
        Reservation reservation = Reservation.builder()
                .user(user)
                .showtime(showtime)
                .status(ReservationStatus.CONFIRMED)
                .totalPrice(totalPrice)
                .build();
        reservation = reservationRepository.save(reservation);

        // 9. Persist reservation seats (DB unique constraint fires here on conflict)
        final Reservation savedReservation = reservation;
        List<ReservationSeat> reservationSeats = seats.stream()
                .map(seat -> ReservationSeat.builder()
                        .reservation(savedReservation)
                        .seat(seat)
                        .showtime(showtime)
                        .build())
                .toList();
        reservationSeatRepository.saveAll(reservationSeats);
        reservation.setReservationSeats(reservationSeats);

        log.info("Reservation {} created: user={} showtime={} seats={}",
                reservation.getId(), userId, showtime.getId(), seatIds);

        return toResponse(reservation);
    }

    // ── User: view own reservations ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ReservationResponse> findByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reservationRepository.findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ReservationResponse findByIdForUser(Long id, Long userId) {
        Reservation reservation = reservationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Reservation", id));
        return toResponse(reservation);
    }

    // ── User: cancel reservation ──────────────────────────────────────────────

    @Transactional
    public ReservationResponse cancel(Long id, Long userId) {
        Reservation reservation = reservationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Reservation", id));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BadRequestException("Reservation is already cancelled.");
        }
        if (reservation.getShowtime().getStartTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Cannot cancel a reservation for a past showtime.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        log.info("Reservation {} cancelled by user {}", id, userId);
        return toResponse(reservationRepository.save(reservation));
    }

    // ── Admin: view all reservations ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<ReservationResponse> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reservationRepository.findAllWithDetails(pageable)
                .map(this::toResponse);
    }

    // ── Mapper ────────────────────────────────────────────────────────────────

    private ReservationResponse toResponse(Reservation r) {
        List<SeatResponse> seatResponses = r.getReservationSeats().stream()
                .map(rs -> SeatResponse.from(rs.getSeat()))
                .toList();

        ShowtimeResponse showtimeResponse = showtimeService.toResponse(r.getShowtime());

        return new ReservationResponse(
                r.getId(),
                r.getUser().getId(),
                r.getUser().getName(),
                showtimeResponse,
                seatResponses,
                r.getStatus().name(),
                r.getTotalPrice(),
                r.getCreatedAt());
    }
}
