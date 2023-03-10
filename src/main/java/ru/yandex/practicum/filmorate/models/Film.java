package ru.yandex.practicum.filmorate.models;

import lombok.Data;


import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Data
public class Film {
    private int id;
    @NotNull(message = "Name should not be empty")
    @NotBlank(message = "Name cannot be empty")
    private String name;
    @Size(max = 200, message = "Description size should not be more than 200 characters")
    private String description;
    private LocalDate releaseDate;
    @Min(value = 0, message = "Duration cannot be negative")
    private int duration;
    private Set<Integer> likedUsers;
    private List<Genre> genres;
    private Rating mpa;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Film film = (Film) o;

        if (id != film.id) return false;
        if (duration != film.duration) return false;
        if (!Objects.equals(name, film.name)) return false;
        if (!Objects.equals(description, film.description)) return false;
        if (!Objects.equals(releaseDate, film.releaseDate)) return false;
        if (!Objects.equals(genres, film.genres)) return false;
        return Objects.equals(mpa, film.mpa);
    }

    @Override
    public int hashCode() {
        return id;
    }
}
