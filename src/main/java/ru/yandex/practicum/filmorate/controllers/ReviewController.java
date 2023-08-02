package ru.yandex.practicum.filmorate.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.models.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Review createReview(@Valid @RequestBody Review review) {
        return reviewService.create(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        return reviewService.update(review);
    }

    @GetMapping("/{id}")
    public Review getReview(@PathVariable int id) {
        return reviewService.getById(id);
    }

    @GetMapping
    public List<Review> getReviewsByFilmId(@RequestParam(name = "filmId", defaultValue = "-1") Integer filmId,
                                           @RequestParam(name = "count", defaultValue = "10") Integer count) {
        return reviewService.getByFilmId(filmId, count);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable int id) {
        reviewService.delete(id);
    }

    @PutMapping("{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.addLike(id, userId);
    }

    @PutMapping("{id}/dislike/{userId}")
    public void addDislike(@PathVariable int id, @PathVariable int userId) {
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.removeReaction(id, userId);
    }

    @DeleteMapping("{id}/dislike/{userId}")
    public void removeDislike(@PathVariable int id, @PathVariable int userId) {
        reviewService.removeReaction(id, userId);
    }

}
