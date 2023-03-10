package ru.yandex.practicum.filmorate.util.dao;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.Genre;
import ru.yandex.practicum.filmorate.models.Rating;
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@SpringBootTest
@AutoConfigureTestDatabase
public class FilmStorageTest {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    static List<Film> filmList;
    static List<User> users;

    @Autowired
    public FilmStorageTest(@Qualifier("filmDAO") FilmStorage filmStorage, @Qualifier("userDAO") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    @BeforeAll
    static void init() {
        filmList = new ArrayList<>();
        Film film = new Film();
        film.setName("Film1");
        film.setDescription("testDescription1");
        film.setReleaseDate(LocalDate.of(2022, 1, 1));
        film.setDuration(120);
        film.setMpa(new Rating(1, null));
        film.setGenres(List.of(new Genre(1)));
        filmList.add(film);

        film = new Film();
        film.setName("Film2");
        film.setDescription("testDescription2");
        film.setReleaseDate(LocalDate.of(2021, 1, 1));
        film.setDuration(100);
        film.setMpa(new Rating(2, null));
        film.setGenres(List.of(new Genre(2)));
        filmList.add(film);

        users = new ArrayList<>();
        User user = new User();
        user.setName("Test");
        user.setLogin("logTest");
        user.setEmail("test@test.ru");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        users.add(user);

        user = new User();
        user.setName("test1");
        user.setLogin("logTest1");
        user.setEmail("test1@test.ru");
        user.setBirthday(LocalDate.of(2000, 1, 2));
        users.add(user);
    }

    @Test
    void implementedMethodTest() {
        userStorage.createUser(users.get(0));
        userStorage.createUser(users.get(1));
        //createFilm() and getFilmById()
        Film filmWithId1 = filmList.get(0);
        filmStorage.createFilm(filmList.get(0));
        assertEquals(filmWithId1, filmStorage.getFilmById(1).orElse(new Film()));


        Film filmWithId2 = filmList.get(1);
        filmStorage.createFilm(filmList.get(1));
        assertEquals(filmWithId2, filmStorage.getFilmById(2).orElse(new Film()));

        //getAll()
        assertIterableEquals(filmList, filmStorage.getAll());

        //updateFilm()
        filmWithId1.setDescription("testUpdateDescription");
        filmWithId1.setName("testUpdateName");
        filmStorage.updateFilm(filmWithId1);

        Film updatedFilm = filmStorage.getFilmById(1).orElse(new Film());
        assertEquals(filmWithId1.getName(), updatedFilm.getName());
        assertEquals(filmWithId1.getDescription(), updatedFilm.getDescription());

        //likeFilm() and getMostPopularFilms()
        filmStorage.likeFilm(1, 2);
        filmStorage.likeFilm(2, 2);
        filmStorage.likeFilm(1, 1);
        List<Film> mostPopular = filmStorage.getMostPopularFilms();
        assertEquals(filmWithId2, mostPopular.get(0));

        //removeLike() and getMostPopularFilms(1)
        filmStorage.removeLike(1, 2);
        filmStorage.removeLike(2, 2);
        mostPopular = filmStorage.getMostPopularFilms();
        assertEquals(filmWithId2, mostPopular.get(1));
        mostPopular = filmStorage.getMostPopularFilms(1);
        assertEquals(1, mostPopular.size());

        //removeFilm()
        filmStorage.removeFilm(1);
        filmStorage.removeFilm(2);
        List<Film> films = filmStorage.getAll();
        assertEquals(0, films.size());
    }

}
