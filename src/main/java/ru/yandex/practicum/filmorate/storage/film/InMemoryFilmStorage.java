package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.util.CustomValidateException;

import java.util.*;

@Component
@RequiredArgsConstructor
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films;
    private int counter;


    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film createFilm(Film film) {
        film.setId(++counter);
        film.setLikedUsers(new HashSet<>());
        films.put(counter, film);
        return films.get(counter);
    }

    @Override
    public Film updateFilm(Film film) {
        if (films.containsKey(film.getId())) {
            if (film.getLikedUsers() == null) {
                film.setLikedUsers(films.get(film.getId()).getLikedUsers());
            }
            films.put(film.getId(), film);
            return films.get(film.getId());
        } else {
            throw new CustomValidateException("Такого объекта нет");
        }
    }

    @Override
    public Optional<Film> getFilmById(int id) {
        if (films.containsKey(id)) {
            return Optional.of(films.get(id));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void removeFilm(int id) {
        films.remove(id);
    }
}
