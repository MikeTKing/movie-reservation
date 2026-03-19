package com.moviereservation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovieRevenueReport(
    Long movieId,
    String movieTitle,
    Long totalReservations,
    BigDecimal totalRevenue
) {}
