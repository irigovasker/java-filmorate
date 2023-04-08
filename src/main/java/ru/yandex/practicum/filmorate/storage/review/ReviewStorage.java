package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.models.Review;

import java.util.List;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    void delete(int id);

    Review getById(int id);

    List<Review> getByFilmId(int filmId, int count);

    List<Review> getReviews(int count);

    void addReaction(int reviewId, int userId, boolean isLike);

    void deleteReaction(int reviewId, int userId);

    Integer getReactions(int reviewId);

    void setUseful(Integer useful, int reviewId);

    boolean isUser(int userId);

    boolean isFilm(int filmId);
}
