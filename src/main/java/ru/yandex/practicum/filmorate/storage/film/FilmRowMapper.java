package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class FilmRowMapper implements RowMapper<Film> {
    private final FilmDAO filmDAO;

    public FilmRowMapper(FilmDAO filmDAO) {
        this.filmDAO = filmDAO;
    }

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        Rating rating = new Rating();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setDuration(rs.getInt("duration"));
        film.setGenres(filmDAO.getGenres(rs.getInt("id")));
        rating.setId(rs.getInt("rating_id"));
        rating.setName(rs.getString("rating_name"));
        film.setReleaseDate(LocalDate.parse(rs.getString("release_date")));
        if (rating.getId() != 0) {
            film.setMpa(rating);
        }
        return film;
    }
}
