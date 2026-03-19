package com.moviereservation.service;

import com.moviereservation.dto.request.ShowtimeRequests.CreateShowtimeRequest;
import com.moviereservation.entity.*;
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

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ShowtimeServiceTest {

    @Autowired ShowtimeService  showtimeService;
    @Autowired MovieRepository  movieRepository;
    @Autowired HallRepository   hallRepository;

    Movie movie;
    Hall  hall;

    @BeforeEach
    void setUp() {
        movie = movieRepository.save(Movie.builder()
                .title("Overlap Movie").durationMin(90).build());
        hall = hallRepository.save(Hall.builder()
                .name("Overlap Hall").totalRows(5).totalCols(10).build());
    }

    @Test
    void createShowtime_noOverlap_succeeds() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        var response = showtimeService.create(
                new CreateShowtimeRequest(movie.getId(), hall.getId(), start, BigDecimal.TEN));
        assertThat(response.id()).isPositive();
    }

    @Test
    void createShowtime_overlapping_throwsConflict() {
        LocalDateTime start = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0);
        showtimeService.create(
                new CreateShowtimeRequest(movie.getId(), hall.getId(), start, BigDecimal.TEN));

        // Second showtime starts 30 min later — overlaps with first (90-min movie)
        LocalDateTime overlappingStart = start.plusMinutes(30);
        assertThatThrownBy(() ->
                showtimeService.create(
                        new CreateShowtimeRequest(movie.getId(), hall.getId(),
                                overlappingStart, BigDecimal.TEN)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createShowtime_backToBack_succeeds() {
        LocalDateTime start1 = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0);
        showtimeService.create(
                new CreateShowtimeRequest(movie.getId(), hall.getId(), start1, BigDecimal.TEN));

        // Second starts exactly when first ends (10:00 + 90min = 11:30)
        LocalDateTime start2 = start1.plusMinutes(90);
        var response = showtimeService.create(
                new CreateShowtimeRequest(movie.getId(), hall.getId(), start2, BigDecimal.TEN));
        assertThat(response.id()).isPositive();
    }
}
