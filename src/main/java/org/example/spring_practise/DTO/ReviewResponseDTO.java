package org.example.spring_practise.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;
@Builder
@Value
@AllArgsConstructor
public class ReviewResponseDTO {
    @Schema(description = "ID отзыва", example = "1")
    UUID id;
    @Schema(description = "ID пользователя", example = "10")
    UUID userId;
    @Schema(description = "Оценка", example = "5")
    Integer rating;
    @Schema(description = "Текст отзыва", example = "Все супер!")
    String text;
}
