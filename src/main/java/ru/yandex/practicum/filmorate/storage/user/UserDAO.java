package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.models.User;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class UserDAO implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    @Autowired
    public UserDAO(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("\"user\"")
                .usingGeneratedKeyColumns("id")
                .usingColumns("email", "login", "name", "birthday");
    }

    @Override
    public List<User> getAll() {
        return jdbcTemplate.query("SELECT * FROM \"user\"", new BeanPropertyRowMapper<>(User.class));
    }

    @Override
    public User createUser(User user) {
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("email", user.getEmail());
        parameters.put("login", user.getLogin());
        parameters.put("name", user.getName());
        parameters.put("birthday", user.getBirthday().toString());

        user.setId(simpleJdbcInsert.executeAndReturnKey(parameters).intValue());
        return user;
    }

    @Override
    public User updateUser(User user) {
        jdbcTemplate.update(
                "UPDATE \"user\" SET email = ?, login = ?, name = ?, birthday = ? WHERE id=? ",
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public Optional<User> getUserById(int userId) {
        return jdbcTemplate.query("SELECT * FROM \"user\" WHERE id=?", new BeanPropertyRowMapper<>(User.class), userId)
                .stream().findAny();
    }

    @Override
    public List<User> getUserFriends(int userId) {
        return jdbcTemplate.query(
                "SELECT u.id, u.email, u.login, u.name, u.birthday " +
                        "FROM \"friendship\" AS f " +
                        "LEFT JOIN \"user\" AS u ON f.SECOND_USER = u.ID " +
                        "WHERE f.FIRST_USER = ? AND (f.STATUS = 1 OR f.STATUS = 3) " +
                        "UNION " +
                        "SELECT u.id, u.email, u.login, u.name, u.birthday " +
                        "FROM \"friendship\" AS f " +
                        "LEFT JOIN \"user\" AS u ON f.FIRST_USER = u.ID " +
                        "WHERE f.SECOND_USER = ? AND (f.STATUS = 2 OR f.STATUS = 3)",
                new BeanPropertyRowMapper<>(User.class), userId, userId
        );
    }

    @Override
    public List<User> getSubscribers(int userId) {
        return jdbcTemplate.query(
                "SELECT u.id, u.email, u.login, u.name, u.birthday " +
                        "FROM \"friendship\" AS f " +
                        "LEFT JOIN \"user\" AS u ON f.SECOND_USER = u.ID " +
                        "WHERE f.FIRST_USER = ? AND f.STATUS = 2 " +
                        "UNION " +
                        "SELECT u.id, u.email, u.login, u.name, u.birthday " +
                        "FROM \"friendship\" AS f " +
                        "LEFT JOIN \"user\" AS u ON f.FIRST_USER = u.ID " +
                        "WHERE f.SECOND_USER = ? AND f.STATUS = 1 ",
                new BeanPropertyRowMapper<>(User.class), userId, userId
        );
    }

    @Override
    public Relation getRelation(int userId, int secondUserId) {
        return jdbcTemplate.query(
                "SELECT * FROM \"friendship\" "+
                        "WHERE (FIRST_USER = ? AND SECOND_USER = ?) OR (SECOND_USER = ? AND FIRST_USER = ?) ",
                new BeanPropertyRowMapper<>(Relation.class), userId, secondUserId, secondUserId, userId)
                .stream().findAny().orElse(null);
    }

    @Override
    public void addRelation(int userId, int secondUserId) {
        jdbcTemplate.update("INSERT INTO \"friendship\"(FIRST_USER, SECOND_USER, status) VALUES ( ?, ?, ? )",
                userId, secondUserId, 1);
    }

    @Override
    public void changeRelationStatus(Relation relation, int statusId) {
        jdbcTemplate.update("UPDATE \"friendship\" SET STATUS = ? WHERE FIRST_USER = ? AND SECOND_USER = ?",
                statusId, relation.getFirstUser(), relation.getSecondUser());
    }

    @Override
    public void removeRelation(Relation relation) {
        jdbcTemplate.update("DELETE FROM \"friendship\" WHERE FIRST_USER = ? AND SECOND_USER = ?",
                relation.getFirstUser(), relation.getSecondUser());
    }

    @Override
    public void removeUser(int id) {
        jdbcTemplate.update("DELETE FROM \"user\" WHERE ID = ?", id);
    }
}
