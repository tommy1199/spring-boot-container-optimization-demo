package com.example.movieservice.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table("movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    @Id
    private Long id;

    @NotBlank(message = "Movie name is required")
    private String name;

    @NotNull(message = "Release date is required")
    private LocalDate releaseDate;

    @NotBlank(message = "Category is required")
    private String category;
}
