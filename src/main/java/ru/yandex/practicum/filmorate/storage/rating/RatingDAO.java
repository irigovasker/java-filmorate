package ru.yandex.practicum.filmorate.storage.rating;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.models.Rating;
import ru.yandex.practicum.filmorate.util.ObjectNotFoundException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RatingDAO implements RatingStorage {
    private final JdbcTemplate jdbcTemplate;


    @Override
    public List<Rating> getAllRatings() {
        return jdbcTemplate.query("SELECT * FROM \"rating\" ", new BeanPropertyRowMapper<>(Rating.class));
    }

    @Override
    public Rating getRatingById(int id) {
        return jdbcTemplate.query("SELECT * FROM \"rating\" WHERE id = ?", new BeanPropertyRowMapper<>(Rating.class), id)
                .stream().findAny().orElseThrow(() -> new ObjectNotFoundException("Несуществующий рейтинг"));
    }
}
