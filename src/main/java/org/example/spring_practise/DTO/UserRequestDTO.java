package org.example.spring_practise.DTO;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class UserRequestDTO {
    @Schema(description = "Имя пользователя", example = "IvanIvanov")
    @NotBlank
    String username;

    @Schema(description = "Электронная почта", example = "ivan@example.com")
    @NotBlank
    @Email
    String email;
}
