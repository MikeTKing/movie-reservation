package com.moviereservation.service;

import com.moviereservation.dto.request.MovieRequests.CreateMovieRequest;
import com.moviereservation.dto.request.MovieRequests.UpdateMovieRequest;
import com.moviereservation.dto.response.Responses.MovieDetailResponse;
import com.moviereservation.dto.response.Responses.MovieSummaryResponse;
import com.moviereservation.entity.Genre;
import com.moviereservation.entity.Movie;
import com.moviereservation.exception.Exceptions.BadRequestException;
import com.moviereservation.exception.Exceptions.ResourceNotFoundException;
import com.moviereservation.repository.GenreRepository;
import com.moviereservation.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<MovieSummaryResponse> findAll(String title, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title"));
        return movieRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(MovieSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public MovieDetailResponse findById(Long id) {
        Movie movie = loadMovie(id);
        return MovieDetailResponse.from(movie);
    }

    @Transactional(readOnly = true)
    public List<MovieSummaryResponse> findByGenre(String genre) {
        return movieRepository.findByGenreName(genre).stream()
                .map(MovieSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovieSummaryResponse> findMoviesWithShowtimesOnDate(LocalDate date) {
        if (date == null) date = LocalDate.now();
        return movieRepository.findMoviesWithShowtimesOnDate(date).stream()
                .map(MovieSummaryResponse::from)
                .toList();
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    @Transactional
    public MovieDetailResponse create(CreateMovieRequest request) {
        Set<Genre> genres = resolveGenres(request.genreIds());
        Movie movie = Movie.builder()
                .title(request.title())
                .description(request.description())
                .posterUrl(request.posterUrl())
                .durationMin(request.durationMin())
                .rating(request.rating())
                .genres(genres)
                .build();
        return MovieDetailResponse.from(movieRepository.save(movie));
    }

    @Transactional
    public MovieDetailResponse update(Long id, UpdateMovieRequest request) {
        Movie movie = loadMovie(id);

        if (StringUtils.hasText(request.title()))       movie.setTitle(request.title());
        if (StringUtils.hasText(request.description())) movie.setDescription(request.description());
        if (StringUtils.hasText(request.posterUrl()))   movie.setPosterUrl(request.posterUrl());
        if (request.durationMin() != null)              movie.setDurationMin(request.durationMin());
        if (StringUtils.hasText(request.rating()))      movie.setRating(request.rating());
        if (request.genreIds() != null)                 movie.setGenres(resolveGenres(request.genreIds()));

        return MovieDetailResponse.from(movieRepository.save(movie));
    }

    @Transactional
    public void delete(Long id) {
        if (!movieRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Movie", id);
        }
        movieRepository.deleteById(id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Movie loadMovie(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Movie", id));
    }

    private Set<Genre> resolveGenres(Set<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) return new HashSet<>();
        List<Genre> found = genreRepository.findAllById(genreIds);
        if (found.size() != genreIds.size()) {
            Set<Long> foundIds = found.stream().map(Genre::getId).collect(Collectors.toSet());
            Set<Long> missing = genreIds.stream()
                    .filter(gid -> !foundIds.contains(gid))
                    .collect(Collectors.toSet());
            throw new BadRequestException("Genre(s) not found: " + missing);
        }
        return new HashSet<>(found);
    }
}
