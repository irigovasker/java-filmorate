package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.Review;
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.util.ObjectNotFoundException;
import javax.sql.DataSource;
import java.util.*;

@Component
public class ReviewDAO {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final FeedDAO feedDAO;

    @Autowired
    public ReviewDAO(JdbcTemplate jdbcTemplate, DataSource dataSource, FeedDAO feedDAO) {
        this.jdbcTemplate = jdbcTemplate;
        this.feedDAO = feedDAO;
        simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("\"review\"")
                .usingGeneratedKeyColumns("id")
                .usingColumns("content", "is_positive", "user_id", "film_id", "useful");
    }

    public Review create(Review review) {
        if (!isUser(review.getUserId()) || !isFilm(review.getFilmId())) {
            throw new ObjectNotFoundException("Невозможно создать отзыв");
        } else if (review.getIsPositive() == null || review.getUserId() == null || review.getFilmId() == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        } else {
            Map<String, Object> parameters = new HashMap<>(5);
            parameters.put("content", review.getContent());
            parameters.put("is_positive", review.getIsPositive());
            parameters.put("user_id", review.getUserId());
            parameters.put("film_id", review.getFilmId());
            parameters.put("useful", review.getUseful());

            review.setId(simpleJdbcInsert.executeAndReturnKey(parameters).intValue());
            Integer useful = getReactions(review.getId());
            if (useful == null) {
                useful = 0;
            }
            setUseful(useful, review.getId());
            feedDAO.writeFeed(review.getUserId(), "REVIEW", "ADD", review.getId());
            return review;
        }
    }

    public Review update(Review review) {
        jdbcTemplate.update("UPDATE \"review\" SET CONTENT = ?, IS_POSITIVE = ? WHERE ID = ?",
                review.getContent(), review.getIsPositive(), review.getId()
        );
        Review review1 = getById(review.getId());
        feedDAO.writeFeed(review1.getUserId(), "REVIEW", "UPDATE", review.getId());
        return review1;
    }

    public void delete(int id) {
        Review review = getById(id);
        feedDAO.writeFeed(review.getUserId(), "REVIEW", "REMOVE", review.getId());
        jdbcTemplate.update("DELETE FROM \"review\" WHERE ID = ?", id);
    }

    public Review getById(int id) {
        return jdbcTemplate.query("SELECT * FROM \"review\" WHERE ID = ?",
                        new BeanPropertyRowMapper<>(Review.class), id)
                .stream().findAny().orElseThrow(() -> new ObjectNotFoundException("Несуществующий отзыв"));
    }

    public List<Review> getByFilmId(int filmId, int count) {
        return jdbcTemplate.query("SELECT * FROM \"review\" WHERE FILM_ID = ? ORDER BY USEFUL DESC LIMIT ?",
                new BeanPropertyRowMapper<>(Review.class), filmId, count);
    }

    public List<Review> getReviews(int count) {
        return jdbcTemplate.query("SELECT * FROM \"review\" ORDER BY USEFUL DESC LIMIT ?",
                new BeanPropertyRowMapper<>(Review.class), count);
    }

    public void addReaction(int reviewId, int userId, boolean isLike) {
        String sql = "INSERT INTO \"review_like\" (REVIEW_ID, USER_ID, USEFUL) VALUES (?, ?, ?)";
        int useful = 0;
        if (isLike) {
            useful += 1;
        } else {
            useful -= 1;
        }
        jdbcTemplate.update(sql, reviewId, userId, useful);
        setUseful(getReactions(reviewId), reviewId);
    }

    public void deleteReaction(int reviewId, int userId) {
        jdbcTemplate.update("DELETE FROM \"review_like\" WHERE REVIEW_ID = ? AND USER_ID = ?", reviewId, userId);
        setUseful(getReactions(reviewId), reviewId);
    }

    public Integer getReactions(int reviewId) {
        return jdbcTemplate.queryForObject("SELECT SUM(useful) FROM \"review_like\" WHERE REVIEW_ID = ?",
                Integer.class, reviewId);
    }

    public void setUseful(Integer useful, int reviewId) {
        jdbcTemplate.update("UPDATE \"review\" SET USEFUL = ? WHERE ID = ?", useful, reviewId);
    }

    public boolean isUser(int userId) {
           Optional<User> user = jdbcTemplate.query(
                    "SELECT * FROM \"user\" WHERE ID = ?",
                           new BeanPropertyRowMapper<>(User.class), userId)
                    .stream().findAny();
        return user.isPresent();
    }

    public boolean isFilm(int filmId) {
           Optional<Film> film = jdbcTemplate.query(
                    "SELECT * FROM \"film\" AS f " +
                            "LEFT JOIN \"rating\" r on f.RATING_ID = r.ID " +
                            "WHERE f.ID = ?", new BeanPropertyRowMapper<>(Film.class), filmId).stream().findAny();
        return film.isPresent();
    }
}
