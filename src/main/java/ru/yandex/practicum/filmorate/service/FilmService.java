package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.storage.DirectorDAO;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.util.ObjectNotFoundException;

import java.util.List;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final DirectorDAO directorDAO;

    @Autowired
    public FilmService(@Qualifier("filmDAO") FilmStorage filmStorage, @Qualifier("userDAO") UserStorage userStorage, DirectorDAO directorDAO) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.directorDAO = directorDAO;
    }

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
        switch (by) {
            case "title":
                return filmStorage.searchByTitle(query);
            case "director":
                return filmStorage.searchByDirector(query);
            case "title,director":
            case "director,title":
                return filmStorage.searchByTitleDirector(query);
            default:
                throw new ObjectNotFoundException("Невалидный поиск");
        }
    }

    public void likeFilm(int filmId, int userId) {
        Film film = getFilmById(filmId);
        User user = userStorage.getUserById(userId).orElseThrow(() -> new ObjectNotFoundException("Несуществующий пользователь"));

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

    public List<Film> getMostPopularFilms() {
        return filmStorage.getMostPopularFilms();
    }

    public List<Film> getMostPopularFilms(int size) {
        return filmStorage.getMostPopularFilms(size);
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> getDirectorsFilmsSortByYear(int directorId) {
        if (directorDAO.findOneById(directorId).isEmpty()) {
            throw new ObjectNotFoundException("Несуществующий режиссер");
        }
        return filmStorage.getDirectorsFilmsSortByYear(directorId);
    }

    public List<Film> getDirectorsFilmsSortByLikes(int directorId) {
        if (directorDAO.findOneById(directorId).isEmpty()) {
            throw new ObjectNotFoundException("Несуществующий режиссер");
        }
        return filmStorage.getDirectorsFilmsSortByLikes(directorId);
    }

    public List<Film> getMostPopularFilms(int genreId, int year) {
        return filmStorage.getMostPopularFilms(genreId, year);
    }

    public List<Film> getMostPopularFilms(int size, int genreId, int year) {
        return filmStorage.getMostPopularFilms(size, genreId, year);
    }

    public List<Film> getMostPopularFilmsByGenre(int genreId) {
        return filmStorage.getMostPopularFilmsByGenre(genreId);
    }

    public List<Film> getMostPopularFilmsByGenre(int size, int genreId) {
        return filmStorage.getMostPopularFilmsByGenre(size, genreId);
    }

    public List<Film> getMostPopularFilmsByYear(int year) {
        return filmStorage.getMostPopularFilmsByYear(year);
    }

    public List<Film> getMostPopularFilmsByYear(int size, int year) {
        return filmStorage.getMostPopularFilmsByYear(size, year);
    }

    public void deleteFilmById(int filmId) {
        filmStorage.deleteFilmById(filmId);
    }
}
