package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.models.Feed;

import java.util.List;

public interface FeedStorage {
    void writeFeed(int userId, String eventType, String operation, Integer entityId);

    List<Feed> getUserFeed(int userId);
}
