package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.util.ObjectNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final DirectorStorage directorStorage;
    private final FeedStorage feedStorage;

    private static final String TITLE = "title";
    private static final String DIRECTOR = "director";

    public List<Film> getAll() {
        return filmStorage.getAll();
    }


    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id).orElseThrow(() -> new ObjectNotFoundException("Несуществующий фильм"));
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

    public List<Film> search(String query, String by) {

        List<String> byList = List.of(by.split(","));

        switch (byList.size()) {
            case 1:
                if (TITLE.contains(by)) {
                    return filmStorage.searchByTitle(query);
                } else if (DIRECTOR.contains(by)) {
                    return filmStorage.searchByDirector(query);
                }
                throw new ObjectNotFoundException("Невалидный поиск");
            case 2:
                if (byList.contains(TITLE) & byList.contains(DIRECTOR)) {
                    return filmStorage.searchByTitleDirector(query);
                }
                throw new ObjectNotFoundException("Невалидный поиск");
            default:
                throw new ObjectNotFoundException("Неверное количество параметров 'By'");
        }
    }

    public void likeFilm(int filmId, int userId) {
        Film film = getFilmById(filmId);
        User user = userStorage.getUserById(userId).orElseThrow(() -> new ObjectNotFoundException("Несуществующий пользователь"));
        feedStorage.writeFeed(userId, "LIKE", "ADD", filmId);
        if (film != null && user != null) {
            try {
                filmStorage.likeFilm(userId, filmId);
            } catch (DataAccessException ignored) {
            }
        } else {
            throw new ObjectNotFoundException("Несуществующий фильм");
        }
    }

    public void removeLike(int filmId, int userId) {
        Film film = getFilmById(filmId);
        User user = userStorage.getUserById(userId).orElseThrow(() -> new ObjectNotFoundException("Несуществующий пользователь"));

        if (film != null && user != null) {
            try {
                filmStorage.removeLike(userId, filmId);
            } catch (DataAccessException ignored) {
            }
        } else {
            throw new ObjectNotFoundException("Несуществующий фильм или пользователь");
        }
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> getDirectorsFilmsSortByYear(int directorId) {
        if (directorStorage.findOneById(directorId).isEmpty()) {
            throw new ObjectNotFoundException("Несуществующий режиссер");
        }
        return filmStorage.getDirectorsFilmsSortByYear(directorId);
    }

    public List<Film> getDirectorsFilmsSortByLikes(int directorId) {
        if (directorStorage.findOneById(directorId).isEmpty()) {
            throw new ObjectNotFoundException("Несуществующий режиссер");
        }
        return filmStorage.getDirectorsFilmsSortByLikes(directorId);
    }

    public List<Film> getMostPopularFilms(Integer size, Integer genreId, Integer year) {
        if (genreId != null & year != null) {
            return filmStorage.getMostPopularFilms(size, genreId, year);
        } else if (genreId != null) {
            return filmStorage.getMostPopularFilmsByGenre(size, genreId);
        } else if (year != null) {
            return filmStorage.getMostPopularFilmsByYear(size, year);
        }
        return filmStorage.getMostPopularFilms(size);
    }

    public void deleteFilmById(int filmId) {
        filmStorage.deleteFilmById(filmId);
    }
}
