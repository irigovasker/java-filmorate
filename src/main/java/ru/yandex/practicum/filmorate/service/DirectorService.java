package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.models.Director;
import ru.yandex.practicum.filmorate.storage.DirectorDAO;
import ru.yandex.practicum.filmorate.util.ObjectNotFoundException;

import java.util.List;

@Service
public class DirectorService {
    private final DirectorDAO directorDAO;

    @Autowired
    public DirectorService(DirectorDAO directorDAO) {
        this.directorDAO = directorDAO;
    }

    public List<Director> getAllDirectors() {
        return directorDAO.findAll();
    }

    public Director getDirectorById(int id) {
        return directorDAO.findOneById(id).orElseThrow(() -> new ObjectNotFoundException("Несуществующий режиссер"));
    }

    public Director createDirector(Director director) {
        return directorDAO.createDirector(director);
    }

    public Director updateDirector(Director director) {
        if (directorDAO.findOneById(director.getId()).isEmpty()) {
            throw new ObjectNotFoundException("Несуществующий режиссер");
        }
        return directorDAO.updateDirector(director);
    }

    public void deleteDirectorById(int id) {
        directorDAO.deleteDirectorById(id);
    }
}
