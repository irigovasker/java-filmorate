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
import ru.yandex.practicum.filmorate.storage.user.Relation;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@SpringBootTest
@AutoConfigureTestDatabase
public class StoragesTests {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    static List<Film> filmList;
    static List<User> users;

    @Autowired
    public StoragesTests(@Qualifier("filmDAO") FilmStorage filmStorage, @Qualifier("userDAO") UserStorage userStorage) {
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
        user.setName("testName");
        user.setLogin("testLogin");
        user.setEmail("test@Email.ru");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        users.add(user);

        user = new User();
        user.setName("testName1");
        user.setLogin("testLogin1");
        user.setLogin("testLogin1");
        user.setEmail("test1@Email.ru");
        user.setBirthday(LocalDate.of(2000, 1, 2));
        users.add(user);
    }

    @Test
    void implementedMethodTest() {
        //createUser() and getUserById() and getAll()
        User userWithId1 = users.get(0);
        User userWithId2 = users.get(1);
        userStorage.createUser(userWithId1);
        userStorage.createUser(userWithId2);

        User userFromDB = userStorage.getUserById(1).orElse(new User());
        User userFromDB2 = userStorage.getUserById(2).orElse(new User());
        assertEquals(userWithId1, userFromDB);
        assertEquals(userWithId2, userFromDB2);
        assertIterableEquals(users, userStorage.getAll());

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

        //getMostPopularFilms(1, 2022)
        mostPopular = filmStorage.getMostPopularFilms(1, 2022);
        assertEquals(filmWithId1, mostPopular.get(0));

        //getMostPopularFilms(1, 1, 2022)
        mostPopular = filmStorage.getMostPopularFilms(1, 2022);
        assertEquals(filmWithId1, mostPopular.get(0));
        assertEquals(1, mostPopular.size());

        //getMostPopularFilmsByGenre(1)
        mostPopular = filmStorage.getMostPopularFilmsByGenre(2);
        assertEquals(filmWithId2, mostPopular.get(0));

        //getMostPopularFilmsByGenre(1, 2)
        mostPopular = filmStorage.getMostPopularFilmsByGenre(1, 2);
        assertEquals(1, mostPopular.size());
        assertEquals(filmWithId2, mostPopular.get(0));

        //getMostPopularFilmsByYear(2022)
        mostPopular = filmStorage.getMostPopularFilmsByYear(2022);
        assertEquals(filmWithId1, mostPopular.get(0));

        //getMostPopularFilmsByYear(1, 2021)
        mostPopular = filmStorage.getMostPopularFilmsByYear(1, 2021);
        assertEquals(1, mostPopular.size());
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

        //updateUser()
        userWithId1.setName("updateName");
        userWithId1.setEmail("update@email.com");
        userStorage.updateUser(userWithId1);
        userFromDB = userStorage.getUserById(1).orElse(new User());
        assertEquals("updateName", userFromDB.getName());
        assertEquals("update@email.com", userFromDB.getEmail());

        //addRelation() and getUserFriends() and getSubscribers()
        userStorage.addRelation(1, 2);
        assertEquals(2, userStorage.getUserFriends(1).get(0).getId());
        assertEquals(1, userStorage.getSubscribers(2).get(0).getId());

        //changeRelationStatus() and getRelation()
        Relation relation = userStorage.getRelation(1, 2);
        userStorage.changeRelationStatus(relation, 3);
        assertEquals(1, userStorage.getUserFriends(2).get(0).getId());

        //removeRelation()
        userStorage.removeRelation(relation);
        assertEquals(0, userStorage.getUserFriends(1).size());
        assertEquals(0, userStorage.getUserFriends(2).size());

        //removeUser()
        userStorage.removeUser(1);
        userStorage.removeUser(2);
        assertEquals(0, userStorage.getAll().size());
    }
}
