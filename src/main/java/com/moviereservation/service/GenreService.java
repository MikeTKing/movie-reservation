package com.moviereservation.service;

import com.moviereservation.dto.response.Responses.GenreResponse;
import com.moviereservation.entity.Genre;
import com.moviereservation.exception.Exceptions.ConflictException;
import com.moviereservation.exception.Exceptions.ResourceNotFoundException;
import com.moviereservation.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    @Transactional(readOnly = true)
    public List<GenreResponse> findAll() {
        return genreRepository.findAll().stream()
                .map(GenreResponse::from)
                .toList();
    }

    @Transactional
    public GenreResponse create(String name) {
        if (genreRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Genre already exists: " + name);
        }
        Genre genre = genreRepository.save(Genre.builder().name(name).build());
        return GenreResponse.from(genre);
    }

    @Transactional
    public void delete(Long id) {
        if (!genreRepository.existsById(id)) {
            throw ResourceNotFoundException.of("Genre", id);
        }
        genreRepository.deleteById(id);
    }
}
