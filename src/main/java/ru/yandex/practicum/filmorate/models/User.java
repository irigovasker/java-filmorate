package ru.yandex.practicum.filmorate.models;


import lombok.Data;


import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

@Data
public class User {
    private int id;
    @NotNull(message = "Email should not be empty")
    @Email(message = "Email should be correct")
    private String email;
    @NotEmpty(message = "Login should not be empty")
    @NotBlank(message = "Login should not be empty")
    private String login;
    private String name;
    private LocalDate birthday;
    private Set<Integer> friends;
}
