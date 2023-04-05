package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.models.Genre;
import ru.yandex.practicum.filmorate.util.ObjectNotFoundException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GenreStorage {
    private final JdbcTemplate jdbcTemplate;


    public List<Genre> getAllGenres() {
        return jdbcTemplate.query("SELECT * FROM \"genre\" ", new BeanPropertyRowMapper<>(Genre.class));
    }

    public Genre getGenreById(int id) {
        return jdbcTemplate.query("SELECT * FROM \"genre\" WHERE id =?",
                        new BeanPropertyRowMapper<>(Genre.class), id)
                .stream().findAny().orElseThrow(() -> new ObjectNotFoundException("Несуществующий жанр"));
    }
}
