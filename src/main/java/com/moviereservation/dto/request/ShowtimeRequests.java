package com.moviereservation.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShowtimeRequests {

    public record CreateShowtimeRequest(
        @NotNull Long movieId,
        @NotNull Long hallId,
        @NotNull @Future LocalDateTime startTime,
        @NotNull @DecimalMin("0.01") BigDecimal price
    ) {}

    public record UpdateShowtimeRequest(
        @Future LocalDateTime startTime,
        @DecimalMin("0.01") BigDecimal price
    ) {}
}
