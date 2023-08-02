package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.models.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    List<Film> getAll();

    Film createFilm(Film film);

    Film updateFilm(Film film);

    Optional<Film> getFilmById(int id);

    void removeFilm(int id);

    void likeFilm(int userId, int filmId);

    void removeLike(int userId, int filmId);

    List<Film> getMostPopularFilms();

    List<Film> getMostPopularFilms(int size);

    List<Film> searchByTitle(String query);

    List<Film> searchByDirector(String query);

    List<Film> searchByTitleDirector(String query);

    List<Film> getCommonFilms(int userId, int friendId);

    List<Film> getDirectorsFilmsSortByYear(int directorId);

    List<Film> getDirectorsFilmsSortByLikes(int directorId);

    List<Film> getMostPopularFilms(int size, int genreId, int year);

    List<Film> getMostPopularFilmsByGenre(int size, int genreId);

    List<Film> getMostPopularFilmsByYear(int size, int year);

    void deleteFilmById(int id);

    List<Film> getLikedFilms(int userId);
}
