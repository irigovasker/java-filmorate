package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.models.Director;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DirectorStorage {
    private final JdbcTemplate template;
    private final SimpleJdbcInsert simpleJdbcInsert;

    @Autowired
    public DirectorStorage(JdbcTemplate template, DataSource dataSource) {
        this.template = template;
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("\"director\"")
                .usingGeneratedKeyColumns("id")
                .usingColumns("name");
    }

    public List<Director> findAll() {
        return template.query(
                "SELECT * FROM \"director\" ", new BeanPropertyRowMapper<>(Director.class)
        );
    }

    public Optional<Director> findOneById(int id) {
        return template.query(
                "SELECT * FROM \"director\" WHERE id = ?",
                new BeanPropertyRowMapper<>(Director.class), id
        ).stream().findAny();
    }

    public Director createDirector(Director director) {
        director.setId(simpleJdbcInsert.executeAndReturnKey(Map.of("name", director.getName())).intValue());
        return director;
    }

    public Director updateDirector(Director director) {
        template.update(
                "UPDATE \"director\" SET name = ? WHERE id = ? ", director.getName(), director.getId()
        );
        return director;
    }

    public void deleteDirectorById(int id) {
        template.update("DELETE FROM \"director\" WHERE id = ? ", id);
    }
}
