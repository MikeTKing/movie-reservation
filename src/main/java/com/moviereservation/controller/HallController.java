package com.moviereservation.controller;

import com.moviereservation.dto.response.Responses.HallResponse;
import com.moviereservation.entity.Hall;
import com.moviereservation.exception.Exceptions.ResourceNotFoundException;
import com.moviereservation.repository.HallRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Halls", description = "Cinema hall information")
@RestController
@RequestMapping("/api/halls")
@RequiredArgsConstructor
public class HallController {

    private final HallRepository hallRepository;

    @Operation(summary = "List all halls")
    @GetMapping
    public ResponseEntity<List<HallResponse>> findAll() {
        return ResponseEntity.ok(
                hallRepository.findAll().stream().map(HallResponse::from).toList());
    }

    @Operation(summary = "Get hall by ID")
    @GetMapping("/{id}")
    public ResponseEntity<HallResponse> findById(@PathVariable Long id) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Hall", id));
        return ResponseEntity.ok(HallResponse.from(hall));
    }
}
