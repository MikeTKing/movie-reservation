package com.moviereservation.repository;

import com.moviereservation.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByHallIdOrderByRowNumAscColNumAsc(Long hallId);

    /**
     * Returns seats in a hall that are NOT yet booked for the given showtime
     * (only counting CONFIRMED reservations).
     */
    @Query("""
        SELECT s FROM Seat s
        WHERE s.hall.id = :hallId
          AND s.id NOT IN (
              SELECT rs.seat.id FROM ReservationSeat rs
              WHERE rs.showtime.id = :showtimeId
                AND rs.reservation.status = com.moviereservation.entity.ReservationStatus.CONFIRMED
          )
        ORDER BY s.rowNum ASC, s.colNum ASC
        """)
    List<Seat> findAvailableSeatsForShowtime(@Param("hallId") Long hallId,
                                             @Param("showtimeId") Long showtimeId);

    @Query("SELECT s FROM Seat s WHERE s.id IN :ids AND s.hall.id = :hallId")
    List<Seat> findByIdsAndHallId(@Param("ids") List<Long> ids, @Param("hallId") Long hallId);
}
