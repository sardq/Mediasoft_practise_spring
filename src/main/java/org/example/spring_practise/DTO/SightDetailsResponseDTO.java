package org.example.spring_practise.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import org.example.spring_practise.Enums.SightCategory;

import java.util.UUID;

@Value
public class SightDetailsResponseDTO {
    @Schema(description = "ID достопримечательности", example = "1")
    UUID id;
    @Schema(description = "Название", example = "Исторический музей")
    String name;
    @Schema(description = "Категория", example = "MUSEUM")
    SightCategory category;
    @Schema(description = "Подробное описание", example = "Крупнейший национальный исторический музей...")
    String description;
    @Schema(description = "Средняя оценка", example = "4.8")
    Double averageRating;
    @Schema(description = "Количество отзывов", example = "342")
    Integer reviewsCount;
}