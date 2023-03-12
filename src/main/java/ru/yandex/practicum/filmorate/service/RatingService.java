package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.models.Rating;
import ru.yandex.practicum.filmorate.storage.RatingDAO;
import ru.yandex.practicum.filmorate.util.ObjectNotFoundException;

import java.util.List;

@Service
public class RatingService {
    private final RatingDAO ratingDAO;

    @Autowired
    public RatingService(RatingDAO ratingDAO) {
        this.ratingDAO = ratingDAO;
    }

    public List<Rating> getAllRatings() {
        return ratingDAO.getAllRatings();
    }

    public Rating getRatingById(int id) {
        Rating rating = ratingDAO.getRatingById(id);
        if (rating == null) {
            throw new ObjectNotFoundException("Несуществующий рейтинг");
        }
        return rating;
    }
}
