package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.models.Director;
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
        List<Director> directors = film.getDirectors();
        if (directors != null) {
            insertFilmDirector(film, directors);
        }
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        jdbcTemplate.update(
                "UPDATE \"film\" SET NAME = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ? , RATING_ID = ? WHERE ID = ? ",
                film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId(), film.getId()
        );
        film.setGenres(updateGenres(film));
        film.setDirectors(updateDirectors(film));
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
                        "SELECT DISTINCT f.ID, count(fl.USER_ID) " +
                        "FROM \"film\" AS f " +
                        "LEFT JOIN \"film_like\" AS fl ON f.ID = fl.FILM_ID " +
                        "GROUP BY f.ID " +
                        "ORDER BY count(fl.USER_ID) DESC " +
                        "LIMIT ? " +
                        ") AS fl " +
                        "LEFT JOIN \"film\" AS f ON fl.ID = f.ID " +
                        "LEFT JOIN \"rating\" AS r ON f.RATING_ID = r.ID ",
                new FilmRowMapper(this), size);
    }

    @Override
    public List<Film> searchByTitle(String query) {
        String q = "%" + query + "%";
        return jdbcTemplate.query(
                selectFilm +
                        "FROM \"film\" AS f " +
                        "LEFT JOIN \"rating\" r on f.RATING_ID = r.ID " +
                        "LEFT JOIN \"film_like\" AS fl on f.ID = fl.FILM_ID " +
                        "WHERE f.name ILIKE ? " +
                        "GROUP BY f.ID " +
                        "ORDER BY COUNT(fl.USER_ID) DESC",
                new FilmRowMapper(this), q);
    }

    @Override
    public List<Film> searchByDirector(String query) {
        String q = "%" + query + "%";
        return jdbcTemplate.query(
                selectFilm +
                        "FROM \"film\" AS f " +
                        "LEFT JOIN \"rating\" r on f.RATING_ID = r.ID " +
                        "LEFT JOIN \"film_director\" AS fd on f.ID = fd.FILM_ID " +
                        "LEFT JOIN \"director\" AS d on fd.DIRECTOR_ID = d.ID " +
                        "LEFT JOIN \"film_like\" AS fl on f.ID = fl.FILM_ID " +
                        "WHERE d.name ILIKE ? " +
                        "GROUP BY f.ID " +
                        "ORDER BY COUNT(fl.USER_ID) DESC",
                new FilmRowMapper(this), q);
    }

    @Override
    public List<Film> searchByTitleDirector(String query) {
        String q = "%" + query + "%";
        return jdbcTemplate.query(
                selectFilm +
                        "FROM \"film\" AS f " +
                        "LEFT JOIN \"rating\" r on f.RATING_ID = r.ID " +
                        "LEFT JOIN \"film_director\" AS fd on f.ID = fd.FILM_ID " +
                        "LEFT JOIN \"director\" AS d on fd.DIRECTOR_ID = d.ID " +
                        "LEFT JOIN \"film_like\" AS fl on f.ID = fl.FILM_ID " +
                        "WHERE f.name ILIKE ? OR d.name ILIKE ? " +
                        "GROUP BY f.ID " +
                        "ORDER BY COUNT(fl.USER_ID) DESC",
                new FilmRowMapper(this), q, q);
    }

    private void insertFilmGenre(int filmId, int genreId) {
        jdbcTemplate.update("INSERT INTO \"film_genre\" VALUES ( ?, ? )", filmId, genreId);
    }

    private void insertFilmGenre(Film film, List<Genre> genres) {
        for (Genre genre : genres) {
            jdbcTemplate.update("INSERT INTO \"film_genre\" VALUES ( ?, ? )", film.getId(), genre.getId());
        }
    }

    private void insertFilmDirector(Film film, List<Director> directors) {
        for (Director director : directors) {
            jdbcTemplate.update("INSERT INTO \"film_director\" VALUES ( ?, ? )", film.getId(), director.getId());
        }
    }

    private void insertFilmDirector(int filmId, int directorId) {
        jdbcTemplate.update("INSERT INTO \"film_director\" VALUES ( ?, ? )", filmId, directorId);
    }


    public List<Director> getDirectors(int filmId) {
        return jdbcTemplate.query(
                "SELECT d.ID, d.NAME " +
                        "FROM \"film_director\" AS fd " +
                        "LEFT JOIN \"director\" AS d ON d.ID = fd.DIRECTOR_ID " +
                        "WHERE fd.FILM_ID = ? ", new BeanPropertyRowMapper<>(Director.class), filmId
        );
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

    private List<Genre> updateGenres(Film film) {
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
            return new ArrayList<>(set);
        } else {
            return Collections.emptyList();
        }
    }


    private List<Director> updateDirectors(Film film) {
        List<Director> directors = film.getDirectors();
        if (directors != null) {
            Set<Director> set = new HashSet<>(directors);
            Set<Director> directorsFormDB = new HashSet<>(getDirectors(film.getId()));
            for (Director director : set) {
                if (directorsFormDB.contains(director)) {
                    directorsFormDB.remove(director);
                } else {
                    insertFilmDirector(film.getId(), director.getId());
                }
            }

            for (Director director : directorsFormDB) {
                jdbcTemplate.update(
                        "DELETE FROM \"film_director\" WHERE FILM_ID=? AND DIRECTOR_ID=?", film.getId(), director.getId()
                );
            }
            return new ArrayList<>(set);
        } else {
            jdbcTemplate.update("DELETE FROM \"film_director\" WHERE FILM_ID = ? ", film.getId());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Film> getDirectorsFilmsSortByYear(int directorId) {
        return jdbcTemplate.query(
                selectFilm +
                        "FROM (" +
                        "SELECT fd.FILM_ID " +
                        "FROM \"film_director\" AS fd " +
                        "WHERE DIRECTOR_ID = ?) AS fd " +
                        "LEFT JOIN \"film\" AS f ON fd.FILM_ID = f.ID " +
                        "LEFT JOIN \"rating\" AS r ON f.RATING_ID = r.ID " +
                        "ORDER BY EXTRACT(YEAR FROM f.RELEASE_DATE) ",
                new FilmRowMapper(this), directorId
        );
    }

    @Override
    public List<Film> getDirectorsFilmsSortByLikes(int directorId) {
        return jdbcTemplate.query(
                selectFilm +
                        "FROM (" +
                        "SELECT f.FILM_ID, COUNT(fl.USER_ID) " +
                        "FROM \"film_director\" AS f " +
                        "LEFT JOIN \"film_like\" as fl " +
                        "WHERE f.DIRECTOR_ID = ? " +
                        "GROUP BY f.FILM_ID " +
                        "ORDER BY COUNT(fl.USER_ID) DESC) AS fd " +
                        "LEFT JOIN \"film\" AS f ON fd.FILM_ID = f.ID " +
                        "LEFT JOIN \"rating\" AS r ON f.RATING_ID = r.ID ",
                new FilmRowMapper(this), directorId
        );
    }

    @Override
    public void deleteFilmById(int filmId) {
        jdbcTemplate.update(
                "DELETE FROM \"film\" WHERE ID = ?", filmId);
    }
}
