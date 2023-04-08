package ru.yandex.practicum.filmorate.storage.rating;

import ru.yandex.practicum.filmorate.models.Rating;

import java.util.List;

public interface RatingStorage {
    List<Rating> getAllRatings();

    Rating getRatingById(int id);
}
