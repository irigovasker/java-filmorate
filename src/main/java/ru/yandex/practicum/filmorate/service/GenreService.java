package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.models.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDAO;
import ru.yandex.practicum.filmorate.util.ObjectNotFoundException;

import java.util.List;

@Service
public class GenreService {
    private final GenreDAO genreDAO;

    @Autowired
    public GenreService(GenreDAO genreDAO) {
        this.genreDAO = genreDAO;
    }

    public List<Genre> getAllGenres() {
        return genreDAO.getAllGenres();
    }

    public Genre getGenreById(int id) {
        Genre genre = genreDAO.getGenreById(id);
        if (genre == null) {
            throw new ObjectNotFoundException("Несуществующий жанр");
        }
        return genre;
    }
}
