package com.moviereservation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ReservationRequests {

    public record CreateReservationRequest(
        @NotNull Long showtimeId,
        @NotEmpty @Size(min = 1, max = 10) List<Long> seatIds
    ) {}
}
