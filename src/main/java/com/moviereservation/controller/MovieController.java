package com.moviereservation.controller;

import com.moviereservation.dto.request.MovieRequests.*;
import com.moviereservation.dto.response.Responses.*;
import com.moviereservation.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Movies", description = "Browse and manage movies")
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    // ── Public ────────────────────────────────────────────────────────────────

    @Operation(summary = "List movies with optional title filter (paginated)")
    @GetMapping
    public ResponseEntity<Page<MovieSummaryResponse>> findAll(
            @RequestParam(required = false, defaultValue = "") String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(movieService.findAll(title, page, size));
    }

    @Operation(summary = "Get movie details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<MovieDetailResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(movieService.findById(id));
    }

    @Operation(summary = "List movies by genre")
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<MovieSummaryResponse>> findByGenre(@PathVariable String genre) {
        return ResponseEntity.ok(movieService.findByGenre(genre));
    }

    @Operation(summary = "List movies that have showtimes on a specific date")
    @GetMapping("/playing")
    public ResponseEntity<List<MovieSummaryResponse>> playing(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(movieService.findMoviesWithShowtimesOnDate(date));
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    @Operation(summary = "Add a new movie (Admin only)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDetailResponse> create(
            @Valid @RequestBody CreateMovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.create(request));
    }

    @Operation(summary = "Update a movie (Admin only)")
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MovieDetailResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMovieRequest request) {
        return ResponseEntity.ok(movieService.update(id, request));
    }

    @Operation(summary = "Delete a movie (Admin only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
