package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.util.ObjectNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User getUserById(int id) {
        User user = userStorage.getUserById(id).orElse(null);
        if (user == null) {
            throw new ObjectNotFoundException("Пользователь не найден");
        }
        return user;
    }

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public boolean addFriend(int userId, int possibleFriend) {
        User user = getUserById(userId);
        User possible = getUserById(possibleFriend);

        if (user != null && possible != null) {
            if (user.getFriends().contains(possibleFriend)) {
                return true;
            } else {
                possible.getFriends().add(userId);
                return user.getFriends().add(possibleFriend);
            }
        } else {
            throw new ObjectNotFoundException("Несуществующий пользователь");
        }
    }

    public boolean removeFriend(int userId, int friendToDelete) {
        User user = getUserById(userId);
        User userToDelete = getUserById(friendToDelete);

        if (user != null && userToDelete != null) {
            if (user.getFriends().contains(friendToDelete)) {
                user.getFriends().remove(friendToDelete);
                return userToDelete.getFriends().remove(userId);
            } else {
                throw new RuntimeException("Пользователи не друзья");
            }
        } else {
            throw new ObjectNotFoundException("Несуществующий пользователь");
        }
    }

    public List<User> getCommonFriend(int userId, int secondUserId) {
        User user = getUserById(userId);
        User secondUser = getUserById(secondUserId);

        if (user == null || secondUser == null) {
            throw new ObjectNotFoundException("Несуществующий пользователь");
        }

        Set<Integer> usersFriend = user.getFriends();
        Set<Integer> secondUsersFriend = secondUser.getFriends();

        List<User> result = new ArrayList<>();

        for (Integer integer : usersFriend) {
            if (secondUsersFriend.contains(integer)) {
                result.add(getUserById(integer));
            }
        }
        return result;
    }

    public List<User> getUserFriends(int id) {
        return userStorage.getUserFriend(id);
    }
}
