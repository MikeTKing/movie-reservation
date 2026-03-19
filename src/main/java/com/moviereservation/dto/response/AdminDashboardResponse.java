package com.moviereservation.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record AdminDashboardResponse(
    long totalReservations,
    long confirmedReservations,
    long cancelledReservations,
    BigDecimal totalRevenue,
    List<MovieRevenueReport> revenueByMovie,
    List<ShowtimeCapacityReport> showtimeCapacity
) {}
