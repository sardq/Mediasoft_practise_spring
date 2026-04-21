package org.example.spring_practise.Controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.spring_practise.DTO.SightDetailsResponseDTO;
import org.example.spring_practise.DTO.SightRequestDTO;
import org.example.spring_practise.DTO.SightResponseDTO;
import org.example.spring_practise.Enums.SightCategory;
import org.example.spring_practise.Enums.SortBy;
import org.example.spring_practise.Services.SightService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(SightController.class)
class SightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SightService sightService;

    private UUID sightId;
    private SightResponseDTO sightResponseDTO;
    private SightDetailsResponseDTO sightDetailsResponseDTO;
    private SightRequestDTO sightRequestDTO;

    @BeforeEach
    void setUp() {
        sightId = UUID.randomUUID();

        sightResponseDTO = new SightResponseDTO(sightId, "Красная площадь", SightCategory.MONUMENT, 4.8, 435.0);

        sightDetailsResponseDTO = new SightDetailsResponseDTO(sightId, "Красная площадь", SightCategory.MONUMENT, "Главная площадь страны", 4.8, 15);

        sightRequestDTO = new SightRequestDTO("Красная площадь", SightCategory.MONUMENT, "Главная площадь страны", 55.7539, 37.6208);
    }



    @Test
    void getNearbySights_WithDefaultParameters_ShouldUseDefaults() throws Exception {
        List<SightResponseDTO> sights = Arrays.asList(sightResponseDTO);
        when(sightService.getNearbySights(eq(55.7539), eq(37.6208), eq(5000),
                isNull(), eq(0.0), eq(SortBy.DISTANCE), eq(10)))
                .thenReturn(sights);

        mockMvc.perform(get("/api/sights/nearby")
                        .param("lat", "55.7539")
                        .param("lon", "37.6208"))
                .andExpect(status().isOk());

        verify(sightService, times(1)).getNearbySights(55.7539, 37.6208, 5000,
                null, 0.0, SortBy.DISTANCE, 10);
    }

    @Test
    void getNearbySights_WithoutRequiredParams_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/sights/nearby")
                        .param("lat", "55.7539"))
                .andExpect(status().isBadRequest());

        verify(sightService, never()).getNearbySights(anyDouble(), anyDouble(), anyInt(),
                any(), anyDouble(), any(), anyInt());
    }

    @ParameterizedTest
    @EnumSource(SortBy.class)
    void getNearbySights_WithDifferentSortBy_ShouldWork(SortBy sortBy) throws Exception {
        when(sightService.getNearbySights(anyDouble(), anyDouble(), anyInt(),
                any(), anyDouble(), eq(sortBy), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/sights/nearby")
                        .param("lat", "55.7539")
                        .param("lon", "37.6208")
                        .param("sortBy", sortBy.name()))
                .andExpect(status().isOk());

        verify(sightService, times(1)).getNearbySights(anyDouble(), anyDouble(), anyInt(),
                any(), anyDouble(), eq(sortBy), anyInt());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 20, 50})
    void getNearbySights_WithDifferentLimits_ShouldWork(int limit) throws Exception {
        when(sightService.getNearbySights(anyDouble(), anyDouble(), anyInt(),
                any(), anyDouble(), any(), eq(limit)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/sights/nearby")
                        .param("lat", "55.7539")
                        .param("lon", "37.6208")
                        .param("limit", String.valueOf(limit)))
                .andExpect(status().isOk());

        verify(sightService, times(1)).getNearbySights(anyDouble(), anyDouble(), anyInt(),
                any(), anyDouble(), any(), eq(limit));
    }


    @Test
    void getSight_WithInvalidUUID_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/sights/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(sightService, never()).getById(any());
    }


    @Test
    void createSight_WithValidData_ShouldReturnCreated() throws Exception {
        when(sightService.create(any(SightRequestDTO.class))).thenReturn(sightDetailsResponseDTO);

        mockMvc.perform(post("/api/sights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sightRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sightId.toString()))
                .andExpect(jsonPath("$.name").value("Красная площадь"));

        verify(sightService, times(1)).create(any(SightRequestDTO.class));
    }

    @Test
    void createSight_WithMissingName_ShouldReturnBadRequest() throws Exception {
        sightRequestDTO = new SightRequestDTO(null, SightCategory.MONUMENT, "Главная площадь страны", 55.7539, 37.6208);


        mockMvc.perform(post("/api/sights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sightRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(sightService, never()).create(any());
    }

    @Test
    void createSight_WithInvalidCoordinates_ShouldReturnBadRequest() throws Exception {
        sightRequestDTO = new SightRequestDTO("Красная площадь", SightCategory.MONUMENT, "Главная площадь страны", 200.0, 200.0);


        mockMvc.perform(post("/api/sights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sightRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(sightService, never()).create(any());
    }

    @Test
    void createSight_WithNullCategory_ShouldReturnBadRequest() throws Exception {
        sightRequestDTO = new SightRequestDTO("Красная площадь", null, "Главная площадь страны", 55.7539, 37.6208);

        mockMvc.perform(post("/api/sights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sightRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(sightService, never()).create(any());
    }


    @Test
    void updateSight_WithValidData_ShouldReturnUpdated() throws Exception {
        when(sightService.update(eq(sightId), any(SightRequestDTO.class)))
                .thenReturn(sightDetailsResponseDTO);

        mockMvc.perform(put("/api/sights/{id}", sightId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sightRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sightId.toString()))
                .andExpect(jsonPath("$.name").value("Красная площадь"));

        verify(sightService, times(1)).update(eq(sightId), any(SightRequestDTO.class));
    }

    @Test
    void updateSight_WithEmptyName_ShouldReturnBadRequest() throws Exception {
        sightRequestDTO = new SightRequestDTO("", SightCategory.MONUMENT, "Главная площадь страны", 55.7539, 37.6208);


        mockMvc.perform(put("/api/sights/{id}", sightId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sightRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(sightService, never()).update(any(), any());
    }


    // ==================== DELETE /api/sights/{id} ====================

    @Test
    void deleteSight_WithValidId_ShouldReturnOk() throws Exception {
        doNothing().when(sightService).remove(sightId);

        mockMvc.perform(delete("/api/sights/{id}", sightId))
                .andExpect(status().isOk());

        verify(sightService, times(1)).remove(sightId);
    }

    @Test
    void deleteSight_WithNonExistentId_ShouldStillReturnOk() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        doNothing().when(sightService).remove(nonExistentId);

        mockMvc.perform(delete("/api/sights/{id}", nonExistentId))
                .andExpect(status().isOk());

        verify(sightService, times(1)).remove(nonExistentId);
    }

    @Test
    void deleteSight_WithInvalidUUID_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/api/sights/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(sightService, never()).remove(any());
    }

    // ==================== GET /api/sights ====================

    @Test
    void getAllSights_ShouldReturnPage() throws Exception {
        Page<SightResponseDTO> page = new PageImpl<>(
                Arrays.asList(sightResponseDTO),
                PageRequest.of(0, 20),
                1
        );

        when(sightService.findAll(eq(0), eq(20))).thenReturn(page);

        mockMvc.perform(get("/api/sights")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(sightId.toString()))
                .andExpect(jsonPath("$.content[0].name").value("Красная площадь"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.numberOfElements").value(1));

        verify(sightService, times(1)).findAll(0, 20);
    }

    @Test
    void getAllSights_WithDefaultPagination_ShouldUseDefaults() throws Exception {
        when(sightService.findAll(eq(0), eq(20))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/sights"))
                .andExpect(status().isOk());

        verify(sightService, times(1)).findAll(0, 20);
    }

    @Test
    void getAllSights_WithCustomPagination_ShouldPassParameters() throws Exception {
        when(sightService.findAll(eq(2), eq(50))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/sights")
                        .param("page", "2")
                        .param("size", "50"))
                .andExpect(status().isOk());

        verify(sightService, times(1)).findAll(2, 50);
    }

    @Test
    void getAllSights_WithNegativePage_ShouldStillPassToService() throws Exception {
        when(sightService.findAll(eq(-1), eq(20))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/sights")
                        .param("page", "-1"))
                .andExpect(status().isOk());

        verify(sightService, times(1)).findAll(-1, 20);
    }

    // ==================== Edge Cases и дополнительные тесты ====================

    @Test
    void getNearbySights_WithZeroRadius_ShouldWork() throws Exception {
        when(sightService.getNearbySights(anyDouble(), anyDouble(), eq(0),
                any(), anyDouble(), any(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/sights/nearby")
                        .param("lat", "48.8584")
                        .param("lon", "2.2945")
                        .param("radius", "0"))
                .andExpect(status().isOk());

        verify(sightService, times(1)).getNearbySights(anyDouble(), anyDouble(), eq(0),
                any(), anyDouble(), any(), anyInt());
    }

    @Test
    void getNearbySights_WithNegativeMinRating_ShouldUseDefault() throws Exception {
        when(sightService.getNearbySights(anyDouble(), anyDouble(), anyInt(),
                any(), eq(0.0), any(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/sights/nearby")
                        .param("lat", "55.7539")
                        .param("lon", "37.6208")
                        .param("minRating", "-1.0"))
                .andExpect(status().isOk());

        verify(sightService, times(1)).getNearbySights(anyDouble(), anyDouble(), anyInt(),
                any(), eq(-1.0), any(), anyInt());
    }

    @Test
    void createSight_WithMaxLengthFields_ShouldWork() throws Exception {
        String longText = "A".repeat(1000);
        sightRequestDTO = new SightRequestDTO(longText, SightCategory.MONUMENT, longText, 55.7539, 37.6208);


        when(sightService.create(any(SightRequestDTO.class))).thenReturn(sightDetailsResponseDTO);

        mockMvc.perform(post("/api/sights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sightRequestDTO)))
                .andExpect(status().isOk());

        verify(sightService, times(1)).create(any(SightRequestDTO.class));
    }

    @Test
    void updateSight_WithPartialData_ShouldWork() throws Exception {

        SightRequestDTO partialUpdate = new SightRequestDTO("Обновленное название", SightCategory.PARK, "Обновленное описание", 55.7539, 37.6208);

        SightDetailsResponseDTO updatedSight = new SightDetailsResponseDTO(sightId, "Обновленное название", SightCategory.PARK, "Обновленное описание", 4.8, 15);

        when(sightService.update(eq(sightId), any(SightRequestDTO.class)))
                .thenReturn(updatedSight);

        mockMvc.perform(put("/api/sights/{id}", sightId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Обновленное название"))
                .andExpect(jsonPath("$.category").value("PARK"));

        verify(sightService, times(1)).update(eq(sightId), any(SightRequestDTO.class));
    }
}