package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.util.CustomValidateException;

import java.util.*;

@Component
@RequiredArgsConstructor()
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users;
    private int counter;


    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User createUser(User user) {
        user.setId(++counter);
        if (user.getName() == "") {
            user.setName(user.getLogin());
        }
        user.setFriends(new HashSet<>());
        users.put(counter, user);
        return users.get(counter);
    }

    @Override
    public User updateUser(User user) {
        if (users.containsKey(user.getId())) {
            if (user.getFriends() == null) {
                user.setFriends(users.get(user.getId()).getFriends());
            }
            users.put(user.getId(), user);
            return users.get(user.getId());
        } else {
            throw new CustomValidateException("Такого объекта нет");
        }
    }

    @Override
    public List<User> getUserFriend(int id) {
        List<User> friends = new ArrayList<>();
        for (Integer integer : users.get(id).getFriends()) {
            friends.add(users.get(integer));
        }
        return friends;
    }

    @Override
    public void removeUser(int id) {
        users.remove(id);
    }

    @Override
    public Optional<User> getUserById(int id) {
        if (users.containsKey(id)) {
            return Optional.of(users.get(id));
        } else {
            return Optional.empty();
        }
    }
}
