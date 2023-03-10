package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.models.User;
import ru.yandex.practicum.filmorate.util.CustomValidateException;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users;
    private int counter;

    public InMemoryUserStorage() {
        this.users = new HashMap<>();
        counter = 1;
    }


    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User createUser(User user) {
        user.setId(counter);
        if (user.getName().equals("")) {
            user.setName(user.getLogin());
        }
        user.setFriends(new HashSet<>());
        users.put(counter, user);
        return users.get(counter++);
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
    public List<User> getUserFriends(int userId) {
        List<User> friends = new ArrayList<>();
        for (Integer integer : users.get(userId).getFriends()) {
            friends.add(users.get(integer));
        }
        return friends;
    }

    @Override
    public void removeUser(int id) {
        users.remove(id);
    }

    @Override
    public Optional<User> getUserById(int userId) {
        if (users.containsKey(userId)) {
            return Optional.of(users.get(userId));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<User> getSubscribers(int userId) {
        return null;
    }

    @Override
    public Relation getRelation(int userId, int secondUserId) {
        return null;
    }

    @Override
    public void addRelation(int userId, int secondUserId) {

    }

    @Override
    public void changeRelationStatus(Relation relation, int statusId) {

    }

    @Override
    public void removeRelation(Relation relation) {

    }
}
