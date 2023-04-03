package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.models.Feed;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.storage.FeedDAO;
import ru.yandex.practicum.filmorate.storage.film.FilmDAO;
import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UserDAO implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final FilmDAO filmDAO;
    private final FeedDAO feedDAO;
    private static final String SELECT_USER = "SELECT u.id, u.email, u.login, u.name, u.birthday ";

    @Autowired
    public UserDAO(JdbcTemplate jdbcTemplate, DataSource dataSource, FilmDAO filmDAO, FeedDAO feedDAO) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmDAO = filmDAO;
        this.feedDAO = feedDAO;
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
        return jdbcTemplate.query(SELECT_USER + "FROM \"user\" AS u WHERE id=?", new BeanPropertyRowMapper<>(User.class), userId)
                .stream().findAny();
    }

    @Override
    public List<User> getUserFriends(int userId) {
        return jdbcTemplate.query(
                SELECT_USER +
                        "FROM \"friendship\" AS f " +
                        "LEFT JOIN \"user\" AS u ON f.SECOND_USER = u.ID " +
                        "WHERE f.FIRST_USER = ? AND (f.STATUS = 1 OR f.STATUS = 3) " +
                        "UNION " +
                        SELECT_USER +
                        "FROM \"friendship\" AS f " +
                        "LEFT JOIN \"user\" AS u ON f.FIRST_USER = u.ID " +
                        "WHERE f.SECOND_USER = ? AND (f.STATUS = 2 OR f.STATUS = 3)",
                new BeanPropertyRowMapper<>(User.class), userId, userId
        );
    }

    @Override
    public List<User> getSubscribers(int userId) {
        return jdbcTemplate.query(
                SELECT_USER +
                        "FROM \"friendship\" AS f " +
                        "LEFT JOIN \"user\" AS u ON f.SECOND_USER = u.ID " +
                        "WHERE f.FIRST_USER = ? AND f.STATUS = 2 " +
                        "UNION " +
                        SELECT_USER +
                        "FROM \"friendship\" AS f " +
                        "LEFT JOIN \"user\" AS u ON f.FIRST_USER = u.ID " +
                        "WHERE f.SECOND_USER = ? AND f.STATUS = 1 ",
                new BeanPropertyRowMapper<>(User.class), userId, userId
        );
    }

    @Override
    public Relation getRelation(int userId, int secondUserId) {
        return jdbcTemplate.query(
                        "SELECT * FROM \"friendship\" " +
                                "WHERE (FIRST_USER = ? AND SECOND_USER = ?) OR (SECOND_USER = ? AND FIRST_USER = ?) ",
                        new BeanPropertyRowMapper<>(Relation.class), userId, secondUserId, secondUserId, userId)
                .stream().findAny().orElse(null);
    }

    @Override
    public void addRelation(int userId, int secondUserId) {
        jdbcTemplate.update("INSERT INTO \"friendship\"(FIRST_USER, SECOND_USER, status) VALUES ( ?, ?, ? )",
                userId, secondUserId, 1);
        feedDAO.writeFeed(userId, "FRIEND", "ADD", secondUserId);
    }

    @Override
    public void changeRelationStatus(Relation relation, int statusId) {
        jdbcTemplate.update("UPDATE \"friendship\" SET STATUS = ? WHERE FIRST_USER = ? AND SECOND_USER = ?",
                statusId, relation.getFirstUser(), relation.getSecondUser());
        feedDAO.writeFeed(relation.getFirstUser(), "FRIEND", "UPDATE", relation.getSecondUser());
    }

    @Override
    public void removeRelation(Relation relation) {
        jdbcTemplate.update("DELETE FROM \"friendship\" WHERE FIRST_USER = ? AND SECOND_USER = ?",
                relation.getFirstUser(), relation.getSecondUser());
        feedDAO.writeFeed(relation.getFirstUser(), "FRIEND", "REMOVE", relation.getSecondUser());
    }

    @Override
    public void removeUser(int id) {
        jdbcTemplate.update("DELETE FROM \"user\" WHERE ID = ?", id);
    }

    @Override
    public List<Film> getSimilarUsers(int userId) {

        log.info("Method: getSimilarUsers; User ID: {}", userId);

        String createTableQuery = "CREATE TEMPORARY TABLE common_movies AS (\n" +
                "    SELECT u1.id AS id1, u2.id AS id2, COUNT(DISTINCT l1.film_id) AS common_movies\n" +
                "    FROM \"film_like\" l1\n" +
                "    JOIN \"film_like\" l2 ON l1.film_id = l2.film_id AND l1.user_id <> l2.user_id\n" +
                "    JOIN \"user\" u1 ON l1.user_id = u1.id\n" +
                "    JOIN \"user\" u2 ON l2.user_id = u2.id\n" +
                "    WHERE u1.id = ?\n" +
                "    GROUP BY u1.id, u2.id\n" +
                ")";

        String selectQuery = "SELECT u.ID " +
                "FROM common_movies \n" +
                "INNER JOIN \"user\" u ON common_movies.id2 = u.ID\n" +
                "WHERE common_movies.common_movies = (\n" +
                "    SELECT MAX(t2.common_movies)\n" +
                "    FROM common_movies t2\n" +
                ")";

        String dropTableQuery = "DROP TABLE IF EXISTS common_movies";

        jdbcTemplate.execute(dropTableQuery);
        jdbcTemplate.update(createTableQuery, userId);
        List<Integer> similarUsers = jdbcTemplate.queryForList(selectQuery, Integer.class);
        jdbcTemplate.execute(dropTableQuery);

        log.info("Method: getSimilarUsers; List of users: {}", similarUsers);

        List<Film> likedMovies = filmDAO.getLikedFilms(userId);

        Map<Film, Integer> recommendations = new HashMap<>();
        for (Integer similarUser : similarUsers) {
            List<Film> similarUserLikedMovies = filmDAO.getLikedFilms(similarUser);
            for (Film movie : similarUserLikedMovies) {
                if (!likedMovies.contains(movie)) {
                    int count = recommendations.getOrDefault(movie, 0);
                    recommendations.put(movie, count + 1);
                }
            }
        }

        return recommendations.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public List<Feed> getUserFeed(int userId) {
        return feedDAO.getUserFeed(userId);
    }
}
