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
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmDAO;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import javax.sql.DataSource;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class UserDAO implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final FilmStorage filmStorage;
    private final FeedStorage feedStorage;
    private static final String SELECT_USER = "SELECT u.id, u.email, u.login, u.name, u.birthday ";

    @Autowired
    public UserDAO(JdbcTemplate jdbcTemplate, DataSource dataSource, FilmStorage filmStorage, FeedStorage feedStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmStorage = filmStorage;
        this.feedStorage = feedStorage;
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
                        "FROM \"user\" AS u INNER JOIN \"friends\" AS f ON u.id = f.friend_id WHERE f.user_id = ?",
                new BeanPropertyRowMapper<>(User.class), userId
        );
    }

    @Override
    public void addFriend(int userId, int friendId) {
        String sqlQuery = "INSERT INTO \"friends\" (USER_ID, FRIEND_ID) " +
                "VALUES (?, ?)";
        jdbcTemplate.update(sqlQuery,
                friendId,
                userId);
        feedStorage.writeFeed(friendId, "FRIEND", "ADD", userId);
    }

    @Override
    public List<User> getCommonFriend(int userId, int friendId) {
        String sql = "SELECT * FROM \"friends\" AS f1 INNER JOIN \"friends\" AS f2 ON f1.friend_id = f2.friend_id " +
                "INNER JOIN \"user\" AS u ON f2.friend_id = u.id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class), userId, friendId);
    }

    @Override
    public boolean removeFriend(int userId, int friendId) {
        feedStorage.writeFeed(userId, "FRIEND", "REMOVE", friendId);
        String sqlQueryDelete = "DELETE FROM \"friends\" WHERE user_id = ? AND friend_id = ?";
        return jdbcTemplate.update(sqlQueryDelete,
                userId,
                friendId) == 1;
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

        List<Film> likedMovies = filmStorage.getLikedFilms(userId);

        Map<Film, Integer> recommendations = new HashMap<>();
        for (Integer similarUser : similarUsers) {
            List<Film> similarUserLikedMovies = filmStorage.getLikedFilms(similarUser);
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
        return feedStorage.getUserFeed(userId);
    }
}
