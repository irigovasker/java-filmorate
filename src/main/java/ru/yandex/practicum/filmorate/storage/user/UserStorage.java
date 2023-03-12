package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.models.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    List<User> getAll();
    User createUser(User user);
    User updateUser(User user);
    Optional<User> getUserById(int userId);
    List<User> getUserFriends(int userId);
    List<User> getSubscribers(int userId);
    Relation getRelation(int userId, int secondUserId);
    void addRelation(int userId, int secondUserId);
    void changeRelationStatus(Relation relation, int statusId);
    void removeRelation(Relation relation);
    void removeUser(int id);
}
