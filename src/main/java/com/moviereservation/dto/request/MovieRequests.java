package com.moviereservation.dto.request;

import jakarta.validation.constraints.*;

import java.util.Set;

public class MovieRequests {

    public record CreateMovieRequest(
        @NotBlank @Size(max = 200) String title,
        String description,
        @Size(max = 500) String posterUrl,
        @NotNull @Min(1) @Max(600) Integer durationMin,
        @Size(max = 10) String rating,
        Set<Long> genreIds
    ) {}

    public record UpdateMovieRequest(
        @Size(max = 200) String title,
        String description,
        @Size(max = 500) String posterUrl,
        @Min(1) @Max(600) Integer durationMin,
        @Size(max = 10) String rating,
        Set<Long> genreIds
    ) {}
}
