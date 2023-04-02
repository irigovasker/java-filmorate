package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.models.Feed;
import javax.sql.DataSource;
import java.util.List;

@Component
public class FeedDAO {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    @Autowired
    public FeedDAO(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("\"feed\"")
                .usingGeneratedKeyColumns("event_id")
                .usingColumns("timestamp", "user_id", "event_type", "operation", "entity_id");
    }

    public void writeFeed (int userId, String eventType, String operation, Integer entityId) {
        jdbcTemplate.update("INSERT INTO \"feed\" (timestamp, user_id, event_type, operation, entity_id) VALUES ( ?, ?, ?, ?, ? )",
                System.currentTimeMillis(), userId, eventType, operation, entityId);
    }

    public List<Feed> getUserFeed(int userId) {
        return jdbcTemplate.query(
                        "SELECT * FROM \"feed\" AS f " +
                        "WHERE f.USER_ID = ?",
                new BeanPropertyRowMapper<>(Feed.class), userId
        );
    }
}