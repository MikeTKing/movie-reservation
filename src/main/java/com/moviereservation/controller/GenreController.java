package com.moviereservation.controller;

import com.moviereservation.dto.response.Responses.GenreResponse;
import com.moviereservation.service.GenreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Genres", description = "Movie genre management")
@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @Operation(summary = "List all genres (public)")
    @GetMapping
    public ResponseEntity<List<GenreResponse>> findAll() {
        return ResponseEntity.ok(genreService.findAll());
    }

    @Operation(summary = "Create a new genre (Admin only)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenreResponse> create(@RequestParam String name) {
        return ResponseEntity.status(HttpStatus.CREATED).body(genreService.create(name));
    }

    @Operation(summary = "Delete a genre (Admin only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        genreService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
