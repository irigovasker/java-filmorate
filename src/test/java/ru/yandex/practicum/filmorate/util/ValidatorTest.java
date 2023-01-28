package ru.yandex.practicum.filmorate.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

public class ValidatorTest {
    @Test
    void userWithBirthdayInFutureExpectCustomValidateException() {
        User user = new User();
        user.setLogin("Login");
        user.setBirthday(LocalDate.of(2124, 1, 1));
        Assertions.assertThrows(CustomValidateException.class, () -> Validator.validateUser(user));
    }

    @Test
    void userWithWhitespaceInLoginExpectCustomValidateException() {
        User user = new User();
        user.setLogin("Log in");
        Assertions.assertThrows(CustomValidateException.class, () -> Validator.validateUser(user));
    }

    @Test
    void filmWithIncorrectReleaseDateExpectCustomValidateException() {
        Film film = new Film();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        Validator.validateFilm(film);

        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        Assertions.assertThrows(CustomValidateException.class, () -> Validator.validateFilm(film));
    }
}
