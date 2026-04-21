package org.example.spring_practise.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import java.util.UUID;

@Value
public class UserResponseDTO {
    @Schema(description = "ID пользователя", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id;

    @Schema(description = "Имя пользователя", example = "IvanIvanov")
    String username;

    @Schema(description = "Электронная почта", example = "ivan@example.com")
    String email;
}