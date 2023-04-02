package ru.yandex.practicum.filmorate.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Feed {
    private Integer eventId;
    private Long timestamp;
    private Integer userId;
    private String eventType;
    private String operation;
    private Integer entityId;
}
