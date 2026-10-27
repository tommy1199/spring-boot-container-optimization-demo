package com.example.movieservice.repository;

import com.example.movieservice.model.Movie;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends ListCrudRepository<Movie, Long> {
    List<Movie> findByCategory(String category);
    List<Movie> findByNameContainingIgnoreCase(String name);
}
