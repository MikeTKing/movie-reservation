package com.moviereservation.service;

import com.moviereservation.dto.request.ShowtimeRequests.CreateShowtimeRequest;
import com.moviereservation.dto.request.ShowtimeRequests.UpdateShowtimeRequest;
import com.moviereservation.dto.response.Responses.*;
import com.moviereservation.entity.*;
import com.moviereservation.exception.Exceptions.*;
import com.moviereservation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository    movieRepository;
    private final HallRepository     hallRepository;
    private final SeatRepository     seatRepository;

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ShowtimeResponse> findByMovieAndDate(Long movieId, LocalDate date) {
        if (date == null) date = LocalDate.now();
        return showtimeRepository.findByMovieIdAndDate(movieId, date).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShowtimeResponse> findAllByDate(LocalDate date) {
        if (date == null) date = LocalDate.now();
        return showtimeRepository.findAllByDate(date).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShowtimeResponse findById(Long id) {
        return toResponse(loadShowtime(id));
    }

    @Transactional(readOnly = true)
    public SeatMapResponse getSeatMap(Long showtimeId) {
        Showtime showtime = loadShowtime(showtimeId);
        Long hallId = showtime.getHall().getId();

        // All seats in the hall
        List<Seat> allSeats = seatRepository.findByHallIdOrderByRowNumAscColNumAsc(hallId);

        // Available seat IDs
        List<Seat> available = seatRepository.findAvailableSeatsForShowtime(hallId, showtimeId);
        var availableIds = available.stream().map(Seat::getId).collect(java.util.stream.Collectors.toSet());

        List<SeatResponse> seatResponses = allSeats.stream()
                .map(s -> SeatResponse.from(s, availableIds.contains(s.getId())))
                .toList();

        return new SeatMapResponse(toResponse(showtime), seatResponses);
    }

    // ── Admin mutations ───────────────────────────────────────────────────────

    @Transactional
    public ShowtimeResponse create(CreateShowtimeRequest request) {
        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> ResourceNotFoundException.of("Movie", request.movieId()));
        Hall hall = hallRepository.findById(request.hallId())
                .orElseThrow(() -> ResourceNotFoundException.of("Hall", request.hallId()));

        LocalDateTime startTime = request.startTime();
        LocalDateTime endTime = startTime.plusMinutes(movie.getDurationMin());

        checkNoOverlap(hall.getId(), startTime, endTime, -1L);

        Showtime showtime = Showtime.builder()
                .movie(movie)
                .hall(hall)
                .startTime(startTime)
                .endTime(endTime)
                .price(request.price())
                .build();
        return toResponse(showtimeRepository.save(showtime));
    }

    @Transactional
    public ShowtimeResponse update(Long id, UpdateShowtimeRequest request) {
        Showtime showtime = loadShowtime(id);

        if (request.startTime() != null) {
            LocalDateTime newEnd = request.startTime()
                    .plusMinutes(showtime.getMovie().getDurationMin());
            checkNoOverlap(showtime.getHall().getId(), request.startTime(), newEnd, id);
            showtime.setStartTime(request.startTime());
            showtime.setEndTime(newEnd);
        }
        if (request.price() != null) showtime.setPrice(request.price());

        return toResponse(showtimeRepository.save(showtime));
    }

    @Transactional
    public void delete(Long id) {
        if (!showtimeRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Showtime", id);
        }
        showtimeRepository.deleteById(id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public Showtime loadShowtime(Long id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Showtime", id));
    }

    private void checkNoOverlap(Long hallId, LocalDateTime start,
                                  LocalDateTime end, Long excludeId) {
        List<Showtime> conflicts =
                showtimeRepository.findOverlappingShowtimes(hallId, start, end, excludeId);
        if (!conflicts.isEmpty()) {
            throw new ConflictException(
                    "Hall is already booked during " + start + " – " + end);
        }
    }

    ShowtimeResponse toResponse(Showtime s) {
        long booked = showtimeRepository.countBookedSeats(s.getId());
        int capacity = s.getHall().getTotalCapacity();
        return new ShowtimeResponse(
                s.getId(),
                s.getMovie().getId(),
                s.getMovie().getTitle(),
                HallResponse.from(s.getHall()),
                s.getStartTime(),
                s.getEndTime(),
                s.getPrice(),
                capacity,
                booked,
                capacity - booked);
    }
}
