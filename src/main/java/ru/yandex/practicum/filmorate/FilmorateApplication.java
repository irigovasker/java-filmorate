package ru.yandex.practicum.filmorate;
//Привет. На счет Relation я реализовал так еще до групового проекта что бы была возможность иметь 3 статуса дружбы. DataAccess это исключение спринга поэтому его нет в проекте

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FilmorateApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilmorateApplication.class, args);
    }

}
