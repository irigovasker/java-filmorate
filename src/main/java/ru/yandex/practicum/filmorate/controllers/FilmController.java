package ru.yandex.practicum.filmorate.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@Slf4j
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

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
        Film updatedFilm = filmService.updateFilm(film);
        log.info("Обновлен фильм: " + film);
        return updatedFilm;
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<List<String>> likeFilm(@PathVariable int id, @PathVariable int userId) {
        if (filmService.likeFilm(id, userId)) {
            return new ResponseEntity<>(List.of("OK"), HttpStatus.OK);
        } else {
            throw new RuntimeException("Неизвестная ошибка");
        }
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<List<String>> removeLike(@PathVariable int id, @PathVariable int userId) {
        if (filmService.removeLike(id, userId)) {
            return new ResponseEntity<>(List.of("OK"), HttpStatus.OK);
        } else {
            throw new RuntimeException("Неизвестная ошибка");
        }
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilm(@RequestParam(name = "count") Optional<Integer> count) {
        if (count.isPresent()) {
            return filmService.getMostPopularFilms(count.get());
        } else {
            return filmService.getMostPopularFilms();
        }
    }
}
