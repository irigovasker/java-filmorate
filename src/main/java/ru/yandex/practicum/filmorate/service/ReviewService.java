package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.models.Review;
import ru.yandex.practicum.filmorate.storage.ReviewDAO;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewDAO reviewStorage;

    @Autowired
    public ReviewService(ReviewDAO reviewStorage) {
        this.reviewStorage = reviewStorage;
    }

    public Review create(Review review) {
        return reviewStorage.create(review);
    }

    public Review update(Review review) {
        return reviewStorage.update(review);
    }

    public Review getById(int id) {
        return reviewStorage.getById(id);
    }

    public List<Review> getByFilmId(int filmId, int count) {
        if (filmId == -1) {
            return reviewStorage.getReviews(count);
        }
        return reviewStorage.getByFilmId(filmId, count);
    }

    public void delete(int id) {
        reviewStorage.delete(id);
    }

    public void addLike(int reviewId, int userId) {
        reviewStorage.addReaction(reviewId, userId, true);
    }

    public void addDislike(int reviewId, int userId) {
        reviewStorage.addReaction(reviewId, userId, false);
    }

    public void removeReaction(int reviewId, int userId) {
        reviewStorage.deleteReaction(reviewId, userId);
    }


}
