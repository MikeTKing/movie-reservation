package com.moviereservation.repository;

import com.moviereservation.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    @Query("""
        SELECT DISTINCT m FROM Movie m
        LEFT JOIN FETCH m.genres
        WHERE (:title IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%')))
        ORDER BY m.title
        """)
    Page<Movie> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);

    @Query("""
        SELECT DISTINCT m FROM Movie m
        JOIN FETCH m.genres g
        WHERE g.name = :genre
        ORDER BY m.title
        """)
    List<Movie> findByGenreName(@Param("genre") String genre);

    /**
     * Movies that have at least one showtime on the given date.
     */
    @Query("""
        SELECT DISTINCT m FROM Movie m
        JOIN m.showtimes s
        WHERE CAST(s.startTime AS date) = :date
        ORDER BY m.title
        """)
    List<Movie> findMoviesWithShowtimesOnDate(@Param("date") LocalDate date);
}
