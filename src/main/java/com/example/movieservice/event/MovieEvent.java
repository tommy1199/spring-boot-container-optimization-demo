package com.example.movieservice.event;

import com.example.movieservice.model.Movie;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieEvent {

    public enum EventType {
        CREATED, UPDATED, DELETED
    }
    
    private EventType eventType;
    private Long movieId;
    private String movieName;
    private String category;
    private LocalDateTime timestamp;
    
    public static MovieEvent created(Movie movie) {
        return new MovieEvent(
            EventType.CREATED,
            movie.getId(),
            movie.getName(),
            movie.getCategory(),
            LocalDateTime.now()
        );
    }
    
    public static MovieEvent updated(Movie movie) {
        return new MovieEvent(
            EventType.UPDATED,
            movie.getId(),
            movie.getName(),
            movie.getCategory(),
            LocalDateTime.now()
        );
    }
    
    public static MovieEvent deleted(Long movieId, String movieName) {
        return new MovieEvent(
            EventType.DELETED,
            movieId,
            movieName,
            null,
            LocalDateTime.now()
        );
    }
}