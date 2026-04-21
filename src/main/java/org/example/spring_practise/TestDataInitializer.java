package org.example.spring_practise;

import lombok.RequiredArgsConstructor;
import org.example.spring_practise.Entities.Review;
import org.example.spring_practise.Entities.Sight;
import org.example.spring_practise.Entities.User;
import org.example.spring_practise.Enums.SightCategory;
import org.example.spring_practise.Repositories.ReviewRepository;
import org.example.spring_practise.Repositories.SightRepository;
import org.example.spring_practise.Repositories.UserRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TestDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SightRepository sightRepository;
    private final ReviewRepository reviewRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        List<User> users = new ArrayList<>();
        users.add(userRepository.save(new User(null, "ivan_traveler", "ivan@mail.com")));
        users.add(userRepository.save(new User(null, "mariya_explorer", "mariya@mail.com")));
        users.add(userRepository.save(new User(null, "alex_guide", "alex@mail.com")));
        users.add(userRepository.save(new User(null, "sergey_city", "sergey@mail.com")));
        users.add(userRepository.save(new User(null, "elena_best", "elena@mail.com")));

        List<Sight> sights = new ArrayList<>();

        sights.add(createSight("Красная площадь", SightCategory.MONUMENT, "Главная площадь страны", 55.7539, 37.6208));

        sights.add(createSight("Государственный Исторический музей", SightCategory.MUSEUM, "Крупнейший национальный музей", 55.7553, 37.6178));

        sights.add(createSight("Парк Горького", SightCategory.PARK, "Главный парк столицы", 55.7297, 37.6015));

        sights.add(createSight("Большой театр", SightCategory.THEATER, "Один из крупнейших театров в мире", 55.7602, 37.6186));

        sights.add(createSight("Кафе Пушкин", SightCategory.RESTAURANT, "Легендарный ресторан русской кухни", 55.7638, 37.6045));

        List<Sight> savedSights = sightRepository.saveAll(sights);


        reviewRepository.save(new Review(null, savedSights.get(0), users.get(0), 5, "Обязательно к посещению!", LocalDateTime.now()));
        reviewRepository.save(new Review(null, savedSights.get(0), users.get(1), 4, "Очень много людей, но красиво.", LocalDateTime.now()));
        updateSightStats(savedSights.get(0), 4.5, 2);

        reviewRepository.save(new Review(null, savedSights.get(1), users.get(2), 5, "Потрясающая коллекция экспонатов.", LocalDateTime.now()));
        updateSightStats(savedSights.get(1), 5.0, 1);

        reviewRepository.save(new Review(null, savedSights.get(2), users.get(3), 5, "Лучшее место для прогулок и катания на самокатах.", LocalDateTime.now()));
        reviewRepository.save(new Review(null, savedSights.get(2), users.get(4), 5, null, LocalDateTime.now()));
        updateSightStats(savedSights.get(2), 5.0, 2);

        System.out.println("Тестовые данные успешно загружены!");
    }

    private Sight createSight(String name, SightCategory category, String desc, double lat, double lon) {
        Sight sight = new Sight();
        sight.setName(name);
        sight.setCategory(category);
        sight.setDescription(desc);
        Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
        sight.setLocation(point);
        sight.setAverageRating(0.0);
        sight.setReviewsCount(0);
        return sight;
    }

    private void updateSightStats(Sight sight, double avg, int count) {
        sight.setAverageRating(avg);
        sight.setReviewsCount(count);
        sightRepository.save(sight);
    }
}