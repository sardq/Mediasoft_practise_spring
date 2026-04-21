package org.example.spring_practise.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.UUID;
@Value
public class ReviewTextRequestDTO {
    @Schema(description = "ID пользователя", required = true)
    @NotNull
    UUID userId;
    @Schema(description = "Текст отзыва", example = "Отличное место для прогулок!")
    @Size(max = 1000)
    String text;
}