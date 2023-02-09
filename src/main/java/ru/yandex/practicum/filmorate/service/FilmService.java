package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
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
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
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
        return filmStorage.updateFilm(film);
    }

    public boolean likeFilm(int filmId, int userId) {
        Film film = getFilmById(filmId);
        User user = userStorage.getUserById(userId).orElse(null);

        if (film != null && user != null) {
            if (film.getLikedUsers().contains(userId)) {
                return true;
            } else {
                return film.getLikedUsers().add(userId);
            }
        } else {
            throw new ObjectNotFoundException("Несуществующий фильм");
        }
    }

    public boolean removeLike(int filmId, int userId) {
        Film film = getFilmById(filmId);
        User user = userStorage.getUserById(userId).orElse(null);

        if (film != null && user != null) {
            if (film.getLikedUsers().contains(userId)) {
                return film.getLikedUsers().remove(userId);
            } else {
                throw new RuntimeException("Несуществующий лайк");
            }
        } else {
            throw new ObjectNotFoundException("Несуществующий фильм или пользователь");
        }
    }

    public List<Film> getMostPopularFilms() {
        try {
            return getSortedListByPopular().subList(0, 10);
        } catch (IndexOutOfBoundsException e) {
            return getSortedListByPopular();
        }
    }

    public List<Film> getMostPopularFilms(int size) {
        try {
            return getSortedListByPopular().subList(0, size);
        } catch (IndexOutOfBoundsException e) {
            return getSortedListByPopular();
        }
    }

    private List<Film> getSortedListByPopular() {
        List<Film> films = getAll();
        films.sort((film1, film2) -> film2.getLikedUsers().size() - film1.getLikedUsers().size());
        return films;
    }
}
