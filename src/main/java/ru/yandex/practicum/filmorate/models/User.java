package ru.yandex.practicum.filmorate.models;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != user.id) return false;
        if (!Objects.equals(email, user.email)) return false;
        if (!Objects.equals(login, user.login)) return false;
        if (!Objects.equals(name, user.name)) return false;
        return Objects.equals(birthday, user.birthday);
    }

    @Override
    public int hashCode() {
        return id;
    }
}
