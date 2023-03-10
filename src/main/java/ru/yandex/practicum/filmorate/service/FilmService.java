package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.util.ObjectNotFoundException;

import java.util.List;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(@Qualifier("filmDAO") FilmStorage filmStorage, @Qualifier("userDAO") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getFilmById(int id) {
        Film film = filmStorage.getFilmById(id).orElse(null);
        if (film == null) {
            throw new ObjectNotFoundException("Фильм не найден");
        }
        return film;
    }

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        if (filmStorage.getFilmById(film.getId()).isEmpty()) {
            throw new ObjectNotFoundException("Несуществующий фильм");
        }
        return filmStorage.updateFilm(film);
    }

    public void likeFilm(int filmId, int userId) {
        Film film = getFilmById(filmId);
        User user = userStorage.getUserById(userId).orElse(null);

        if (film != null && user != null) {
            try {
                filmStorage.likeFilm(userId, filmId);
            } catch (DataAccessException e) {
                throw new ObjectNotFoundException("Лайк уже существует");
            }
        } else {
            throw new ObjectNotFoundException("Несуществующий фильм");
        }
    }

    public void removeLike(int filmId, int userId) {
        Film film = getFilmById(filmId);
        User user = userStorage.getUserById(userId).orElse(null);

        if (film != null && user != null) {
            try {
                filmStorage.removeLike(userId, filmId);
            } catch (DataAccessException ignored) {
            }
        } else {
            throw new ObjectNotFoundException("Несуществующий фильм или пользователь");
        }
    }

    public List<Film> getMostPopularFilms() {
        return filmStorage.getMostPopularFilms();
    }

    public List<Film> getMostPopularFilms(int size) {
        return filmStorage.getMostPopularFilms(size);
    }
}
