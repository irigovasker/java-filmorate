package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.relational.core.sql.In;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.models.Film;
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.storage.user.Relation;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.util.ObjectNotFoundException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDAO") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

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

    public void addFriend(int userId, int possibleFriend) {
        getUserById(userId);
        getUserById(possibleFriend);

        Relation relation = userStorage.getRelation(userId, possibleFriend);
        if (relation == null) {
            userStorage.addRelation(userId, possibleFriend);
            return;
        }

        boolean relationNotReversed = userId == relation.getFirstUser();
        switch (relation.getStatus()) {
            case 1:
                if (!relationNotReversed) {
                    userStorage.changeRelationStatus(relation, 3);
                }
                break;
            case 2:
                if (relationNotReversed) {
                    userStorage.changeRelationStatus(relation, 3);
                }
                break;
            case 3:
                return;
            default:
                throw new RuntimeException();
        }
    }

    public void removeFriend(int userId, int friendToDelete) {
        getUserById(userId);
        getUserById(friendToDelete);


        Relation relation = userStorage.getRelation(userId, friendToDelete);
        if (relation == null) {
            throw new ObjectNotFoundException("Пользователи не друзья");
        }

        boolean relationNotReversed = userId == relation.getFirstUser();
        switch (relation.getStatus()) {
            case 1:
                if (relationNotReversed) {
                    userStorage.removeRelation(relation);
                }
                break;
            case 2:
                if (!relationNotReversed) {
                    userStorage.removeRelation(relation);
                    return;
                }
                break;
            case 3:
                if (relationNotReversed) {
                    userStorage.changeRelationStatus(relation, 2);
                } else {
                    userStorage.changeRelationStatus(relation, 1);
                }
            default:
                throw new RuntimeException();
        }
    }

    public List<User> getCommonFriend(int userId, int secondUserId) {
        User firstUser = getUserById(userId);
        User secondUser = getUserById(secondUserId);

        if (firstUser == null || secondUser == null) {
            throw new ObjectNotFoundException("Несуществующий пользователь");
        }

        Set<User> usersFriend = new HashSet<>(userStorage.getUserFriends(userId));
        Set<User> secondUsersFriends = new HashSet<>(userStorage.getUserFriends(secondUserId));

        List<User> result = new ArrayList<>();
        usersFriend.forEach(user -> {
            if (secondUsersFriends.contains(user)) {
                result.add(user);
            }
        });
        return result;
    }

    public List<User> getUserFriends(int id) {
        return userStorage.getUserFriends(id);
    }

    public List<Film> getSimilarUsers(int id) {

        log.info("Method: getSimilarUsers; User ID: {}", id);

        return userStorage.getSimilarUsers(id);
    }
}
