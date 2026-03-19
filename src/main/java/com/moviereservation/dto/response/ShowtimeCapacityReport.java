package com.moviereservation.dto.response;

import java.time.LocalDateTime;

public record ShowtimeCapacityReport(
    Long showtimeId,
    String movieTitle,
    LocalDateTime startTime,
    String hallName,
    Long totalCapacity,
    Long bookedSeats
) {
    public long availableSeats() {
        return totalCapacity - bookedSeats;
    }

    public double occupancyPercent() {
        if (totalCapacity == 0) return 0;
        return (bookedSeats * 100.0) / totalCapacity;
    }
}
