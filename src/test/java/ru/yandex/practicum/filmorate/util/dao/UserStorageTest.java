package ru.yandex.practicum.filmorate.util.dao;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.storage.user.Relation;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@SpringBootTest
@AutoConfigureTestDatabase
public class UserStorageTest {
    private final UserStorage userStorage;
    static List<User> users;

    @Autowired
    public UserStorageTest(@Qualifier("userDAO") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @BeforeAll
    static void init() {
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
    void testUserDao() {
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
