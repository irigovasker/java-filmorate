package ru.yandex.practicum.filmorate.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Rating {
    private int id;
    private String name;

    public Rating() {
    }

    public Rating(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rating rating = (Rating) o;

        return id == rating.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
