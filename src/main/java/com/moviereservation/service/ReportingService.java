package com.moviereservation.service;

import com.moviereservation.dto.response.AdminDashboardResponse;
import com.moviereservation.dto.response.MovieRevenueReport;
import com.moviereservation.dto.response.ShowtimeCapacityReport;
import com.moviereservation.entity.ReservationStatus;
import com.moviereservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportingService {

    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        long total       = reservationRepository.count();
        long confirmed   = reservationRepository.countByStatus(ReservationStatus.CONFIRMED);
        long cancelled   = reservationRepository.countByStatus(ReservationStatus.CANCELLED);
        BigDecimal revenue = reservationRepository.sumRevenue(ReservationStatus.CONFIRMED);

        List<MovieRevenueReport>     revenueByMovie  = reservationRepository.getRevenueByMovie();
        List<ShowtimeCapacityReport> capacityReports = reservationRepository.getShowtimeCapacityReport();

        return new AdminDashboardResponse(
                total, confirmed, cancelled, revenue, revenueByMovie, capacityReports);
    }

    @Transactional(readOnly = true)
    public List<MovieRevenueReport> getRevenueByMovie() {
        return reservationRepository.getRevenueByMovie();
    }

    @Transactional(readOnly = true)
    public List<ShowtimeCapacityReport> getShowtimeCapacity() {
        return reservationRepository.getShowtimeCapacityReport();
    }
}
