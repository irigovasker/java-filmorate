package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.models.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.util.ObjectNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;


    public List<Director> getAllDirectors() {
        return directorStorage.findAll();
    }

    public Director getDirectorById(int id) {
        return directorStorage.findOneById(id).orElseThrow(() -> new ObjectNotFoundException("Несуществующий режиссер"));
    }

    public Director createDirector(Director director) {
        return directorStorage.createDirector(director);
    }

    public Director updateDirector(Director director) {
        if (directorStorage.findOneById(director.getId()).isEmpty()) {
            throw new ObjectNotFoundException("Несуществующий режиссер");
        }
        return directorStorage.updateDirector(director);
    }

    public void deleteDirectorById(int id) {
        directorStorage.deleteDirectorById(id);
    }
}
