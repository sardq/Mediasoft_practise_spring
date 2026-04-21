package org.example.spring_practise.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.spring_practise.DTO.SightDetailsResponseDTO;
import org.example.spring_practise.DTO.SightRequestDTO;
import org.example.spring_practise.DTO.SightResponseDTO;
import org.example.spring_practise.Enums.SightCategory;
import org.example.spring_practise.Enums.SortBy;
import org.example.spring_practise.Services.SightService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sights")
@Tag(name = "Sights", description = "Поиск и получение информации о достопримечательностях")
@RequiredArgsConstructor
public class SightController {

    private final SightService sightService;

    @GetMapping("/nearby")
    @Operation(summary = "Получить достопримечательности рядом")
    @ApiResponse(responseCode = "200", description = "Список достопримечательностей")
    public List<SightResponseDTO> GetNearbySights(
            @Parameter(description = "Широта", required = true) @RequestParam double lat,
            @Parameter(description = "Долгота", required = true) @RequestParam double lon,
            @Parameter(description = "Радиус в метрах") @RequestParam(defaultValue = "5000") int radius,
            @Parameter(description = "Категория") @RequestParam(required = false) SightCategory category,
            @Parameter(description = "Минимальная оценка") @RequestParam(defaultValue = "0.0") double minRating,
            @Parameter(description = "Сортировка") @RequestParam(defaultValue = "DISTANCE") SortBy sortBy,
            @Parameter(description = "Лимит записей") @RequestParam(defaultValue = "10") int limit) {

        return sightService.getNearbySights(lat, lon, radius, category, minRating, sortBy, limit);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить информацию о конкретной достопримечательности")
    @ApiResponse(responseCode = "200", description = "Детальная информация")
    @ApiResponse(responseCode = "404", description = "Достопримечательность не найдена")
    public SightDetailsResponseDTO GetSight(
            @Parameter(description = "ID достопримечательности", required = true) @PathVariable UUID id) {
        return sightService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Создать достопримечательность)")
    @ApiResponse(responseCode = "201", description = "Успешно создано")
    public SightDetailsResponseDTO CreateSight(@Valid @RequestBody SightRequestDTO dto) {
        return sightService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить достопримечательность")
    @ApiResponse(responseCode = "200", description = "Успешно обновлено")
    public SightDetailsResponseDTO UpdateSight(
            @Parameter(description = "ID", required = true) @PathVariable UUID id,
            @Valid @RequestBody SightRequestDTO dto) {
        return sightService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить достопримечательность")
    @ApiResponse(responseCode = "200", description = "Успешно удалено")
    public void DeleteSight(@Parameter(description = "ID", required = true) @PathVariable UUID id) {
        sightService.remove(id);
    }
    @GetMapping
    @Operation(summary = "Получить все достопримечательности")
    public Page<SightResponseDTO> GetAllSights(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return sightService.findAll(page, size);
    }

}
