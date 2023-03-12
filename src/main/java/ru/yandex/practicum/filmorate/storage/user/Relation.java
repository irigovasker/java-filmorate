package ru.yandex.practicum.filmorate.storage.user;

import lombok.Data;

@Data
public class Relation {
    private int firstUser;
    private int secondUser;
    private int status;
}
