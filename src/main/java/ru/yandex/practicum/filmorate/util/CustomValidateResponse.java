package ru.yandex.practicum.filmorate.util;

public class CustomValidateResponse {
    private String message;

    public CustomValidateResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
