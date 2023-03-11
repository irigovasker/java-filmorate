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
        List<User> list = new ArrayList<>();
        users.get(userId).getSubscribers().forEach(integer -> list.add(users.get(integer)));
        return list;
    }

    @Override
    public Relation getRelation(int userId, int secondUserId) {
        Set<Integer> firstUserFriends = users.get(userId).getFriends();
        Set<Integer> firstUserSubs = users.get(userId).getSubscribers();
        Set<Integer> secondUserFriends = users.get(secondUserId).getFriends();
        Set<Integer> secondUserSubs = users.get(secondUserId).getSubscribers();
        Relation relation = new Relation();
        relation.setSecondUser(userId);
        relation.setSecondUser(secondUserId);

        if (firstUserFriends.contains(secondUserId) && secondUserSubs.contains(userId)) {
            relation.setSecondUser(1);
            return relation;
        } else if (firstUserSubs.contains(secondUserId) && secondUserFriends.contains(userId)) {
            relation.setStatus(2);
            return relation;
        } else if (firstUserFriends.contains(secondUserId) && secondUserFriends.contains(userId)) {
            relation.setStatus(3);
            return relation;
        } else {
            return null;
        }
    }

    @Override
    public void addRelation(int userId, int secondUserId) {
        users.get(userId).getFriends().add(secondUserId);
        users.get(secondUserId).getSubscribers().add(userId);
    }

    @Override
    public void changeRelationStatus(Relation relation, int statusId) {
        User firstUser = users.get(relation.getFirstUser());
        User secondUser = users.get(relation.getSecondUser());
        if (statusId == 1) {
            firstUser.getFriends().add(secondUser.getId());
            firstUser.getSubscribers().remove(secondUser.getId());
            secondUser.getFriends().remove(firstUser.getId());
            secondUser.getSubscribers().add(firstUser.getId());
        } else if (statusId == 2) {
            firstUser.getFriends().remove(secondUser.getId());
            firstUser.getSubscribers().add(secondUser.getId());
            secondUser.getFriends().add(firstUser.getId());
            secondUser.getSubscribers().remove(firstUser.getId());
        } else if (statusId == 3) {
            firstUser.getFriends().add(secondUser.getId());
            firstUser.getSubscribers().remove(secondUser.getId());
            secondUser.getFriends().add(firstUser.getId());
            secondUser.getSubscribers().remove(firstUser.getId());
        }
    }

    @Override
    public void removeRelation(Relation relation) {
        User firstUser = users.get(relation.getFirstUser());
        User secondUser = users.get(relation.getSecondUser());
        firstUser.getFriends().remove(secondUser.getId());
        firstUser.getSubscribers().remove(secondUser.getId());
        secondUser.getFriends().remove(firstUser.getId());
        secondUser.getSubscribers().remove(firstUser.getId());
    }
}
