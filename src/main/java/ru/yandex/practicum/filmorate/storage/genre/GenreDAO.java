package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.models.Genre;
import ru.yandex.practicum.filmorate.util.ObjectNotFoundException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GenreDAO implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;


    @Override
    public List<Genre> getAllGenres() {
        return jdbcTemplate.query("SELECT * FROM \"genre\" ", new BeanPropertyRowMapper<>(Genre.class));
    }

    @Override
    public Genre getGenreById(int id) {
        return jdbcTemplate.query("SELECT * FROM \"genre\" WHERE id =?",
                        new BeanPropertyRowMapper<>(Genre.class), id)
                .stream().findAny().orElseThrow(() -> new ObjectNotFoundException("Несуществующий жанр"));
    }
}
