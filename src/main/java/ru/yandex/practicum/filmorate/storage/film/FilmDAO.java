package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.Genre;

import javax.sql.DataSource;
import java.util.*;

@Component
public class FilmDAO implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final String selectFilm = "SELECT f.id, f.name, f.description, f.release_date, f.duration, r.ID AS rating_id, r.NAME AS rating_name ";

    @Autowired
    public FilmDAO(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("\"film\"")
                .usingGeneratedKeyColumns("id")
                .usingColumns("name", "description", "release_date", "duration", "rating_id");
    }

    @Override
    public List<Film> getAll() {
        return jdbcTemplate.query(
                selectFilm +
                        "FROM \"film\" AS f " +
                        "LEFT JOIN \"rating\" r on f.RATING_ID = r.ID ", new FilmRowMapper(this));
    }

    @Override
    public Film createFilm(Film film) {
        Map<String, Object> parameters = new HashMap<>(5);
        parameters.put("name", film.getName());
        parameters.put("description", film.getDescription());
        parameters.put("release_date", film.getReleaseDate().toString());
        parameters.put("duration", film.getDuration());
        parameters.put("rating_id", film.getMpa().getId());

        film.setId(simpleJdbcInsert.executeAndReturnKey(parameters).intValue());
        List<Genre> genres = film.getGenres();
        if (genres != null) {
            insertFilmGenre(film, genres);
        }
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        jdbcTemplate.update(
                "UPDATE \"film\" SET NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ? , RATING_ID = ? WHERE ID = ? ",
                film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId(), film.getId()
        );
        List<Genre> genres = film.getGenres();
        if (genres != null) {
            Set<Genre> set = new HashSet<>(genres);
            Set<Genre> genresFromDB = new HashSet<>(getGenres(film.getId()));
            for (Genre genre : set) {
                if (genresFromDB.contains(genre)) {
                    genresFromDB.remove(genre);
                } else {
                    insertFilmGenre(film.getId(), genre.getId());
                }
            }

            for (Genre genre : genresFromDB) {
                jdbcTemplate.update(
                        "DELETE FROM \"film_genre\" WHERE FILM_ID=? AND GENRE_ID=?", film.getId(), genre.getId()
                );
            }
            film.setGenres(new ArrayList<>(set));
        }
        return film;
    }

    @Override
    public Optional<Film> getFilmById(int id) {
        return jdbcTemplate.query(
                selectFilm +
                        "FROM \"film\" AS f " +
                        "LEFT JOIN \"rating\" r on f.RATING_ID = r.ID " +
                        "WHERE f.ID = ?", new FilmRowMapper(this), id).stream().findAny();
    }

    @Override
    public void removeFilm(int id) {
        jdbcTemplate.update("DELETE FROM \"film\" WHERE ID = ?", id);
    }

    @Override
    public void likeFilm(int userId, int filmId) {
        jdbcTemplate.update("INSERT INTO \"film_like\"(user_id, film_id) VALUES ( ?, ? ) ", userId, filmId);
    }

    @Override
    public void removeLike(int userId, int filmId) {
        jdbcTemplate.update("DELETE FROM \"film_like\" WHERE USER_ID = ? AND FILM_ID = ?", userId, filmId);
    }

    @Override
    public List<Film> getMostPopularFilms() {
        return jdbcTemplate.query(
                selectFilm +
                        "FROM (" +
                        "SELECT DISTINCT f.ID, count(fl.USER_ID) " +
                        "FROM \"film\" AS f " +
                        "LEFT JOIN \"film_like\" AS fl ON f.ID = fl.FILM_ID " +
                        "GROUP BY f.ID " +
                        "ORDER BY count(fl.USER_ID) DESC " +
                        "LIMIT 10 " +
                        ") AS fl " +
                        "LEFT JOIN \"film\" AS f ON fl.ID = f.ID " +
                        "LEFT JOIN \"rating\" AS r ON f.RATING_ID = r.ID ",
                new FilmRowMapper(this));
    }

    @Override
    public List<Film> getMostPopularFilms(int size) {
        return jdbcTemplate.query(
                selectFilm +
                        "FROM (" +
                        "SELECT DISTINCT fl.FILM_ID, count(fl.USER_ID) " +
                        "FROM \"film_like\" AS fl " +
                        "GROUP BY fl.FILM_ID " +
                        "ORDER BY count(fl.USER_ID) DESC " +
                        "LIMIT ? " +
                        ") AS fl " +
                        "LEFT JOIN \"film\" AS f ON fl.FILM_ID = f.ID " +
                        "LEFT JOIN \"rating\" AS r ON f.RATING_ID = r.ID ",
                new FilmRowMapper(this), size);
    }

    private void insertFilmGenre(int filmId, int genreId) {
        jdbcTemplate.update("INSERT INTO \"film_genre\" VALUES ( ?, ? )", filmId, genreId);
    }

    private void insertFilmGenre(Film film, List<Genre> genres) {
        for (Genre genre : genres) {
            jdbcTemplate.update("INSERT INTO \"film_genre\" VALUES ( ?, ? )", film.getId(), genre.getId());
        }
    }

    public List<Genre> getGenres(int filmId) {
        return jdbcTemplate.query(
                "SELECT g.ID, g.NAME" +
                        " FROM \"film_genre\" AS fg " +
                        "LEFT JOIN \"genre\" AS g on g.ID = fg.GENRE_ID " +
                        "WHERE fg.FILM_ID = ?",
                new BeanPropertyRowMapper<>(Genre.class), filmId);
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        return jdbcTemplate.query(
                selectFilm +
                        "FROM (" +
                        "SELECT DISTINCT f.FILM_ID, count(f.USER_ID) " +
                        "FROM \"film_like\" AS f " +
                        "LEFT JOIN \"film_like\" AS fl ON f.FILM_ID = fl.FILM_ID " +
                        "WHERE f.USER_ID = ? and fl.USER_ID = ? " +
                        "GROUP BY f.FILM_ID " +
                        "ORDER BY count(f.USER_ID) DESC " +
                        ") AS fl " +
                        "LEFT JOIN \"film\" AS f ON fl.FILM_ID = f.ID " +
                        "LEFT JOIN \"rating\" AS r ON f.RATING_ID = r.ID ",
                new FilmRowMapper(this), userId, friendId);
    }
}
