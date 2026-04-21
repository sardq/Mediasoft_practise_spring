package org.example.spring_practise.Services;

import org.example.spring_practise.DTO.RatingRequestDTO;
import org.example.spring_practise.DTO.ReviewResponseDTO;
import org.example.spring_practise.DTO.ReviewTextRequestDTO;
import org.example.spring_practise.Entities.Review;
import org.example.spring_practise.Entities.Sight;
import org.example.spring_practise.Entities.User;
import org.example.spring_practise.Mappers.ReviewMapper;
import org.example.spring_practise.Repositories.ReviewRepository;
import org.example.spring_practise.Repositories.SightRepository;
import org.example.spring_practise.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private SightRepository sightRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewService reviewService;

    private UUID sightId;
    private UUID userId;
    private UUID reviewId;
    private Sight sight;
    private User user;
    private Review review;
    private ReviewResponseDTO reviewResponseDTO;
    private RatingRequestDTO ratingRequestDTO;
    private ReviewTextRequestDTO reviewTextRequestDTO;

    @BeforeEach
    void setUp() {
        sightId = UUID.randomUUID();
        userId = UUID.randomUUID();
        reviewId = UUID.randomUUID();

        sight = new Sight();
        sight.setId(sightId);
        sight.setName("Красная площадь");
        sight.setAverageRating(4.5);
        sight.setReviewsCount(10);

        user = new User();
        user.setId(userId);
        user.setUsername("john_doe");
        user.setEmail("john@example.com");

        review = new Review();
        review.setId(reviewId);
        review.setSight(sight);
        review.setUser(user);
        review.setRating(5);
        review.setText("Отличное место!");

        reviewResponseDTO = new ReviewResponseDTO(reviewId,userId, 5, "Отличное место!");

        ratingRequestDTO = new RatingRequestDTO(userId, 4);


        reviewTextRequestDTO = new ReviewTextRequestDTO(userId, "Новый текст отзыва");

    }


    @Test
    void getReviewsBySightId_ShouldReturnListOfReviewDTOs() {
        Page<Review> reviewPage = new PageImpl<>(List.of(review));
        when(reviewRepository.findBySightId(eq(sightId), any(Pageable.class)))
                .thenReturn(reviewPage);
        when(reviewMapper.toDTO(review)).thenReturn(reviewResponseDTO);

        List<ReviewResponseDTO> result = reviewService.getReviewsBySightId(sightId, 0, 20);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(reviewId);
        assertThat(result.get(0).getRating()).isEqualTo(5);
        verify(reviewRepository, times(1))
                .findBySightId(eq(sightId), eq(PageRequest.of(0, 20)));
        verify(reviewMapper, times(1)).toDTO(review);
    }

    @Test
    void getReviewsBySightId_WithNoReviews_ShouldReturnEmptyList() {
         
        Page<Review> emptyPage = Page.empty();
        when(reviewRepository.findBySightId(eq(sightId), any(Pageable.class)))
                .thenReturn(emptyPage);

        
        List<ReviewResponseDTO> result = reviewService.getReviewsBySightId(sightId, 0, 20);

         
        assertThat(result).isEmpty();
        verify(reviewRepository, times(1))
                .findBySightId(eq(sightId), eq(PageRequest.of(0, 20)));
        verify(reviewMapper, never()).toDTO(any());
    }

    @Test
    void getReviewsBySightId_WithCustomPagination_ShouldUseCorrectPageable() {
         
        Page<Review> reviewPage = new PageImpl<>(List.of(review));
        when(reviewRepository.findBySightId(eq(sightId), any(Pageable.class)))
                .thenReturn(reviewPage);
        when(reviewMapper.toDTO(review)).thenReturn(reviewResponseDTO);

        reviewService.getReviewsBySightId(sightId, 2, 10);

        verify(reviewRepository, times(1))
                .findBySightId(eq(sightId), eq(PageRequest.of(2, 10)));
    }


    @Test
    void findAll_ShouldReturnPageOfReviewDTOs() {
        Page<Review> reviewPage = new PageImpl<>(List.of(review));
        when(reviewRepository.findAll(any(Pageable.class))).thenReturn(reviewPage);
        when(reviewMapper.toDTO(review)).thenReturn(reviewResponseDTO);

        Page<ReviewResponseDTO> result = reviewService.findAll(0, 20);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(reviewId);
        verify(reviewRepository, times(1)).findAll(PageRequest.of(0, 20));
        verify(reviewMapper, times(1)).toDTO(review);
    }

    @Test
    void findAll_WithEmptyPage_ShouldReturnEmptyPage() {
        Page<Review> emptyPage = Page.empty();
        when(reviewRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        Page<ReviewResponseDTO> result = reviewService.findAll(0, 20);

        assertThat(result).isEmpty();
        verify(reviewRepository, times(1)).findAll(PageRequest.of(0, 20));
        verify(reviewMapper, never()).toDTO(any());
    }

    @Test
    void findAll_WithCustomPagination_ShouldUseCorrectPageable() {
        when(reviewRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        reviewService.findAll(3, 50);

        verify(reviewRepository, times(1)).findAll(PageRequest.of(3, 50));
    }


    @Test
    void saveOrUpdateRating_WhenSightNotFound_ShouldThrowException() {
        when(sightRepository.findById(sightId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.saveOrUpdateRating(sightId, ratingRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Достопримечательность не найдена");

        verify(sightRepository, times(1)).findById(sightId);
        verify(reviewRepository, never()).findBySightIdAndUserId(any(), any());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void saveOrUpdateRating_WhenUserNotFound_ShouldThrowException() {
        when(sightRepository.findById(sightId)).thenReturn(Optional.of(sight));
        when(reviewRepository.findBySightIdAndUserId(sightId, userId))
                .thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.saveOrUpdateRating(sightId, ratingRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Пользователь не найден");

        verify(userRepository, times(1)).findById(userId);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void saveOrUpdateRating_WhenCreatingNewReview_ShouldSaveAndRecalculate() {
        ratingRequestDTO = new RatingRequestDTO(userId, 4);

        when(sightRepository.findById(sightId)).thenReturn(Optional.of(sight));
        when(reviewRepository.findBySightIdAndUserId(sightId, userId))
                .thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        reviewService.saveOrUpdateRating(sightId, ratingRequestDTO);

        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(sightRepository, times(1)).save(sight);

        assertThat(sight.getReviewsCount()).isEqualTo(11);
        double expectedAvg = (4.5 * 10 + 4) / 11;
        assertThat(sight.getAverageRating()).isEqualTo(expectedAvg);
    }

    @Test
    void saveOrUpdateRating_WhenUpdatingExistingReview_ShouldUpdateAndRecalculate() {
        ratingRequestDTO = new RatingRequestDTO(userId, 4);
        review.setRating(5);

        when(sightRepository.findById(sightId)).thenReturn(Optional.of(sight));
        when(reviewRepository.findBySightIdAndUserId(sightId, userId))
                .thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        reviewService.saveOrUpdateRating(sightId, ratingRequestDTO);

        assertThat(review.getRating()).isEqualTo(4);
        verify(reviewRepository, times(1)).save(review);
        verify(sightRepository, times(1)).save(sight);

        double expectedAvg = (4.5 * 10 - 5 + 4) / 10;
        assertThat(sight.getAverageRating()).isEqualTo(expectedAvg);
        assertThat(sight.getReviewsCount()).isEqualTo(10); 
    }




    @Test
    void saveOrUpdateText_WhenSightNotFound_ShouldThrowException() {
        when(sightRepository.findById(sightId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.saveOrUpdateText(sightId, reviewTextRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Достопримечательность не найдена");

        verify(sightRepository, times(1)).findById(sightId);
        verify(reviewRepository, never()).findBySightIdAndUserId(any(), any());
    }

    @Test
    void saveOrUpdateText_WhenUserNotFound_ShouldThrowException() {
        when(sightRepository.findById(sightId)).thenReturn(Optional.of(sight));
        when(reviewRepository.findBySightIdAndUserId(sightId, userId))
                .thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.saveOrUpdateText(sightId, reviewTextRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Пользователь не найден");

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void saveOrUpdateText_WhenCreatingNewReview_ShouldSaveReview() {
        when(sightRepository.findById(sightId)).thenReturn(Optional.of(sight));
        when(reviewRepository.findBySightIdAndUserId(sightId, userId))
                .thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        reviewService.saveOrUpdateText(sightId, reviewTextRequestDTO);

        verify(reviewRepository, times(1)).save(any(Review.class));
        verify(sightRepository, never()).save(any());
    }

    @Test
    void saveOrUpdateText_WhenUpdatingExistingReview_ShouldUpdateText() {
        review.setText("Старый текст");

        when(sightRepository.findById(sightId)).thenReturn(Optional.of(sight));
        when(reviewRepository.findBySightIdAndUserId(sightId, userId))
                .thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        reviewService.saveOrUpdateText(sightId, reviewTextRequestDTO);

        assertThat(review.getText()).isEqualTo("Новый текст отзыва");
        verify(reviewRepository, times(1)).save(review);
        verify(sightRepository, never()).save(any());
    }

    @Test
    void saveOrUpdateText_WhenUpdatingWithEmptyText_ShouldUpdateToEmpty() {
        reviewTextRequestDTO = new ReviewTextRequestDTO(userId, "");
        review.setText("Старый текст");

        when(sightRepository.findById(sightId)).thenReturn(Optional.of(sight));
        when(reviewRepository.findBySightIdAndUserId(sightId, userId))
                .thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        reviewService.saveOrUpdateText(sightId, reviewTextRequestDTO);

        assertThat(review.getText()).isEmpty();
        verify(reviewRepository, times(1)).save(review);
    }


    @Test
    void getById_WhenReviewExists_ShouldReturnDTO() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewMapper.toDTO(review)).thenReturn(reviewResponseDTO);

        ReviewResponseDTO result = reviewService.getById(reviewId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(reviewId);
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getText()).isEqualTo("Отличное место!");

        verify(reviewRepository, times(1)).findById(reviewId);
        verify(reviewMapper, times(1)).toDTO(review);
    }

    @Test
    void getById_WhenReviewNotFound_ShouldThrowException() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getById(reviewId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Отзыв не найден");

        verify(reviewRepository, times(1)).findById(reviewId);
        verify(reviewMapper, never()).toDTO(any());
    }


    @Test
    void remove_WhenReviewExistsWithRating_ShouldUpdateSightAndDeleteReview() {
        sight.setAverageRating(4.5);
        sight.setReviewsCount(10);
        review.setRating(5);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        reviewService.remove(reviewId);

        assertThat(sight.getReviewsCount()).isEqualTo(9);
        double expectedAvg = (4.5 * 10 - 5) / 9;
        assertThat(sight.getAverageRating()).isEqualTo(expectedAvg);

        verify(sightRepository, times(1)).save(sight);
        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    void remove_WhenReviewExistsWithoutRating_ShouldDeleteWithoutUpdatingSight() {
        review.setRating(null);
        sight.setReviewsCount(10);
        sight.setAverageRating(4.5);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        reviewService.remove(reviewId);

        assertThat(sight.getReviewsCount()).isEqualTo(10); // Не изменилось
        assertThat(sight.getAverageRating()).isEqualTo(4.5); // Не изменилось

        verify(sightRepository, never()).save(sight);
        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    void remove_WhenLastReviewWithRating_ShouldSetAverageRatingToZero() {
         
        sight.setReviewsCount(1);
        sight.setAverageRating(5.0);
        review.setRating(5);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        
        reviewService.remove(reviewId);

         
        assertThat(sight.getReviewsCount()).isEqualTo(0);
        assertThat(sight.getAverageRating()).isEqualTo(0.0);

        verify(sightRepository, times(1)).save(sight);
        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    void remove_WhenReviewNotFound_ShouldThrowException() {
         
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> reviewService.remove(reviewId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Отзыв не найден");

        verify(reviewRepository, times(1)).findById(reviewId);
        verify(reviewRepository, never()).delete(any());
        verify(sightRepository, never()).save(any());
    }


    @Test
    void saveOrUpdateRating_WithMultipleUpdates_ShouldMaintainConsistentAverage() {
         
        sight.setAverageRating(0.0);
        sight.setReviewsCount(0);

        when(sightRepository.findById(sightId)).thenReturn(Optional.of(sight));
        when(reviewRepository.findBySightIdAndUserId(sightId, userId))
                .thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        ratingRequestDTO = new RatingRequestDTO(userId,4 );
        reviewService.saveOrUpdateRating(sightId, ratingRequestDTO);

         
        assertThat(sight.getReviewsCount()).isEqualTo(1);
        assertThat(sight.getAverageRating()).isEqualTo(4.0);

        UUID userId2 = UUID.randomUUID();
        User user2 = new User();
        user2.setId(userId2);

        RatingRequestDTO secondRating = new RatingRequestDTO(userId2, 5);

        when(reviewRepository.findBySightIdAndUserId(sightId, userId2))
                .thenReturn(Optional.empty());
        when(userRepository.findById(userId2)).thenReturn(Optional.of(user2));

        reviewService.saveOrUpdateRating(sightId, secondRating);

         
        assertThat(sight.getReviewsCount()).isEqualTo(2);
        assertThat(sight.getAverageRating()).isEqualTo(4.5);
    }

    @Test
    void saveOrUpdateRating_WhenUpdatingSameReviewMultipleTimes_ShouldUpdateCorrectly() {
         
        sight.setAverageRating(4.0);
        sight.setReviewsCount(1);
        review.setRating(4);

        when(sightRepository.findById(sightId)).thenReturn(Optional.of(sight));
        when(reviewRepository.findBySightIdAndUserId(sightId, userId))
                .thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        ratingRequestDTO = new RatingRequestDTO(userId, 5);
        reviewService.saveOrUpdateRating(sightId, ratingRequestDTO);

         
        assertThat(sight.getReviewsCount()).isEqualTo(1);
        assertThat(sight.getAverageRating()).isEqualTo(5.0);

        ratingRequestDTO = new RatingRequestDTO(userId, 3);
        reviewService.saveOrUpdateRating(sightId, ratingRequestDTO);

         
        assertThat(sight.getReviewsCount()).isEqualTo(1);
        assertThat(sight.getAverageRating()).isEqualTo(3.0);
    }


    @Test
    void getReviewsBySightId_WithLargePagination_ShouldWork() {
         
        when(reviewRepository.findBySightId(eq(sightId), any(Pageable.class)))
                .thenReturn(Page.empty());

        reviewService.getReviewsBySightId(sightId, 1000, 1000);

         
        verify(reviewRepository, times(1))
                .findBySightId(eq(sightId), eq(PageRequest.of(1000, 1000)));
    }

    @Test
    void saveOrUpdateRating_WithBoundaryRatings_ShouldWork() {

        ratingRequestDTO = new RatingRequestDTO(userId, 1);


        when(sightRepository.findById(sightId)).thenReturn(Optional.of(sight));
        when(reviewRepository.findBySightIdAndUserId(sightId, userId))
                .thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        reviewService.saveOrUpdateRating(sightId, ratingRequestDTO);

         
        verify(reviewRepository, times(1)).save(any(Review.class));

        assertThat(ratingRequestDTO.getRating()).isEqualTo(1);
    }

    @Test
    void saveOrUpdateText_WithNullText_ShouldSetNull() {
         
        reviewTextRequestDTO= new ReviewTextRequestDTO(userId, null);

        when(sightRepository.findById(sightId)).thenReturn(Optional.of(sight));
        when(reviewRepository.findBySightIdAndUserId(sightId, userId))
                .thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        
        reviewService.saveOrUpdateText(sightId, reviewTextRequestDTO);

         
        assertThat(review.getText()).isNull();
        verify(reviewRepository, times(1)).save(review);
    }

    @Test
    void remove_WhenReviewExistsWithNullRating_ShouldNotUpdateSight() {
         
        review.setRating(null);
        sight.setReviewsCount(10);
        sight.setAverageRating(4.5);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        
        reviewService.remove(reviewId);

         
        assertThat(sight.getReviewsCount()).isEqualTo(10);
        assertThat(sight.getAverageRating()).isEqualTo(4.5);

        verify(sightRepository, never()).save(any());
        verify(reviewRepository, times(1)).delete(review);
    }
}