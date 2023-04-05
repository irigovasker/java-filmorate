package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.models.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.util.ObjectNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;


    public List<Genre> getAllGenres() {
        return genreStorage.getAllGenres();
    }

    public Genre getGenreById(int id) {
        Genre genre = genreStorage.getGenreById(id);
        if (genre == null) {
            throw new ObjectNotFoundException("Несуществующий жанр");
        }
        return genre;
    }
}
