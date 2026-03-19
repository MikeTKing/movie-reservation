package com.moviereservation.controller;

import com.moviereservation.dto.response.*;
import com.moviereservation.dto.response.Responses.ReservationResponse;
import com.moviereservation.dto.response.Responses.UserResponse;
import com.moviereservation.repository.UserRepository;
import com.moviereservation.service.ReportingService;
import com.moviereservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin", description = "Admin-only: reporting, reservation oversight, user management")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final ReservationService reservationService;
    private final ReportingService   reportingService;
    private final UserRepository     userRepository;

    // ── Reservations ──────────────────────────────────────────────────────────

    @Operation(summary = "List all reservations (paginated)")
    @GetMapping("/reservations")
    public ResponseEntity<Page<ReservationResponse>> allReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(reservationService.findAll(page, size));
    }

    // ── Reporting ─────────────────────────────────────────────────────────────

    @Operation(summary = "Admin dashboard: totals, revenue, capacity")
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> dashboard() {
        return ResponseEntity.ok(reportingService.getDashboard());
    }

    @Operation(summary = "Revenue breakdown by movie")
    @GetMapping("/reports/revenue")
    public ResponseEntity<List<MovieRevenueReport>> revenueReport() {
        return ResponseEntity.ok(reportingService.getRevenueByMovie());
    }

    @Operation(summary = "Capacity & occupancy report per showtime")
    @GetMapping("/reports/capacity")
    public ResponseEntity<List<ShowtimeCapacityReport>> capacityReport() {
        return ResponseEntity.ok(reportingService.getShowtimeCapacity());
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    @Operation(summary = "List all users")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> allUsers() {
        return ResponseEntity.ok(
                userRepository.findAll().stream()
                        .map(UserResponse::from)
                        .toList());
    }
}
