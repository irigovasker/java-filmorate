package ru.yandex.practicum.filmorate.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

@Slf4j
public class ErrorsUtil {
    public static void returnErrorsToClient(BindingResult bindingResult) {
        StringBuilder errorMsg = new StringBuilder();

        for (FieldError error : bindingResult.getFieldErrors()) {
            errorMsg.append(error.getField())
                    .append(" - ").append(error.getDefaultMessage() == null ? error.getCode() : error.getDefaultMessage())
                    .append("\n");
        }
        log.warn("Был передан некорректный объект: " + errorMsg);
        throw new CustomValidateException(errorMsg.toString());
    }
}
