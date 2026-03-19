package com.moviereservation.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reservation_seats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"seat_id", "showtime_id"})
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class ReservationSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;
}
