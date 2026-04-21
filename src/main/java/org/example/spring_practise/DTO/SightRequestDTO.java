package org.example.spring_practise.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.example.spring_practise.Enums.SightCategory;

@Value
public class SightRequestDTO {
    @Schema(description = "Название достопримечательности", example = "Исторический музей")
    @NotBlank
    String name;

    @Schema(description = "Категория", example = "MUSEUM")
    @NotNull
    SightCategory category;

    @Schema(description = "Подробное описание", example = "Описание музея...")
    String description;

    @Schema(description = "Широта", example = "55.7558")
    @NotNull
    @Min(-90) @Max(90)
    Double lat;

    @Schema(description = "Долгота", example = "37.6173")
    @NotNull
    @Min(-180) @Max(180)
    Double lon;
}