package ru.yandex.practicum.filmorate.util;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.User;

import java.time.LocalDate;

@Slf4j
public class Validator {
    public static void validateUser(User user) {
        LocalDate currentDate = LocalDate.now();
        if (user.getLogin().contains(" ")) {
            throw new CustomValidateException("Login cannot contains whitespase");
        }
        if (user.getBirthday().isAfter(currentDate)) {
            log.warn("Была передана некорректная дата рождения пользователя: " + user);
            throw new CustomValidateException("Birthday can't be in the future");
        }
    }

    public static void validateFilm(Film film) {
        LocalDate date = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(date)) {
            log.warn("Была передана некорректная дата релиза фильма: " + film);
            throw new CustomValidateException("At that time, the cinema had not yet been invented");
        }
    }
}
