package com.moviereservation.controller;

import com.moviereservation.dto.request.ShowtimeRequests.*;
import com.moviereservation.dto.response.Responses.*;
import com.moviereservation.service.ShowtimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Showtimes", description = "Showtime scheduling and seat maps")
@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    // ── Public ────────────────────────────────────────────────────────────────

    @Operation(summary = "List all showtimes for a given date (defaults to today)")
    @GetMapping
    public ResponseEntity<List<ShowtimeResponse>> findByDate(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(showtimeService.findAllByDate(date));
    }

    @Operation(summary = "Get showtime details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ShowtimeResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(showtimeService.findById(id));
    }

    @Operation(summary = "List showtimes for a movie on a given date")
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ShowtimeResponse>> findByMovieAndDate(
            @PathVariable Long movieId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(showtimeService.findByMovieAndDate(movieId, date));
    }

    @Operation(summary = "Get seat availability map for a showtime")
    @GetMapping("/{id}/seat-map")
    public ResponseEntity<SeatMapResponse> seatMap(@PathVariable Long id) {
        return ResponseEntity.ok(showtimeService.getSeatMap(id));
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    @Operation(summary = "Schedule a new showtime (Admin only)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShowtimeResponse> create(
            @Valid @RequestBody CreateShowtimeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(showtimeService.create(request));
    }

    @Operation(summary = "Update a showtime's start time or price (Admin only)")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShowtimeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateShowtimeRequest request) {
        return ResponseEntity.ok(showtimeService.update(id, request));
    }

    @Operation(summary = "Delete a showtime (Admin only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        showtimeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
