package org.example.spring_practise.Repositories;

import org.example.spring_practise.DTO.SightProjection;
import org.example.spring_practise.Entities.Sight;
import org.example.spring_practise.Enums.SightCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;


import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SightRepositoryTest {

    @Autowired
    private SightRepository sightRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Sight eiffelTower;
    private Sight louvre;
    private Sight notreDame;
    private Sight sacreCoeur;
    private Sight arcDeTriomphe;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @BeforeEach
    void setUp() {
        eiffelTower = createSight(
                "Эйфелева башня",
                SightCategory.MONUMENT,
                48.8584, 2.2945,
                4.8, 1500
        );

        louvre = createSight(
                "Лувр",
                SightCategory.MUSEUM,
                48.8606, 2.3376,
                4.9, 2000
        );

        notreDame = createSight(
                "Собор Парижской Богоматери",
                SightCategory.MONUMENT,
                48.8529, 2.3499,
                4.7, 800
        );

        sacreCoeur = createSight(
                "Базилика Сакре-Кёр",
                SightCategory.MONUMENT,
                48.8867, 2.3431,
                4.6, 600
        );

        arcDeTriomphe = createSight(
                "Триумфальная арка",
                SightCategory.MONUMENT,
                48.8738, 2.2950,
                4.7, 900
        );

        entityManager.flush();
    }

    private Sight createSight(String name, SightCategory category,
                              double lat, double lon,
                              double avgRating, int reviewsCount) {
        Sight sight = new Sight();
        sight.setName(name);
        sight.setDescription("Description of " + name);
        sight.setCategory(category);
        Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
        sight.setLocation(point);
        sight.setAverageRating(avgRating);
        sight.setReviewsCount(reviewsCount);

        return entityManager.persist(sight);
    }


    @Test
    void findNearbySights_WithDefaultParameters_ShouldReturnSightsWithinRadius() {
        double lat = 48.8584;
        double lon = 2.2945;
        int radius = 5000;
        int limit = 10;

         
        List<SightProjection> results = sightRepository.findNearbySights(
                lat, lon, radius, null, 0.0, "DISTANCE", limit);

          
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(5);
    }

    @Test
    void findNearbySights_WithCategoryFilter_ShouldReturnOnlyMatchingCategory() {
          
        double lat = 48.8584;
        double lon = 2.2945;
        int radius = 5000;
        String category = "MUSEUM";
        int limit = 10;

         
        List<SightProjection> results = sightRepository.findNearbySights(
                lat, lon, radius, category, 0.0, "DISTANCE", limit);

          
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Лувр");
        assertThat(results.get(0).getCategory()).isEqualTo("MUSEUM");
    }

    @Test
    void findNearbySights_WithRatingFilter_ShouldReturnOnlySightsAboveMinRating() {
          
        double lat = 48.8584;
        double lon = 2.2945;
        int radius = 5000;
        double minRating = 4.8;

         
        List<SightProjection> results = sightRepository.findNearbySights(
                lat, lon, radius, null, minRating, "RATING", 10);

          
        assertThat(results).hasSize(2);
        assertThat(results).extracting(SightProjection::getAverageRating)
                .allMatch(rating -> rating >= 4.8);
    }

    @Test
    void findNearbySights_SortedByDistance_ShouldReturnClosestFirst() {
          
        double lat = 48.8584;
        double lon = 2.2945;
        int radius = 5000;

         
        List<SightProjection> results = sightRepository.findNearbySights(
                lat, lon, radius, null, 0.0, "DISTANCE", 10);

          
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getName()).isEqualTo("Эйфелева башня");
        assertThat(results.get(0).getDistanceMeters()).isLessThan(10);

        for (int i = 0; i < results.size() - 1; i++) {
            assertThat(results.get(i).getDistanceMeters())
                    .isLessThanOrEqualTo(results.get(i + 1).getDistanceMeters());
        }
    }

    @Test
    void findNearbySights_SortedByRating_ShouldReturnHighestRatedFirst() {
          
        double lat = 48.8584;
        double lon = 2.2945;
        int radius = 5000;

         
        List<SightProjection> results = sightRepository.findNearbySights(
                lat, lon, radius, null, 0.0, "RATING", 10);

          
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getName()).isEqualTo("Лувр");
        assertThat(results.get(0).getAverageRating()).isEqualTo(4.9);

        for (int i = 0; i < results.size() - 1; i++) {
            assertThat(results.get(i).getAverageRating())
                    .isGreaterThanOrEqualTo(results.get(i + 1).getAverageRating());
        }
    }

    @Test
    void findNearbySights_WithLimit_ShouldReturnOnlyLimitedResults() {
          
        double lat = 48.8584;
        double lon = 2.2945;
        int radius = 5000;
        int limit = 2;

         
        List<SightProjection> results = sightRepository.findNearbySights(
                lat, lon, radius, null, 0.0, "DISTANCE", limit);

          
        assertThat(results).hasSize(limit);
    }

    @Test
    void findNearbySights_WithSmallRadius_ShouldReturnOnlyNearbySights() {
        double lat = 48.8584;
        double lon = 2.2945;
        int radius = 1000;

         
        List<SightProjection> results = sightRepository.findNearbySights(
                lat, lon, radius, null, 0.0, "DISTANCE", 10);

          
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Эйфелева башня");
    }

    @Test
    void findNearbySights_WithLargeRadius_ShouldReturnAllSights() {
          
        double lat = 48.8584;
        double lon = 2.2945;
        int radius = 10000;

         
        List<SightProjection> results = sightRepository.findNearbySights(
                lat, lon, radius, null, 0.0, "DISTANCE", 10);

          
        assertThat(results).hasSize(5);
    }

    @Test
    void findNearbySights_WithNoSightsInRadius_ShouldReturnEmptyList() {
        double lat = 55.7558;
        double lon = 37.6176;
        int radius = 1000;

         
        List<SightProjection> results = sightRepository.findNearbySights(
                lat, lon, radius, null, 0.0, "DISTANCE", 10);

          
        assertThat(results).isEmpty();
    }

    @Test
    void findNearbySights_WithHighMinRating_ShouldReturnOnlyTopRated() {
          
        double lat = 48.8584;
        double lon = 2.2945;
        int radius = 5000;
        double minRating = 4.85;

         
        List<SightProjection> results = sightRepository.findNearbySights(
                lat, lon, radius, null, minRating, "RATING", 10);

          
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Лувр");
    }

    @ParameterizedTest
    @EnumSource(SightCategory.class)
    void findNearbySights_WithDifferentCategories_ShouldFilterCorrectly(SightCategory category) {
          
        double lat = 48.8584;
        double lon = 2.2945;
        int radius = 5000;

         
        List<SightProjection> results = sightRepository.findNearbySights(
                lat, lon, radius, category.name(), 0.0, "DISTANCE", 10);

          
        if (category == SightCategory.MUSEUM) {
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getName()).isEqualTo("Лувр");
        } else if (category == SightCategory.MONUMENT) {
            assertThat(results).hasSize(4);
            assertThat(results).extracting(SightProjection::getCategory)
                    .containsOnly("MONUMENT");
        } else {
            assertThat(results).isEmpty();
        }
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 1.0, 2.0, 3.0, 4.0, 4.5, 4.8, 4.9, 5.0})
    void findNearbySights_WithDifferentMinRatings_ShouldFilterCorrectly(double minRating) {
          
        double lat = 48.8584;
        double lon = 2.2945;
        int radius = 5000;

         
        List<SightProjection> results = sightRepository.findNearbySights(
                lat, lon, radius, null, minRating, "RATING", 10);

          
        assertThat(results).allMatch(sight -> sight.getAverageRating() >= minRating);

        if (minRating <= 4.6) {
            assertThat(results).hasSize(5);
        } else if (minRating <= 4.7) {
            assertThat(results).hasSize(4);
        } else if (minRating <= 4.8) {
            assertThat(results).hasSize(2);
        } else if (minRating <= 4.9) {
            assertThat(results).hasSize(1);
        } else {
            assertThat(results).isEmpty();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 10})
    void findNearbySights_WithDifferentLimits_ShouldRespectLimit(int limit) {
          
        double lat = 48.8584;
        double lon = 2.2945;
        int radius = 5000;

         
        List<SightProjection> results = sightRepository.findNearbySights(
                lat, lon, radius, null, 0.0, "DISTANCE", limit);

          
        assertThat(results).hasSizeLessThanOrEqualTo(limit);
        if (limit >= 5) {
            assertThat(results).hasSize(5);
        } else {
            assertThat(results).hasSize(limit);
        }
    }

    @Test
    void findNearbySights_ShouldReturnCorrectProjectionFields() {
          
        double lat = 48.8584;
        double lon = 2.2945;
        int radius = 5000;

         
        List<SightProjection> results = sightRepository.findNearbySights(
                lat, lon, radius, null, 0.0, "DISTANCE", 10);

          
        SightProjection projection = results.get(0);
        assertThat(projection.getId()).isNotNull();
        assertThat(projection.getName()).isNotNull();
        assertThat(projection.getCategory()).isNotNull();
        assertThat(projection.getAverageRating()).isNotNull();
        assertThat(projection.getDistanceMeters()).isNotNull();
    }



    @Test
    void findNearbySights_WithNullCategory_ShouldIgnoreCategoryFilter() {
          
        double lat = 48.8584;
        double lon = 2.2945;
        int radius = 5000;

         
        List<SightProjection> resultsWithNull = sightRepository.findNearbySights(
                lat, lon, radius, null, 0.0, "DISTANCE", 10);

        List<SightProjection> resultsWithAny = sightRepository.findNearbySights(
                lat, lon, radius, "MONUMENT", 0.0, "DISTANCE", 10);

          
        assertThat(resultsWithNull).hasSize(5);
        assertThat(resultsWithAny).hasSize(4);
    }

    @Test
    void findNearbySights_WithExactLocation_ShouldReturnZeroDistance() {
          
        double lat = 48.8584;
        double lon = 2.2945;
        int radius = 100;

         
        List<SightProjection> results = sightRepository.findNearbySights(
                lat, lon, radius, null, 0.0, "DISTANCE", 10);

          
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Эйфелева башня");
        assertThat(results.get(0).getDistanceMeters()).isLessThan(1);
    }

    @Test
    void findNearbySights_WithZeroRadius_ShouldReturnOnlyExactLocation() {
          
        double lat = 48.8584;
        double lon = 2.2945;
        int radius = 0;

         
        List<SightProjection> results = sightRepository.findNearbySights(
                lat, lon, radius, null, 0.0, "DISTANCE", 10);

          
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Эйфелева башня");
    }

    @Test
    void findNearbySights_WithNegativeRadius_ShouldReturnEmpty() {
          
        double lat = 48.8584;
        double lon = 2.2945;
        int radius = -1;

         
        List<SightProjection> results = sightRepository.findNearbySights(
                lat, lon, radius, null, 0.0, "DISTANCE", 10);

          
        assertThat(results).isEmpty();
    }

    @Test
    void findNearbySights_WithInvalidCoordinates_ShouldHandleGracefully() {
          
        double lat = 1000.0;
        double lon = 1000.0;
        int radius = 5000;

         
        List<SightProjection> results = sightRepository.findNearbySights(
                lat, lon, radius, null, 0.0, "DISTANCE", 10);

          
        assertThat(results).isEmpty();
    }

    @Test
    void findNearbySights_WithDifferentSortBy_ShouldReturnCorrectOrder() {
          
        double lat = 48.8584;
        double lon = 2.2945;
        int radius = 5000;

         
        List<SightProjection> byDistance = sightRepository.findNearbySights(
                lat, lon, radius, null, 0.0, "DISTANCE", 10);

        List<SightProjection> byRating = sightRepository.findNearbySights(
                lat, lon, radius, null, 0.0, "RATING", 10);

          
        assertThat(byDistance.get(0).getName()).isEqualTo("Эйфелева башня");
        assertThat(byRating.get(0).getName()).isEqualTo("Лувр");
    }



    @Test
    void save_ShouldPersistSight() {
          
        Sight newSight = createSight(
                "Новая достопримечательность",
                SightCategory.PARK,
                48.8500, 2.3000,
                0.0, 0
        );

         
        Sight savedSight = sightRepository.save(newSight);
        entityManager.flush();

          
        assertThat(savedSight.getId()).isNotNull();

        Sight foundSight = entityManager.find(Sight.class, savedSight.getId());
        assertThat(foundSight).isNotNull();
        assertThat(foundSight.getName()).isEqualTo("Новая достопримечательность");
    }

    @Test
    void update_ShouldModifySight() {
          
        eiffelTower.setName("Обновленное название");
        eiffelTower.setAverageRating(5.0);

         
        Sight updatedSight = sightRepository.save(eiffelTower);
        entityManager.flush();
        entityManager.clear();

          
        Sight foundSight = entityManager.find(Sight.class, updatedSight.getId());
        assertThat(foundSight.getName()).isEqualTo("Обновленное название");
        assertThat(foundSight.getAverageRating()).isEqualTo(5.0);
    }

    @Test
    void delete_ShouldRemoveSight() {
          
        UUID id = eiffelTower.getId();

         
        sightRepository.deleteById(id);
        entityManager.flush();

          
        Sight foundSight = entityManager.find(Sight.class, id);
        assertThat(foundSight).isNull();
    }

    @Test
    void findById_ShouldReturnSight() {
         
        java.util.Optional<Sight> found = sightRepository.findById(eiffelTower.getId());

          
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Эйфелева башня");
    }

    @Test
    void existsById_ShouldReturnTrueForExistingSight() {
         
        boolean exists = sightRepository.existsById(eiffelTower.getId());

          
        assertThat(exists).isTrue();
    }

    @Test
    void findAll_ShouldReturnAllSights() {
         
        List<Sight> allSights = sightRepository.findAll();

          
        assertThat(allSights).hasSize(5);
    }

    @Test
    void count_ShouldReturnCorrectNumberOfSights() {
         
        long count = sightRepository.count();

          
        assertThat(count).isEqualTo(5);
    }
}