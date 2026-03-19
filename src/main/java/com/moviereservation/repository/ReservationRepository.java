package com.moviereservation.repository;

import com.moviereservation.entity.Reservation;
import com.moviereservation.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /* ---- user-scoped queries ---- */

    @Query("""
        SELECT r FROM Reservation r
        JOIN FETCH r.showtime s
        JOIN FETCH s.movie
        JOIN FETCH s.hall
        WHERE r.user.id = :userId
        ORDER BY r.createdAt DESC
        """)
    Page<Reservation> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        SELECT r FROM Reservation r
        JOIN FETCH r.showtime s
        JOIN FETCH s.movie
        WHERE r.id = :id AND r.user.id = :userId
        """)
    Optional<Reservation> findByIdAndUserId(@Param("id") Long id,
                                             @Param("userId") Long userId);

    /* ---- admin queries ---- */

    @Query("""
        SELECT r FROM Reservation r
        JOIN FETCH r.user
        JOIN FETCH r.showtime s
        JOIN FETCH s.movie
        JOIN FETCH s.hall
        ORDER BY r.createdAt DESC
        """)
    Page<Reservation> findAllWithDetails(Pageable pageable);

    /* ---- reporting ---- */

    @Query("""
        SELECT COALESCE(SUM(r.totalPrice), 0)
        FROM Reservation r
        WHERE r.status = :status
        """)
    BigDecimal sumRevenue(@Param("status") ReservationStatus status);

    @Query("""
        SELECT COALESCE(SUM(r.totalPrice), 0)
        FROM Reservation r
        WHERE r.status = :status
          AND r.createdAt BETWEEN :from AND :to
        """)
    BigDecimal sumRevenueBetween(@Param("status") ReservationStatus status,
                                  @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);

    @Query("""
        SELECT COUNT(r) FROM Reservation r WHERE r.status = :status
        """)
    long countByStatus(@Param("status") ReservationStatus status);

    /**
     * Per-movie revenue report.
     */
    @Query("""
        SELECT new com.moviereservation.dto.response.MovieRevenueReport(
            m.id, m.title,
            COUNT(DISTINCT r.id),
            COALESCE(SUM(r.totalPrice), 0)
        )
        FROM Reservation r
        JOIN r.showtime s
        JOIN s.movie m
        WHERE r.status = com.moviereservation.entity.ReservationStatus.CONFIRMED
        GROUP BY m.id, m.title
        ORDER BY SUM(r.totalPrice) DESC
        """)
    List<com.moviereservation.dto.response.MovieRevenueReport> getRevenueByMovie();

    /**
     * Per-showtime capacity report.
     */
    @Query("""
        SELECT new com.moviereservation.dto.response.ShowtimeCapacityReport(
            s.id, m.title, s.startTime, h.name, h.totalRows * h.totalCols,
            COUNT(rs.id)
        )
        FROM Showtime s
        JOIN s.movie m
        JOIN s.hall h
        LEFT JOIN ReservationSeat rs ON rs.showtime = s
            AND rs.reservation.status = com.moviereservation.entity.ReservationStatus.CONFIRMED
        GROUP BY s.id, m.title, s.startTime, h.name, h.totalRows, h.totalCols
        ORDER BY s.startTime DESC
        """)
    List<com.moviereservation.dto.response.ShowtimeCapacityReport> getShowtimeCapacityReport();
}
