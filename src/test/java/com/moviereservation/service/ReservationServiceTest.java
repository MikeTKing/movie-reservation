package com.moviereservation.service;

import com.moviereservation.dto.request.ReservationRequests.CreateReservationRequest;
import com.moviereservation.entity.*;
import com.moviereservation.exception.Exceptions.BadRequestException;
import com.moviereservation.exception.Exceptions.ConflictException;
import com.moviereservation.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservationServiceTest {

    @Autowired ReservationService  reservationService;
    @Autowired UserRepository      userRepository;
    @Autowired MovieRepository     movieRepository;
    @Autowired HallRepository      hallRepository;
    @Autowired SeatRepository      seatRepository;
    @Autowired ShowtimeRepository  showtimeRepository;

    User user1, user2;
    Showtime showtime;
    List<Seat> hallSeats;

    @BeforeEach
    void setUp() {
        // Create users
        user1 = userRepository.save(User.builder()
                .name("User One").email("u1@test.com")
                .password("hashed").role(Role.USER).build());
        user2 = userRepository.save(User.builder()
                .name("User Two").email("u2@test.com")
                .password("hashed").role(Role.USER).build());

        // Create hall with 2 rows x 3 cols = 6 seats
        Hall hall = hallRepository.save(Hall.builder()
                .name("Test Hall").totalRows(2).totalCols(3).build());

        // Create seats
        for (int r = 1; r <= 2; r++) {
            for (int c = 1; c <= 3; c++) {
                seatRepository.save(Seat.builder()
                        .hall(hall)
                        .rowNum(r).colNum(c)
                        .label(((char)('A' + r - 1)) + String.valueOf(c))
                        .build());
            }
        }
        hallSeats = seatRepository.findByHallIdOrderByRowNumAscColNumAsc(hall.getId());

        // Create movie & showtime
        Movie movie = movieRepository.save(Movie.builder()
                .title("Test Movie").durationMin(120).build());

        showtime = showtimeRepository.save(Showtime.builder()
                .movie(movie).hall(hall)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .price(BigDecimal.valueOf(10.00))
                .build());
    }

    @Test
    void createReservation_success() {
        List<Long> seatIds = List.of(hallSeats.get(0).getId(), hallSeats.get(1).getId());
        var request = new CreateReservationRequest(showtime.getId(), seatIds);

        var response = reservationService.create(user1.getId(), request);

        assertThat(response).isNotNull();
        assertThat(response.seats()).hasSize(2);
        assertThat(response.status()).isEqualTo("CONFIRMED");
        assertThat(response.totalPrice()).isEqualByComparingTo(BigDecimal.valueOf(20.00));
    }

    @Test
    void createReservation_conflictingSeats_throwsConflictException() {
        List<Long> seatIds = List.of(hallSeats.get(0).getId(), hallSeats.get(1).getId());

        // User 1 reserves first
        reservationService.create(user1.getId(),
                new CreateReservationRequest(showtime.getId(), seatIds));

        // User 2 tries to reserve the same seats
        assertThatThrownBy(() ->
                reservationService.create(user2.getId(),
                        new CreateReservationRequest(showtime.getId(), seatIds)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createReservation_partialOverlap_throwsConflictException() {
        // User1 takes seat 0
        reservationService.create(user1.getId(),
                new CreateReservationRequest(showtime.getId(),
                        List.of(hallSeats.get(0).getId())));

        // User2 requests seat 0 AND seat 1 — should fail due to seat 0 conflict
        assertThatThrownBy(() ->
                reservationService.create(user2.getId(),
                        new CreateReservationRequest(showtime.getId(),
                                List.of(hallSeats.get(0).getId(), hallSeats.get(1).getId()))))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createReservation_nonOverlappingSeats_bothSucceed() {
        reservationService.create(user1.getId(),
                new CreateReservationRequest(showtime.getId(),
                        List.of(hallSeats.get(0).getId())));

        // Different seat — must succeed
        var response = reservationService.create(user2.getId(),
                new CreateReservationRequest(showtime.getId(),
                        List.of(hallSeats.get(1).getId())));

        assertThat(response.status()).isEqualTo("CONFIRMED");
    }

    @Test
    void cancelReservation_upcoming_succeeds() {
        var created = reservationService.create(user1.getId(),
                new CreateReservationRequest(showtime.getId(),
                        List.of(hallSeats.get(0).getId())));

        var cancelled = reservationService.cancel(created.id(), user1.getId());
        assertThat(cancelled.status()).isEqualTo("CANCELLED");
    }

    @Test
    void cancelReservation_alreadyCancelled_throwsBadRequest() {
        var created = reservationService.create(user1.getId(),
                new CreateReservationRequest(showtime.getId(),
                        List.of(hallSeats.get(0).getId())));

        reservationService.cancel(created.id(), user1.getId());

        assertThatThrownBy(() -> reservationService.cancel(created.id(), user1.getId()))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void createReservation_duplicateSeatIds_throwsBadRequest() {
        Long seatId = hallSeats.get(0).getId();
        assertThatThrownBy(() ->
                reservationService.create(user1.getId(),
                        new CreateReservationRequest(showtime.getId(),
                                List.of(seatId, seatId))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Duplicate");
    }
}
