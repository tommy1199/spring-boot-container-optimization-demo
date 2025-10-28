package com.example.movieservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Movie name is required")
    @Column(nullable = false)
    private String name;
    
    @NotNull(message = "Release date is required")
    @Column(nullable = false)
    private LocalDate releaseDate;
    
    @NotBlank(message = "Category is required")
    @Column(nullable = false)
    private String category;
}