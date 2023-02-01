package ru.yandex.practicum.filmorate.model;

import lombok.Data;


import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

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
}
