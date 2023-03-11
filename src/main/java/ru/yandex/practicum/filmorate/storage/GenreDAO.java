package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.models.Genre;
import ru.yandex.practicum.filmorate.util.ObjectNotFoundException;

import java.util.List;

@Component
public class GenreDAO {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Genre> getAllGenres() {
        return jdbcTemplate.query("SELECT * FROM \"genre\" ", new BeanPropertyRowMapper<>(Genre.class));
    }

    public Genre getGenreById(int id) {
        return jdbcTemplate.query("SELECT * FROM \"genre\" WHERE id =?"
                        , new BeanPropertyRowMapper<>(Genre.class), id)
                .stream().findAny().orElseThrow(() -> new ObjectNotFoundException("Несуществующий жанр"));
    }
}
