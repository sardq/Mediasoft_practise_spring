package org.example.spring_practise.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;
import org.example.spring_practise.Enums.SightCategory;

import java.util.UUID;

@Value
public class SightResponseDTO {
    @Schema(description = "ID достопримечательности", example = "1")
    UUID id;
    @Schema(description = "Название", example = "Исторический музей")
    String name;
    @Schema(description = "Категория", example = "MUSEUM")
    SightCategory category;
    @Schema(description = "Средняя оценка", example = "4.8")
    Double averageRating;
    @Schema(description = "Дистанция от пользователя в метрах (если применимо)", example = "150.5")
    Double distanceMeters;
}
