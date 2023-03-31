package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.Review;
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.storage.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.util.CustomValidateException;
import ru.yandex.practicum.filmorate.util.ObjectNotFoundException;

import javax.sql.DataSource;
import java.net.http.HttpTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ReviewDAO {


    /*

     апдейт ревью, не должен меняться юзер/фильм айди
     ревью для фильма по айди не ищутся



     */
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    @Autowired
    public ReviewDAO(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("\"review\"")
                .usingGeneratedKeyColumns("id")
                .usingColumns("content", "is_positive", "user_id", "film_id", "useful");
    }

    public Review create(Review review) {
        if (!isUser(review.getUserId()) || !isFilm(review.getFilmId()) ) {
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
            return review;

        }

    }

    public Review update(Review review) {
        jdbcTemplate.update("UPDATE \"review\" SET CONTENT = ?, IS_POSITIVE = ?, USEFUL = ? WHERE ID = ?",
                review.getContent(), review.getIsPositive(), review.getUseful(), review.getId()
        );
        return getById(review.getId());
    }

    public void delete(int id) {
        jdbcTemplate.update("DELETE FROM \"review\" WHERE ID = ?", id);
    }

    public Review getById(int id) {
        return jdbcTemplate.query("SELECT * FROM \"review\" WHERE ID = ?",
                        new BeanPropertyRowMapper<>(Review.class), id)
                .stream().findAny().orElseThrow(() -> new ObjectNotFoundException("Несуществующий отзыв"));
    }

    public List<Review> getByFilmId(int filmId, int count) {
        return jdbcTemplate.query("SELECT * FROM \"review\" WHERE FILM_ID = ? LIMIT ?",
                new BeanPropertyRowMapper<>(Review.class), filmId, count);
    }

    public List<Review> getReviews(int count) {
        return jdbcTemplate.query("SELECT * FROM \"review\" LIMIT ?",
                new BeanPropertyRowMapper<>(Review.class), count);
    }

    public boolean isUser(int userId) {
           Optional<User> user = jdbcTemplate.query(
                    "SELECT * FROM \"user\" WHERE id = ?"
                    , new BeanPropertyRowMapper<>(User.class), userId)
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
