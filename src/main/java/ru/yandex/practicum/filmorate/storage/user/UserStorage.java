package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.models.Feed;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    List<User> getAll();

    User createUser(User user);

    User updateUser(User user);

    Optional<User> getUserById(int userId);

    List<User> getUserFriends(int userId);

    //List<User> getSubscribers(int userId);

    //Relation getRelation(int userId, int secondUserId);

    void addFriend(int userId, int friendId);

    boolean removeFriend(int userId, int friendId);

    List<User> getCommonFriend(int userId, int friendId);

    void removeUser(int id);

    List<Film> getSimilarUsers(int userId);

    List<Feed> getUserFeed(int userId);
}
