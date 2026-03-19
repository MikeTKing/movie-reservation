package com.moviereservation.repository;

import com.moviereservation.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    List<Showtime> findByMovieIdOrderByStartTimeAsc(Long movieId);

    /**
     * Showtimes for a specific movie on a specific date.
     */
    @Query("""
        SELECT s FROM Showtime s
        JOIN FETCH s.hall
        WHERE s.movie.id = :movieId
          AND CAST(s.startTime AS date) = :date
        ORDER BY s.startTime
        """)
    List<Showtime> findByMovieIdAndDate(@Param("movieId") Long movieId,
                                        @Param("date") LocalDate date);

    /**
     * All showtimes on a given date (for browsing).
     */
    @Query("""
        SELECT s FROM Showtime s
        JOIN FETCH s.movie
        JOIN FETCH s.hall
        WHERE CAST(s.startTime AS date) = :date
        ORDER BY s.startTime
        """)
    List<Showtime> findAllByDate(@Param("date") LocalDate date);

    /**
     * Check for overlapping showtimes in the same hall (used as a safeguard
     * before DB-level EXCLUDE constraint fires).
     */
    @Query("""
        SELECT s FROM Showtime s
        WHERE s.hall.id = :hallId
          AND s.id <> :excludeId
          AND s.startTime < :endTime
          AND s.endTime > :startTime
        """)
    List<Showtime> findOverlappingShowtimes(@Param("hallId") Long hallId,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime,
                                             @Param("excludeId") Long excludeId);

    /**
     * Count booked seats for a showtime (CONFIRMED only).
     */
    @Query("""
        SELECT COUNT(rs) FROM ReservationSeat rs
        WHERE rs.showtime.id = :showtimeId
          AND rs.reservation.status = com.moviereservation.entity.ReservationStatus.CONFIRMED
        """)
    long countBookedSeats(@Param("showtimeId") Long showtimeId);
}
