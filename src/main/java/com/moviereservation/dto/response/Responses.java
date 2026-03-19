package com.moviereservation.dto.response;

import com.moviereservation.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// ── Auth ─────────────────────────────────────────────────────────────────────

public class Responses {

    public record AuthResponse(String token, String type, UserResponse user) {
        public static AuthResponse of(String token, User user) {
            return new AuthResponse(token, "Bearer", UserResponse.from(user));
        }
    }

    // ── User ─────────────────────────────────────────────────────────────────

    public record UserResponse(Long id, String name, String email, String role) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole().name());
        }
    }

    // ── Genre ────────────────────────────────────────────────────────────────

    public record GenreResponse(Long id, String name) {
        public static GenreResponse from(Genre g) {
            return new GenreResponse(g.getId(), g.getName());
        }
    }

    // ── Hall ─────────────────────────────────────────────────────────────────

    public record HallResponse(Long id, String name, int totalRows, int totalCols, int capacity) {
        public static HallResponse from(Hall h) {
            return new HallResponse(h.getId(), h.getName(),
                    h.getTotalRows(), h.getTotalCols(), h.getTotalCapacity());
        }
    }

    // ── Seat ─────────────────────────────────────────────────────────────────

    public record SeatResponse(Long id, int rowNum, int colNum, String label, boolean available) {
        public static SeatResponse from(Seat s, boolean available) {
            return new SeatResponse(s.getId(), s.getRowNum(), s.getColNum(), s.getLabel(), available);
        }
        public static SeatResponse from(Seat s) {
            return from(s, true);
        }
    }

    // ── Movie ────────────────────────────────────────────────────────────────

    public record MovieSummaryResponse(Long id, String title, String posterUrl,
                                        int durationMin, String rating,
                                        Set<String> genres) {
        public static MovieSummaryResponse from(Movie m) {
            return new MovieSummaryResponse(
                    m.getId(), m.getTitle(), m.getPosterUrl(),
                    m.getDurationMin(), m.getRating(),
                    m.getGenres().stream().map(Genre::getName).collect(Collectors.toSet()));
        }
    }

    public record MovieDetailResponse(Long id, String title, String description,
                                       String posterUrl, int durationMin, String rating,
                                       Set<GenreResponse> genres,
                                       LocalDateTime createdAt) {
        public static MovieDetailResponse from(Movie m) {
            return new MovieDetailResponse(
                    m.getId(), m.getTitle(), m.getDescription(),
                    m.getPosterUrl(), m.getDurationMin(), m.getRating(),
                    m.getGenres().stream().map(GenreResponse::from).collect(Collectors.toSet()),
                    m.getCreatedAt());
        }
    }

    // ── Showtime ─────────────────────────────────────────────────────────────

    public record ShowtimeResponse(Long id, Long movieId, String movieTitle,
                                    HallResponse hall, LocalDateTime startTime,
                                    LocalDateTime endTime, BigDecimal price,
                                    int totalCapacity, long bookedSeats, long availableSeats) {}

    // ── Reservation ──────────────────────────────────────────────────────────

    public record ReservationResponse(Long id, Long userId, String userName,
                                       ShowtimeResponse showtime,
                                       List<SeatResponse> seats,
                                       String status, BigDecimal totalPrice,
                                       LocalDateTime createdAt) {}

    // ── Seat map ─────────────────────────────────────────────────────────────

    public record SeatMapResponse(ShowtimeResponse showtime, List<SeatResponse> seats) {}
}
