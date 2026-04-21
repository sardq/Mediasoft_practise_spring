package org.example.spring_practise.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.spring_practise.DTO.UserRequestDTO;
import org.example.spring_practise.DTO.UserResponseDTO;
import org.example.spring_practise.Services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "CRUD операции для пользователей")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Получить всех пользователей")
    public Page<UserResponseDTO> GetAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return userService.findAll(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить определенного пользователя")
    @ApiResponse(responseCode = "200", description = "Информация о пользователе")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public UserResponseDTO GetUser(@Parameter(description = "ID пользователя", required = true) @PathVariable UUID id) {
        return userService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Создать пользователя")
    @ApiResponse(responseCode = "201", description = "Пользователь успешно создан")
    public UserResponseDTO CreateUser(@Valid @RequestBody UserRequestDTO userDTO) {
        return userService.create(userDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить пользователя")
    @ApiResponse(responseCode = "200", description = "Пользователь успешно обновлен")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public UserResponseDTO UpdateUser(
            @Parameter(description = "ID пользователя", required = true) @PathVariable UUID id,
            @Valid @RequestBody UserRequestDTO userDTO) {
        return userService.update(id, userDTO);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя")
    @ApiResponse(responseCode = "200", description = "Пользователь удален")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public void DeleteUser(@Parameter(description = "ID пользователя", required = true) @PathVariable UUID id) {
        userService.remove(id);
    }

}