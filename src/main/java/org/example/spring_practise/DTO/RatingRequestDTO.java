package org.example.spring_practise.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class RatingRequestDTO {
    @Schema(description = "Оценка достопримечательности от 1 до 5", example = "5")
    @NotNull
    @Min(1)
    @Max(5)
    Integer rating;
}