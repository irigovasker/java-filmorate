package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.models.Feed;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.util.ObjectNotFoundException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User getUserById(int id) {
        return userStorage.getUserById(id).orElseThrow(() -> new ObjectNotFoundException("Несуществующий пользователь"));
    }

    public User createUser(User user) {
        if (user.getName().equals("")) {
            user.setName(user.getLogin());
        }
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        if (userStorage.getUserById(user.getId()).isEmpty()) {
            throw new ObjectNotFoundException("Несуществущий пользователь");
        }
        return userStorage.updateUser(user);
    }

    public void addFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);
        userStorage.addFriend(friendId, userId);
    }

    public void removeFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);
        if (!userStorage.removeFriend(userId, friendId)) {
            throw new ObjectNotFoundException("Пользователи не были друзьями.");
        }
    }

    public List<User> getCommonFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);
        return userStorage.getCommonFriend(userId, friendId);
    }

    public List<User> getUserFriends(int id) {
        getUserById(id);
        return userStorage.getUserFriends(id);
    }

    public List<Film> getSimilarUsers(int id) {

        log.info("Method: getSimilarUsers; User ID: {}", id);

        return userStorage.getSimilarUsers(id);
    }

    public void deleteUserById(int id) {
        getUserById(id);
        userStorage.removeUser(id);
    }

    public List<Feed> getUserFeed(int id) {
        getUserById(id);
        return userStorage.getUserFeed(id);
    }
}
