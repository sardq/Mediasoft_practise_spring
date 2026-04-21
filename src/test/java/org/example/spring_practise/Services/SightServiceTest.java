package org.example.spring_practise.Services;

import org.example.spring_practise.DTO.SightDetailsResponseDTO;
import org.example.spring_practise.DTO.SightProjection;
import org.example.spring_practise.DTO.SightRequestDTO;
import org.example.spring_practise.DTO.SightResponseDTO;
import org.example.spring_practise.Entities.Review;
import org.example.spring_practise.Entities.Sight;
import org.example.spring_practise.Enums.SightCategory;
import org.example.spring_practise.Enums.SortBy;
import org.example.spring_practise.Mappers.SightMapper;
import org.example.spring_practise.Repositories.ReviewRepository;
import org.example.spring_practise.Repositories.SightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SightServiceTest {

    @Mock
    private SightRepository sightRepository;

    @Mock
    private SightMapper sightMapper;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private SightService sightService;

    private UUID sightId;
    private Sight sight;
    private SightRequestDTO sightRequestDTO;
    private SightDetailsResponseDTO sightDetailsResponseDTO;
    private SightResponseDTO sightResponseDTO;
    private SightProjection sightProjection;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    @BeforeEach
    void setUp() {
        sightId = UUID.randomUUID();
        double lat = 55.7539;
        double lon = 37.6208;
        Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
        sight = new Sight();
        sight.setId(sightId);
        sight.setName("Красная площадь");
        sight.setDescription("Главная площадь страны");
        sight.setCategory(SightCategory.MONUMENT);
        sight.setLocation(point);
        sight.setAverageRating(4.8);
        sight.setReviewsCount(1500);

        sightRequestDTO = new SightRequestDTO("Красная площадь", SightCategory.MONUMENT, "Главная площадь страны",55.7539, 37.6208 );

        sightDetailsResponseDTO = new SightDetailsResponseDTO(sightId, "Красная площадь", SightCategory.MONUMENT, "Главная площадь страны", 4.8, 1500);

        sightResponseDTO = new SightResponseDTO(sightId, "Красная площадь", SightCategory.MONUMENT, 4.8, 500.0);
        
        sightProjection = new SightProjection() {
            @Override
            public UUID getId() {
                return sightId;
            }

            @Override
            public String getName() {
                return "Красная площадь";
            }

            @Override
            public String getCategory() {
                return "MONUMENT";
            }

            @Override
            public Double getAverageRating() {
                return 4.8;
            }

            @Override
            public Double getDistanceMeters() {
                return 500.0;
            }
        };
    }


    @Test
    void getNearbySights_WithAllParameters_ShouldReturnListOfSightResponseDTO() {
         
        List<SightProjection> projections = List.of(sightProjection);
        when(sightRepository.findNearbySights(eq(55.7539), eq(37.6208), eq(5000),
                eq("MONUMENT"), eq(4.0), eq("DISTANCE"), eq(10)))
                .thenReturn(projections);

         
        List<SightResponseDTO> result = sightService.getNearbySights(
                55.7539, 37.6208, 5000, SightCategory.MONUMENT, 4.0, SortBy.DISTANCE, 10);

         
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(sightId);
        assertThat(result.get(0).getName()).isEqualTo("Красная площадь");
        assertThat(result.get(0).getCategory()).isEqualTo(SightCategory.MONUMENT);
        assertThat(result.get(0).getAverageRating()).isEqualTo(4.8);
        assertThat(result.get(0).getDistanceMeters()).isEqualTo(500.0);

        verify(sightRepository, times(1)).findNearbySights(
                55.7539, 37.6208, 5000, "MONUMENT", 4.0, "DISTANCE", 10);
    }

    @Test
    void getNearbySights_WithNullCategory_ShouldPassNullToRepository() {
         
        List<SightProjection> projections = List.of(sightProjection);
        when(sightRepository.findNearbySights(eq(55.7539), eq(37.6208), eq(5000),
                isNull(), eq(0.0), eq("RATING"), eq(5)))
                .thenReturn(projections);

         
        List<SightResponseDTO> result = sightService.getNearbySights(
                55.7539, 37.6208, 5000, null, 0.0, SortBy.RATING, 5);

         
        assertThat(result).hasSize(1);
        verify(sightRepository, times(1)).findNearbySights(
                55.7539, 37.6208, 5000, null, 0.0, "RATING", 5);
    }

    @Test
    void getNearbySights_WithNoResults_ShouldReturnEmptyList() {
         
        when(sightRepository.findNearbySights(anyDouble(), anyDouble(), anyInt(),
                any(), anyDouble(), anyString(), anyInt()))
                .thenReturn(List.of());

         
        List<SightResponseDTO> result = sightService.getNearbySights(
                55.7539, 37.6208, 5000, SightCategory.MONUMENT, 4.0, SortBy.DISTANCE, 10);

         
        assertThat(result).isEmpty();
        verify(sightRepository, times(1)).findNearbySights(
                55.7539, 37.6208, 5000, "MONUMENT", 4.0, "DISTANCE", 10);
    }

    @ParameterizedTest
    @EnumSource(SightCategory.class)
    void getNearbySights_WithDifferentCategories_ShouldWork(SightCategory category) {
         
        when(sightRepository.findNearbySights(anyDouble(), anyDouble(), anyInt(),
                eq(category != null ? category.name() : null), anyDouble(), anyString(), anyInt()))
                .thenReturn(List.of());

         
        sightService.getNearbySights(55.7539, 37.6208, 5000, category, 0.0, SortBy.DISTANCE, 10);

         
        verify(sightRepository, times(1)).findNearbySights(
                anyDouble(), anyDouble(), anyInt(),
                eq(category != null ? category.name() : null), anyDouble(), anyString(), anyInt());
    }

    @ParameterizedTest
    @EnumSource(SortBy.class)
    void getNearbySights_WithDifferentSortBy_ShouldWork(SortBy sortBy) {
         
        when(sightRepository.findNearbySights(anyDouble(), anyDouble(), anyInt(),
                any(), anyDouble(), eq(sortBy.name()), anyInt()))
                .thenReturn(List.of());

         
        sightService.getNearbySights(55.7539, 37.6208, 5000, SightCategory.MONUMENT, 0.0, sortBy, 10);

         
        verify(sightRepository, times(1)).findNearbySights(
                anyDouble(), anyDouble(), anyInt(),
                any(), anyDouble(), eq(sortBy.name()), anyInt());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 20, 50, 100})
    void getNearbySights_WithDifferentLimits_ShouldWork(int limit) {
         
        when(sightRepository.findNearbySights(anyDouble(), anyDouble(), anyInt(),
                any(), anyDouble(), anyString(), eq(limit)))
                .thenReturn(List.of());

         
        sightService.getNearbySights(55.7539, 37.6208, 5000, SightCategory.MONUMENT, 0.0, SortBy.DISTANCE, limit);

         
        verify(sightRepository, times(1)).findNearbySights(
                anyDouble(), anyDouble(), anyInt(),
                any(), anyDouble(), anyString(), eq(limit));
    }

    @Test
    void getNearbySights_WithMultipleResults_ShouldReturnAll() {
         
        SightProjection projection2 = mock(SightProjection.class);
        when(projection2.getId()).thenReturn(UUID.randomUUID());
        when(projection2.getName()).thenReturn(" Название");
        when(projection2.getCategory()).thenReturn("MUSEUM");
        when(projection2.getAverageRating()).thenReturn(4.9);
        when(projection2.getDistanceMeters()).thenReturn(1200.0);

        List<SightProjection> projections = Arrays.asList(sightProjection, projection2);
        when(sightRepository.findNearbySights(anyDouble(), anyDouble(), anyInt(),
                any(), anyDouble(), anyString(), anyInt()))
                .thenReturn(projections);

         
        List<SightResponseDTO> result = sightService.getNearbySights(
                55.7539, 37.6208, 5000, null, 0.0, SortBy.DISTANCE, 20);

         
        assertThat(result).hasSize(2);
        verify(sightRepository, times(1)).findNearbySights(
                anyDouble(), anyDouble(), anyInt(), any(), anyDouble(), anyString(), anyInt());
    }


    @Test
    void getById_WhenSightExists_ShouldReturnDetailsDTO() {
         
        when(sightRepository.findById(sightId)).thenReturn(Optional.of(sight));
        when(sightMapper.toDetailsDTO(sight)).thenReturn(sightDetailsResponseDTO);

         
        SightDetailsResponseDTO result = sightService.getById(sightId);

         
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(sightId);
        assertThat(result.getName()).isEqualTo("Красная площадь");
        assertThat(result.getCategory()).isEqualTo(SightCategory.MONUMENT);
        assertThat(result.getAverageRating()).isEqualTo(4.8);
        assertThat(result.getReviewsCount()).isEqualTo(1500);

        verify(sightRepository, times(1)).findById(sightId);
        verify(sightMapper, times(1)).toDetailsDTO(sight);
    }

    @Test
    void getById_WhenSightNotFound_ShouldThrowException() {
         
        when(sightRepository.findById(sightId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sightService.getById(sightId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Достопримечательность не найдена");

        verify(sightRepository, times(1)).findById(sightId);
        verify(sightMapper, never()).toDetailsDTO(any());
    }


    @Test
    void create_WithValidData_ShouldSaveAndReturnDetailsDTO() {
         
        when(sightMapper.toEntity(sightRequestDTO)).thenReturn(sight);
        when(sightRepository.save(sight)).thenReturn(sight);
        when(sightMapper.toDetailsDTO(sight)).thenReturn(sightDetailsResponseDTO);

         
        SightDetailsResponseDTO result = sightService.create(sightRequestDTO);

         
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(sightId);
        assertThat(result.getName()).isEqualTo("Красная площадь");

        verify(sightMapper, times(1)).toEntity(sightRequestDTO);
        verify(sightRepository, times(1)).save(sight);
        verify(sightMapper, times(1)).toDetailsDTO(sight);
    }

    

    @Test
    void update_WhenSightExists_ShouldUpdateAndReturnDetailsDTO() {
         
        when(sightRepository.findById(sightId)).thenReturn(Optional.of(sight));
        doNothing().when(sightMapper).updateEntity(sightRequestDTO, sight);
        when(sightRepository.save(sight)).thenReturn(sight);
        when(sightMapper.toDetailsDTO(sight)).thenReturn(sightDetailsResponseDTO);

         
        SightDetailsResponseDTO result = sightService.update(sightId, sightRequestDTO);

         
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(sightId);

        verify(sightRepository, times(1)).findById(sightId);
        verify(sightMapper, times(1)).updateEntity(sightRequestDTO, sight);
        verify(sightRepository, times(1)).save(sight);
        verify(sightMapper, times(1)).toDetailsDTO(sight);
    }

    @Test
    void update_WhenSightNotFound_ShouldThrowException() {
         
        when(sightRepository.findById(sightId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sightService.update(sightId, sightRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Достопримечательность не найдена");

        verify(sightRepository, times(1)).findById(sightId);
        verify(sightMapper, never()).updateEntity(any(), any());
        verify(sightRepository, never()).save(any());
    }

    @Test
    void update_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        SightRequestDTO partialUpdate = new SightRequestDTO("Обновленное название", SightCategory.MONUMENT, "Новое описание",55.7539, 37.6208 );

        when(sightRepository.findById(sightId)).thenReturn(Optional.of(sight));
        doNothing().when(sightMapper).updateEntity(partialUpdate, sight);
        when(sightRepository.save(sight)).thenReturn(sight);
        when(sightMapper.toDetailsDTO(sight)).thenReturn(sightDetailsResponseDTO);

         
        SightDetailsResponseDTO result = sightService.update(sightId, partialUpdate);

         
        assertThat(result).isNotNull();
        verify(sightMapper, times(1)).updateEntity(partialUpdate, sight);
        verify(sightRepository, times(1)).save(sight);
    }


    @Test
    void remove_WhenSightExists_ShouldDeleteReviewsAndSight() {
         
        List<Review> reviews = Arrays.asList(new Review(), new Review());
        when(sightRepository.existsById(sightId)).thenReturn(true);
        when(reviewRepository.findBySightId(sightId)).thenReturn(reviews);
        doNothing().when(reviewRepository).deleteAll(reviews);
        doNothing().when(sightRepository).deleteById(sightId);

         
        sightService.remove(sightId);

         
        verify(sightRepository, times(1)).existsById(sightId);
        verify(reviewRepository, times(1)).findBySightId(sightId);
        verify(reviewRepository, times(1)).deleteAll(reviews);
        verify(sightRepository, times(1)).deleteById(sightId);
    }

    @Test
    void remove_WhenSightExistsWithNoReviews_ShouldDeleteSightOnly() {
         
        when(sightRepository.existsById(sightId)).thenReturn(true);
        when(reviewRepository.findBySightId(sightId)).thenReturn(List.of());
        doNothing().when(sightRepository).deleteById(sightId);

         
        sightService.remove(sightId);

         
        verify(reviewRepository, times(1)).findBySightId(sightId);
        verify(reviewRepository, times(1)).deleteAll(List.of());
        verify(sightRepository, times(1)).deleteById(sightId);
    }

    @Test
    void remove_WhenSightNotFound_ShouldThrowException() {
         
        when(sightRepository.existsById(sightId)).thenReturn(false);

        assertThatThrownBy(() -> sightService.remove(sightId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Достопримечательность не найдена");

        verify(sightRepository, times(1)).existsById(sightId);
        verify(reviewRepository, never()).findBySightId(any());
        verify(reviewRepository, never()).deleteAll(any());
        verify(sightRepository, never()).deleteById(any());
    }

    @Test
    void remove_WithMultipleReviews_ShouldDeleteAll() {
         
        List<Review> reviews = Arrays.asList(
                mock(Review.class),
                mock(Review.class),
                mock(Review.class)
        );
        when(sightRepository.existsById(sightId)).thenReturn(true);
        when(reviewRepository.findBySightId(sightId)).thenReturn(reviews);
        doNothing().when(reviewRepository).deleteAll(reviews);
        doNothing().when(sightRepository).deleteById(sightId);

         
        sightService.remove(sightId);

         
        verify(reviewRepository, times(1)).deleteAll(reviews);
        assertThat(reviews).hasSize(3);
    }


    @Test
    void findAll_ShouldReturnPageOfSightResponseDTO() {
         
        Page<Sight> sightPage = new PageImpl<>(List.of(sight));
        when(sightRepository.findAll(any(Pageable.class))).thenReturn(sightPage);
        when(sightMapper.toResponseWithDistance(sight, null)).thenReturn(sightResponseDTO);

         
        Page<SightResponseDTO> result = sightService.findAll(0, 20);

         
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(sightId);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Красная площадь");

        verify(sightRepository, times(1)).findAll(PageRequest.of(0, 20));
        verify(sightMapper, times(1)).toResponseWithDistance(sight, null);
    }

    @Test
    void findAll_WithEmptyPage_ShouldReturnEmptyPage() {
         
        Page<Sight> emptyPage = Page.empty();
        when(sightRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

         
        Page<SightResponseDTO> result = sightService.findAll(0, 20);

         
        assertThat(result).isEmpty();
        verify(sightRepository, times(1)).findAll(PageRequest.of(0, 20));
        verify(sightMapper, never()).toResponseWithDistance(any(), any());
    }

    @Test
    void findAll_WithCustomPagination_ShouldUseCorrectPageable() {
         
        when(sightRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

         
        sightService.findAll(3, 50);

         
        verify(sightRepository, times(1)).findAll(PageRequest.of(3, 50));
    }

    @Test
    void getNearbySights_WithZeroRadius_ShouldWork() {
         
        when(sightRepository.findNearbySights(eq(55.7539), eq(37.6208), eq(0),
                any(), anyDouble(), anyString(), anyInt()))
                .thenReturn(List.of());

         
        sightService.getNearbySights(55.7539, 37.6208, 0, SightCategory.MONUMENT, 0.0, SortBy.DISTANCE, 10);

         
        verify(sightRepository, times(1)).findNearbySights(
                55.7539, 37.6208, 0, "MONUMENT", 0.0, "DISTANCE", 10);
    }

    @Test
    void getNearbySights_WithMaxRating_ShouldWork() {
         
        when(sightRepository.findNearbySights(anyDouble(), anyDouble(), anyInt(),
                any(), eq(5.0), anyString(), anyInt()))
                .thenReturn(List.of());

         
        sightService.getNearbySights(55.7539, 37.6208, 5000, SightCategory.MONUMENT, 5.0, SortBy.RATING, 10);

         
        verify(sightRepository, times(1)).findNearbySights(
                anyDouble(), anyDouble(), anyInt(), any(), eq(5.0), anyString(), anyInt());
    }

    @Test
    void update_WithSameData_ShouldWork() {
         
        when(sightRepository.findById(sightId)).thenReturn(Optional.of(sight));
        doNothing().when(sightMapper).updateEntity(sightRequestDTO, sight);
        when(sightRepository.save(sight)).thenReturn(sight);
        when(sightMapper.toDetailsDTO(sight)).thenReturn(sightDetailsResponseDTO);

         
        SightDetailsResponseDTO result = sightService.update(sightId, sightRequestDTO);

         
        assertThat(result).isNotNull();
        verify(sightMapper, times(1)).updateEntity(sightRequestDTO, sight);
    }

    @Test
    void remove_ShouldBeIdempotent() {
         
        when(sightRepository.existsById(sightId)).thenReturn(true);
        when(reviewRepository.findBySightId(sightId)).thenReturn(List.of());
        doNothing().when(sightRepository).deleteById(sightId);

         
        sightService.remove(sightId);

        sightService.remove(sightId);

        verify(sightRepository, times(2)).existsById(sightId);
        verify(sightRepository, times(2)).deleteById(sightId);
    }

    @Test
    void getNearbySights_WithNegativeMinRating_ShouldPassToRepository() {
         
        when(sightRepository.findNearbySights(anyDouble(), anyDouble(), anyInt(),
                any(), eq(-1.0), anyString(), anyInt()))
                .thenReturn(List.of());

         
        sightService.getNearbySights(55.7539, 37.6208, 5000, SightCategory.MONUMENT, -1.0, SortBy.DISTANCE, 10);

         
        verify(sightRepository, times(1)).findNearbySights(
                anyDouble(), anyDouble(), anyInt(), any(), eq(-1.0), anyString(), anyInt());
    }

    @Test
    void getNearbySights_WithLargeRadius_ShouldWork() {
         
        int largeRadius = 100000; // 100 km
        when(sightRepository.findNearbySights(anyDouble(), anyDouble(), eq(largeRadius),
                any(), anyDouble(), anyString(), anyInt()))
                .thenReturn(List.of());

         
        sightService.getNearbySights(55.7539, 37.6208, largeRadius, SightCategory.MONUMENT, 0.0, SortBy.DISTANCE, 10);

         
        verify(sightRepository, times(1)).findNearbySights(
                anyDouble(), anyDouble(), eq(largeRadius), any(), anyDouble(), anyString(), anyInt());
    }
}