package com.moviereservation.repository;

import com.moviereservation.entity.ReservationSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {

    List<ReservationSeat> findByReservationId(Long reservationId);

    /**
     * Returns seat IDs already booked (CONFIRMED) for a showtime.
     */
    @Query("""
        SELECT rs.seat.id FROM ReservationSeat rs
        WHERE rs.showtime.id = :showtimeId
          AND rs.reservation.status = com.moviereservation.entity.ReservationStatus.CONFIRMED
        """)
    Set<Long> findBookedSeatIdsByShowtime(@Param("showtimeId") Long showtimeId);

    /**
     * Check if any of the given seat IDs are already booked for the showtime.
     */
    @Query("""
        SELECT COUNT(rs) > 0 FROM ReservationSeat rs
        WHERE rs.showtime.id = :showtimeId
          AND rs.seat.id IN :seatIds
          AND rs.reservation.status = com.moviereservation.entity.ReservationStatus.CONFIRMED
        """)
    boolean existsConflict(@Param("showtimeId") Long showtimeId,
                            @Param("seatIds") List<Long> seatIds);
}
