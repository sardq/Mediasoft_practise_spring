package org.example.spring_practise.Repositories;

import org.example.spring_practise.Entities.Review;
import org.example.spring_practise.Entities.Sight;
import org.example.spring_practise.Entities.User;
import org.example.spring_practise.Enums.SightCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TestEntityManager entityManager;

    private UUID sightId;
    private UUID userId;
    private Sight sight;
    private User user;
    private Review review;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @BeforeEach
    void setUp() {
        double lat = 55.7539;
        double lon = 37.6208;
        Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
        
        sight = new Sight();
        sight.setName(" Красная площадь");
        sight.setDescription("Главная площадь страны");
        sight.setCategory(SightCategory.MONUMENT);
        
        sight.setLocation(point);
        sight.setAverageRating(4.5);
        sight.setReviewsCount(10);
        sight = entityManager.persistAndFlush(sight);
        sightId = sight.getId();

        user = new User();
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user = entityManager.persistAndFlush(user);
        userId = user.getId();

        review = new Review();
        review.setSight(sight);
        review.setUser(user);
        review.setRating(5);
        review.setText("Отличное место!");
        review = entityManager.persistAndFlush(review);
    }


    @Test
    void findBySightId_WithValidSightId_ShouldReturnPageOfReviews() {
         
        Pageable pageable = PageRequest.of(0, 10);

          
        Page<Review> result = reviewRepository.findBySightId(sightId, pageable);

          
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(review.getId());
        assertThat(result.getContent().get(0).getSight().getId()).isEqualTo(sightId);
        assertThat(result.getContent().get(0).getUser().getId()).isEqualTo(userId);
        assertThat(result.getContent().get(0).getRating()).isEqualTo(5);
        assertThat(result.getContent().get(0).getText()).isEqualTo("Отличное место!");
    }

    @Test
    void findBySightId_WithMultipleReviews_ShouldReturnAll() {
         
        Review review2 = new Review();
        review2.setSight(sight);
        review2.setUser(user);
        review2.setRating(4);
        review2.setText("Хорошее место");
        entityManager.persistAndFlush(review2);

        Pageable pageable = PageRequest.of(0, 10);

          
        Page<Review> result = reviewRepository.findBySightId(sightId, pageable);

          
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Review::getRating)
                .containsExactlyInAnyOrder(5, 4);
    }

    @Test
    void findBySightId_WithNoReviews_ShouldReturnEmptyPage() {
         
        UUID nonExistentSightId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

          
        Page<Review> result = reviewRepository.findBySightId(nonExistentSightId, pageable);

          
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void findBySightId_WithPagination_ShouldReturnCorrectPage() {
         
        for (int i = 1; i <= 25; i++) {
            Review additionalReview = new Review();
            additionalReview.setSight(sight);
            additionalReview.setUser(user);
            additionalReview.setRating(i % 5 + 1);
            additionalReview.setText("Отзыв " + i);
            entityManager.persist(additionalReview);
        }
        entityManager.flush();

        Pageable firstPage = PageRequest.of(0, 10);
        Page<Review> firstPageResult = reviewRepository.findBySightId(sightId, firstPage);

          
        assertThat(firstPageResult.getContent()).hasSize(10);
        assertThat(firstPageResult.getTotalElements()).isEqualTo(26); // 25 новых + 1 из setUp
        assertThat(firstPageResult.getTotalPages()).isEqualTo(3);

        Pageable secondPage = PageRequest.of(1, 10);
        Page<Review> secondPageResult = reviewRepository.findBySightId(sightId, secondPage);

          
        assertThat(secondPageResult.getContent()).hasSize(10);

        Pageable thirdPage = PageRequest.of(2, 10);
        Page<Review> thirdPageResult = reviewRepository.findBySightId(sightId, thirdPage);

          
        assertThat(thirdPageResult.getContent()).hasSize(6);
    }


    @Test
    void findBySightId_List_WithValidSightId_ShouldReturnListOfReviews() {
          
        List<Review> result = reviewRepository.findBySightId(sightId);

          
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(review.getId());
        assertThat(result.get(0).getSight().getId()).isEqualTo(sightId);
    }

    @Test
    void findBySightId_List_WithMultipleReviews_ShouldReturnAll() {
         
        Review review2 = new Review();
        review2.setSight(sight);
        review2.setUser(user);
        review2.setRating(4);
        review2.setText("Хорошее место");
        entityManager.persistAndFlush(review2);

          
        List<Review> result = reviewRepository.findBySightId(sightId);

          
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Review::getRating).containsExactlyInAnyOrder(5, 4);
    }

    @Test
    void findBySightId_List_WithNoReviews_ShouldReturnEmptyList() {
         
        UUID nonExistentSightId = UUID.randomUUID();

          
        List<Review> result = reviewRepository.findBySightId(nonExistentSightId);

          
        assertThat(result).isEmpty();
    }

    @Test
    void findBySightId_List_WithReviewsForDifferentSights_ShouldReturnOnlyCorrectSight() {
         
        Sight anotherSight = new Sight();
        anotherSight.setName("Лувр");
        anotherSight.setCategory(SightCategory.MUSEUM);
        Point point = geometryFactory.createPoint(new Coordinate(2.3376, 48.8606));
        anotherSight = entityManager.persistAndFlush(anotherSight);
        anotherSight.setLocation(point);
        Review reviewForAnotherSight = new Review();
        reviewForAnotherSight.setSight(anotherSight);
        reviewForAnotherSight.setUser(user);
        reviewForAnotherSight.setRating(5);
        reviewForAnotherSight.setText("Прекрасный музей");
        entityManager.persistAndFlush(reviewForAnotherSight);

          
        List<Review> result = reviewRepository.findBySightId(sightId);

          
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSight().getId()).isEqualTo(sightId);
    }


    @Test
    void findByUserId_WithValidUserId_ShouldReturnListOfReviews() {
          
        List<Review> result = reviewRepository.findByUserId(userId);

          
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(review.getId());
        assertThat(result.get(0).getUser().getId()).isEqualTo(userId);
    }

    @Test
    void findByUserId_WithMultipleReviewsBySameUser_ShouldReturnAll() {
         
        Review review2 = new Review();
        review2.setSight(sight);
        review2.setUser(user);
        review2.setRating(4);
        review2.setText("Еще один отзыв");
        entityManager.persistAndFlush(review2);

        Review review3 = new Review();
        review3.setSight(sight);
        review3.setUser(user);
        review3.setRating(3);
        review3.setText("Третий отзыв");
        entityManager.persistAndFlush(review3);

          
        List<Review> result = reviewRepository.findByUserId(userId);

          
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Review::getRating).containsExactlyInAnyOrder(5, 4, 3);
    }

    @Test
    void findByUserId_WithNoReviews_ShouldReturnEmptyList() {
         
        UUID nonExistentUserId = UUID.randomUUID();

          
        List<Review> result = reviewRepository.findByUserId(nonExistentUserId);

          
        assertThat(result).isEmpty();
    }

    @Test
    void findByUserId_WithReviewsFromDifferentUsers_ShouldReturnOnlySpecificUser() {
         
        User anotherUser = new User();
        anotherUser.setUsername("jane_doe");
        anotherUser.setEmail("jane@example.com");
        anotherUser = entityManager.persistAndFlush(anotherUser);

        Review reviewFromAnotherUser = new Review();
        reviewFromAnotherUser.setSight(sight);
        reviewFromAnotherUser.setUser(anotherUser);
        reviewFromAnotherUser.setRating(5);
        reviewFromAnotherUser.setText("Отзыв от другого пользователя");
        entityManager.persistAndFlush(reviewFromAnotherUser);

          
        List<Review> result = reviewRepository.findByUserId(userId);

          
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getId()).isEqualTo(userId);
    }


    @Test
    void findBySightIdAndUserId_WhenReviewExists_ShouldReturnOptionalWithReview() {
          
        Optional<Review> result = reviewRepository.findBySightIdAndUserId(sightId, userId);

          
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(review.getId());
        assertThat(result.get().getSight().getId()).isEqualTo(sightId);
        assertThat(result.get().getUser().getId()).isEqualTo(userId);
        assertThat(result.get().getRating()).isEqualTo(5);
        assertThat(result.get().getText()).isEqualTo("Отличное место!");
    }

    @Test
    void findBySightIdAndUserId_WhenReviewDoesNotExist_ShouldReturnEmptyOptional() {
         
        UUID nonExistentSightId = UUID.randomUUID();

          
        Optional<Review> result = reviewRepository.findBySightIdAndUserId(nonExistentSightId, userId);

          
        assertThat(result).isEmpty();
    }

    @Test
    void findBySightIdAndUserId_WhenWrongUser_ShouldReturnEmptyOptional() {
         
        UUID wrongUserId = UUID.randomUUID();

          
        Optional<Review> result = reviewRepository.findBySightIdAndUserId(sightId, wrongUserId);

          
        assertThat(result).isEmpty();
    }

    @Test
    void findBySightIdAndUserId_WithMultipleReviews_ShouldReturnCorrectOne() {
         
        User anotherUser = new User();
        anotherUser.setUsername("jane_doe");
        anotherUser.setEmail("jane@example.com");
        anotherUser = entityManager.persistAndFlush(anotherUser);

        Review reviewFromAnotherUser = new Review();
        reviewFromAnotherUser.setSight(sight);
        reviewFromAnotherUser.setUser(anotherUser);
        reviewFromAnotherUser.setRating(4);
        reviewFromAnotherUser.setText("Отзыв от другого пользователя");
        entityManager.persistAndFlush(reviewFromAnotherUser);

          
        Optional<Review> result = reviewRepository.findBySightIdAndUserId(sightId, userId);

          
        assertThat(result).isPresent();
        assertThat(result.get().getUser().getId()).isEqualTo(userId);
        assertThat(result.get().getRating()).isEqualTo(5);
    }

    @Test
    void findBySightIdAndUserId_WithSameUserDifferentSight_ShouldReturnEmpty() {
         
        Sight anotherSight = new Sight();
        anotherSight.setName("Лувр");
        anotherSight.setCategory(SightCategory.MUSEUM);
        Point point = geometryFactory.createPoint(new Coordinate(2.3376, 48.8606));
        anotherSight.setLocation(point);
        anotherSight = entityManager.persistAndFlush(anotherSight);

          
        Optional<Review> result = reviewRepository.findBySightIdAndUserId(anotherSight.getId(), userId);

          
        assertThat(result).isEmpty();
    }


    @Test
    void save_ShouldPersistReview() {
         
        Review newReview = new Review();
        newReview.setSight(sight);
        newReview.setUser(user);
        newReview.setRating(3);
        newReview.setText("Нормальное место");

          
        Review savedReview = reviewRepository.save(newReview);
        entityManager.flush();

          
        assertThat(savedReview.getId()).isNotNull();

        Review foundReview = entityManager.find(Review.class, savedReview.getId());
        assertThat(foundReview).isNotNull();
        assertThat(foundReview.getRating()).isEqualTo(3);
        assertThat(foundReview.getText()).isEqualTo("Нормальное место");
    }

    @Test
    void update_ShouldModifyReview() {
         
        review.setText("Обновленный текст");
        review.setRating(4);

          
        Review updatedReview = reviewRepository.save(review);
        entityManager.flush();
        entityManager.clear();

          
        Review foundReview = entityManager.find(Review.class, updatedReview.getId());
        assertThat(foundReview.getText()).isEqualTo("Обновленный текст");
        assertThat(foundReview.getRating()).isEqualTo(4);
    }

    @Test
    void delete_ShouldRemoveReview() {
          
        reviewRepository.delete(review);
        entityManager.flush();

          
        Review foundReview = entityManager.find(Review.class, review.getId());
        assertThat(foundReview).isNull();
    }

    @Test
    void findById_ShouldReturnReview() {
          
        Optional<Review> found = reviewRepository.findById(review.getId());

          
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(review.getId());
    }

    @Test
    void existsById_ShouldReturnTrueForExistingReview() {
          
        boolean exists = reviewRepository.existsById(review.getId());

          
        assertThat(exists).isTrue();
    }

    @Test
    void existsById_ShouldReturnFalseForNonExistingReview() {
          
        boolean exists = reviewRepository.existsById(UUID.randomUUID());

          
        assertThat(exists).isFalse();
    }

    @Test
    void count_ShouldReturnCorrectNumberOfReviews() {
         
        long initialCount = reviewRepository.count();

        Review newReview = new Review();
        newReview.setSight(sight);
        newReview.setUser(user);
        newReview.setRating(4);
        reviewRepository.save(newReview);
        entityManager.flush();

          
        long newCount = reviewRepository.count();

          
        assertThat(newCount).isEqualTo(initialCount + 1);
    }

    @Test
    void deleteAll_ShouldRemoveAllReviews() {
          
        reviewRepository.deleteAll();
        entityManager.flush();

          
        assertThat(reviewRepository.count()).isZero();
    }
}