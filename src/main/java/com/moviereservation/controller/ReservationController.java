package com.moviereservation.controller;

import com.moviereservation.dto.request.ReservationRequests.CreateReservationRequest;
import com.moviereservation.dto.response.Responses.ReservationResponse;
import com.moviereservation.entity.User;
import com.moviereservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reservations", description = "Create and manage seat reservations")
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "Reserve seats for a showtime")
    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.create(currentUser.getId(), request));
    }

    @Operation(summary = "List my reservations (paginated)")
    @GetMapping("/my")
    public ResponseEntity<Page<ReservationResponse>> myReservations(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                reservationService.findByUser(currentUser.getId(), page, size));
    }

    @Operation(summary = "Get a specific reservation (must belong to me)")
    @GetMapping("/my/{id}")
    public ResponseEntity<ReservationResponse> findMyReservation(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                reservationService.findByIdForUser(id, currentUser.getId()));
    }

    @Operation(summary = "Cancel an upcoming reservation")
    @PatchMapping("/my/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancel(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                reservationService.cancel(id, currentUser.getId()));
    }
}
