package com.example.movieservice.service;

import com.example.movieservice.event.MovieEvent;
import com.example.movieservice.kafka.MovieEventProducer;
import com.example.movieservice.model.Movie;
import com.example.movieservice.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovieService {
    
    private final MovieRepository movieRepository;
    private final MovieEventProducer eventProducer;
    
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }
    
    public Optional<Movie> getMovieById(Long id) {
        return movieRepository.findById(id);
    }
    
    public List<Movie> getMoviesByCategory(String category) {
        return movieRepository.findByCategory(category);
    }
    
    public List<Movie> searchMoviesByName(String name) {
        return movieRepository.findByNameContainingIgnoreCase(name);
    }
    
    @Transactional
    public Movie createMovie(Movie movie) {
        Movie savedMovie = movieRepository.save(movie);
        eventProducer.sendMovieEvent(MovieEvent.created(savedMovie));
        return savedMovie;
    }
    
    @Transactional
    public Optional<Movie> updateMovie(Long id, Movie movieDetails) {
        return movieRepository.findById(id)
            .map(movie -> {
                movie.setName(movieDetails.getName());
                movie.setReleaseDate(movieDetails.getReleaseDate());
                movie.setCategory(movieDetails.getCategory());
                Movie updatedMovie = movieRepository.save(movie);
                eventProducer.sendMovieEvent(MovieEvent.updated(updatedMovie));
                return updatedMovie;
            });
    }
    
    @Transactional
    public boolean deleteMovie(Long id) {
        return movieRepository.findById(id)
            .map(movie -> {
                String movieName = movie.getName();
                movieRepository.delete(movie);
                eventProducer.sendMovieEvent(MovieEvent.deleted(id, movieName));
                return true;
            })
            .orElse(false);
    }
}