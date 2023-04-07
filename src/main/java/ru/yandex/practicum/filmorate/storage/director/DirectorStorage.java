package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.models.Director;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
    List<Director> findAll();

    Optional<Director> findOneById(int id);

    Director createDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirectorById(int id);
}
