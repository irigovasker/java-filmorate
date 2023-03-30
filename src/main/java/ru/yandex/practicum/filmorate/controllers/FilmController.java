package ru.yandex.practicum.filmorate.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.util.ErrorsUtil;
import ru.yandex.practicum.filmorate.util.Validator;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Slf4j
public class FilmController {
    private final FilmService filmService;


    @GetMapping
    public List<Film> getFilms() {
        return filmService.getAll();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable int id) {
        return filmService.getFilmById(id);
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film, BindingResult bindingResult) {
        Validator.validateFilm(film);
        if (bindingResult.hasErrors()) {
            ErrorsUtil.returnErrorsToClient(bindingResult);
        }
        log.info("Добавлен фильм: " + film);
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film, BindingResult bindingResult) {
        Validator.validateFilm(film);
        if (bindingResult.hasErrors()) {
            ErrorsUtil.returnErrorsToClient(bindingResult);
        }
        log.info("Обновлен фильм: " + film);
        return filmService.updateFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeFilm(@PathVariable int id, @PathVariable int userId) {
        filmService.likeFilm(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilm(@RequestParam(name = "count") Optional<Integer> count) {
        if (count.isPresent()) {
            return filmService.getMostPopularFilms(count.get());
        } else {
            return filmService.getMostPopularFilms();
        }
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam(name = "userId") int userId,
                                     @RequestParam(name = "friendId") int friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/director/{id}")
    public List<Film> getDirectorsFilms(@PathVariable int id, @RequestParam(name = "sortBy") String sortBy) {
        if (sortBy.equals("year")) {
            return filmService.getDirectorsFilmsSortByYear(id);
        } else if (sortBy.equals("likes")) {
            return filmService.getDirectorsFilmsSortByLikes(id);
        } else {
            throw new RuntimeException();
        }
    }
}
