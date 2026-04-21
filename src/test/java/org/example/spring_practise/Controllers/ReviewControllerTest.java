package org.example.spring_practise.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.spring_practise.DTO.RatingRequestDTO;
import org.example.spring_practise.DTO.ReviewResponseDTO;
import org.example.spring_practise.DTO.ReviewTextRequestDTO;
import org.example.spring_practise.Services.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ReviewController.class)
class ReviewControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private ReviewService reviewService;
    private UUID userId;
    private UUID reviewId;
    private UUID sightId;
    private ReviewResponseDTO reviewResponseDTO;
    private RatingRequestDTO ratingRequestDTO;
    private ReviewTextRequestDTO reviewTextRequestDTO;
    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        reviewId = UUID.randomUUID();
        sightId = UUID.randomUUID();
        reviewResponseDTO = ReviewResponseDTO.builder()
                .id(reviewId)
                .userId(userId)
                .rating(5)
                .text("Отличное место!")
                .build();

        ratingRequestDTO = new RatingRequestDTO(userId, 4);
        reviewTextRequestDTO = new ReviewTextRequestDTO(userId, "Очень красивая достопримечательность!");
    }

    @Test
    void getReviews_ShouldReturnListOfReviews() throws Exception {
        when(reviewService.getReviewsBySightId(eq(sightId), anyInt(), anyInt()))
                .thenReturn(List.of(reviewResponseDTO));

        mockMvc.perform(get("/api/reviews/sight/{sightId}", sightId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reviewId.toString()))
                .andExpect(jsonPath("$[0].text").value("Отличное место!"));
    }

    @Test
    void getReviews_WithDefaultPagination_ShouldUseDefaults() throws Exception {
        when(reviewService.getReviewsBySightId(eq(sightId), eq(0), eq(20)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/reviews/sight/{sightId}", sightId))
                .andExpect(status().isOk());

        verify(reviewService, times(1)).getReviewsBySightId(sightId, 0, 20);
    }

    @Test
    void getReviews_WithCustomPagination_ShouldPassParameters() throws Exception {
        when(reviewService.getReviewsBySightId(eq(sightId), eq(2), eq(10)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/reviews/sight/{sightId}", sightId)
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(reviewService, times(1)).getReviewsBySightId(sightId, 2, 10);
    }


    @Test
    void setRating_WithValidData_ShouldReturnOk() throws Exception {
        doNothing().when(reviewService).saveOrUpdateRating(eq(sightId), any(RatingRequestDTO.class));

        mockMvc.perform(put("/api/reviews/rating/{sightId}", sightId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingRequestDTO)))
                .andExpect(status().isOk());

        verify(reviewService, times(1)).saveOrUpdateRating(eq(sightId), any(RatingRequestDTO.class));
    }

    @Test
    void setRating_WithInvalidRating_ShouldReturnBadRequest() throws Exception {
        RatingRequestDTO invalidRequest = new RatingRequestDTO(userId, 6);

        mockMvc.perform(put("/api/reviews/rating/{sightId}", sightId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).saveOrUpdateRating(any(), any());
    }

    @Test
    void setRating_WithNullUserEmail_ShouldReturnBadRequest() throws Exception {
        RatingRequestDTO invalidRequest = new RatingRequestDTO(null, 4);

        mockMvc.perform(put("/api/reviews/rating/{sightId}", sightId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).saveOrUpdateRating(any(), any());
    }


    @Test
    void setReviewText_WithValidData_ShouldReturnOk() throws Exception {
        doNothing().when(reviewService).saveOrUpdateText(eq(sightId), any(ReviewTextRequestDTO.class));

        mockMvc.perform(put("/api/reviews/text/{sightId}", sightId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewTextRequestDTO)))
                .andExpect(status().isOk());

        verify(reviewService, times(1)).saveOrUpdateText(eq(sightId), any(ReviewTextRequestDTO.class));
    }


    @Test
    void setReviewText_WithNullUserEmail_ShouldReturnBadRequest() throws Exception {
        ReviewTextRequestDTO invalidRequest = new ReviewTextRequestDTO(null, "Красивое место");

        mockMvc.perform(put("/api/reviews/text/{sightId}", sightId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).saveOrUpdateText(any(), any());
    }

    @Test
    void getAllReviews_WithDefaultPagination_ShouldUseDefaults() throws Exception {
        when(reviewService.findAll(eq(0), eq(20))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/reviews"))
                .andExpect(status().isOk());

        verify(reviewService, times(1)).findAll(0, 20);
    }


    @Test
    void delete_ShouldReturnOk() throws Exception {
        doNothing().when(reviewService).remove(reviewId);

        mockMvc.perform(delete("/api/reviews/{id}", reviewId))
                .andExpect(status().isOk());

        verify(reviewService, times(1)).remove(reviewId);
    }

    @Test
    void delete_WithNonExistentId_ShouldStillCallService() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        doNothing().when(reviewService).remove(nonExistentId);

        mockMvc.perform(delete("/api/reviews/{id}", nonExistentId))
                .andExpect(status().isOk());

        verify(reviewService, times(1)).remove(nonExistentId);
    }


    @Test
    void getReviews_WithNegativePage_ShouldStillPassToService() throws Exception {
        when(reviewService.getReviewsBySightId(eq(sightId), eq(-1), eq(20)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/reviews/sight/{sightId}", sightId)
                        .param("page", "-1"))
                .andExpect(status().isOk());

        verify(reviewService, times(1)).getReviewsBySightId(sightId, -1, 20);
    }

    @Test
    void setRating_WithMinimalRating_ShouldSucceed() throws Exception {
        ratingRequestDTO = new RatingRequestDTO(userId, 1);

        doNothing().when(reviewService).saveOrUpdateRating(eq(sightId), any(RatingRequestDTO.class));

        mockMvc.perform(put("/api/reviews/rating/{sightId}", sightId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingRequestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void setRating_WithMaximalRating_ShouldSucceed() throws Exception {
        ratingRequestDTO = new RatingRequestDTO(userId, 5);

        doNothing().when(reviewService).saveOrUpdateRating(eq(sightId), any(RatingRequestDTO.class));

        mockMvc.perform(put("/api/reviews/rating/{sightId}", sightId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingRequestDTO)))
                .andExpect(status().isOk());
    }
}