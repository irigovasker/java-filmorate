package ru.yandex.practicum.filmorate.models;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class Director {
    private int id;
    @NotBlank(message = "Name must contain characters")
    private String name;
}
