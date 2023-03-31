package ru.yandex.practicum.filmorate.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.client.HttpClientErrorException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Getter
@Setter
public class Review {

    @JsonProperty("reviewId")
    private int id;
    @NotBlank
    private String content;
    @JsonProperty("isPositive")
    @NotNull
    private Boolean isPositive;
    @NotNull
    private Integer userId;
    @NotNull
    private Integer filmId;
    private int useful;
    private Set<Integer> likes;
    private Set<Integer> dislikes;

}
