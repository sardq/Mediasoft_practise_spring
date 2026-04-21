package org.example.spring_practise.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.spring_practise.DTO.RatingRequestDTO;
import org.example.spring_practise.DTO.ReviewResponseDTO;
import org.example.spring_practise.DTO.ReviewTextRequestDTO;
import org.example.spring_practise.Services.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Управление отзывами и оценками достопримечательностей")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/sight/{sightId}")
    @Operation(summary = "Получить отзывы о достопримечательности")
    @ApiResponse(responseCode = "200", description = "Список отзывов")
    public List<ReviewResponseDTO> getReviews(
            @Parameter(description = "ID достопримечательности", required = true) @PathVariable UUID sightId,
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "20") int size) {
        return reviewService.getReviewsBySightId(sightId, page, size);
    }

    @PutMapping("/rating/{sightId}")
    @Operation(summary = "Выставить или обновить оценку (1-5)")
    @ApiResponse(responseCode = "200", description = "Оценка успешно сохранена")
    public void setRating(
            @Parameter(description = "ID достопримечательности", required = true) @PathVariable UUID sightId,
            @Valid @RequestBody RatingRequestDTO ratingRequest) {
        reviewService.saveOrUpdateRating(sightId, ratingRequest);
    }

    @PutMapping("/text/{sightId}")
    @Operation(summary = "Написать или обновить текст отзыва")
    @ApiResponse(responseCode = "200", description = "Текст успешно сохранен")
    public void setReviewText(
            @Parameter(description = "ID достопримечательности", required = true) @PathVariable UUID sightId,
            @Valid @RequestBody ReviewTextRequestDTO textRequest) {
        reviewService.saveOrUpdateText(sightId, textRequest);
    }
    @GetMapping
    @Operation(summary = "Получить все отзывы")
    public Page<ReviewResponseDTO> GetAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return reviewService.findAll(page, size);
    }
    @DeleteMapping("/{id}")
    public void Delete(@PathVariable UUID id) {
        reviewService.remove(id);
    }
}