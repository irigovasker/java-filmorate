package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.models.Rating;

import java.util.List;

@Component
public class RatingDAO {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public RatingDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Rating> getAllRatings() {
        return jdbcTemplate.query("SELECT * FROM \"rating\" ", new BeanPropertyRowMapper<>(Rating.class));
    }

    public Rating getRatingById(int id) {
        return jdbcTemplate.query("SELECT * FROM \"rating\" WHERE id = ?", new BeanPropertyRowMapper<>(Rating.class), id)
                .stream().findAny().orElse(null);
    }
}
