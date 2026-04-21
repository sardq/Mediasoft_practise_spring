package org.example.spring_practise.Services;

import lombok.RequiredArgsConstructor;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final SightRepository sightRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    public List<ReviewResponseDTO> getReviewsBySightId(UUID sightId, int page, int size) {
        Page<Review> reviews = reviewRepository.findBySightId(sightId, PageRequest.of(page, size));
        return reviews.stream().map(reviewMapper::toDTO).toList();
    }
    public Page<ReviewResponseDTO> findAll(int page, int size) {
        return reviewRepository.findAll(PageRequest.of(page, size))
                .map(reviewMapper::toDTO);
    }
    public void saveOrUpdateRating(UUID sightId, RatingRequestDTO dto) {
        Sight sight = sightRepository.findById(sightId)
                .orElseThrow(() -> new RuntimeException("Достопримечательность не найдена"));

        Optional<Review> existingReview = reviewRepository.findBySightIdAndUserId(sightId, dto.getUserId());

        if (existingReview.isPresent()) {
            Review review = existingReview.get();
            Integer oldRating = review.getRating();
            review.setRating(dto.getRating());
            reviewRepository.save(review);

            recalculateAverageRating(sight, oldRating, dto.getRating());
        } else {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            Review review = new Review();
            review.setSight(sight);
            review.setUser(user);
            review.setRating(dto.getRating());
            reviewRepository.save(review);

            recalculateAverageRating(sight, null, dto.getRating());
        }
    }

    public void saveOrUpdateText(UUID sightId, ReviewTextRequestDTO dto) {
        Sight sight = sightRepository.findById(sightId)
                .orElseThrow(() -> new RuntimeException("Достопримечательность не найдена"));

        Optional<Review> existingReview = reviewRepository.findBySightIdAndUserId(sightId, dto.getUserId());

        if (existingReview.isPresent()) {
            Review review = existingReview.get();
            review.setText(dto.getText());
            reviewRepository.save(review);
        } else {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
            Review review = new Review();
            review.setSight(sight);
            review.setUser(user);
            review.setText(dto.getText());
            reviewRepository.save(review);
        }
    }

    private void recalculateAverageRating(Sight sight, Integer oldRating, Integer newRating) {
        double currentTotal = sight.getAverageRating() * sight.getReviewsCount();

        if (oldRating == null) {
            sight.setReviewsCount(sight.getReviewsCount() + 1);
            sight.setAverageRating((currentTotal + newRating) / sight.getReviewsCount());
        } else {
            sight.setAverageRating((currentTotal - oldRating + newRating) / sight.getReviewsCount());
        }
        sightRepository.save(sight);
    }

    public ReviewResponseDTO getById(UUID id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден"));
        return reviewMapper.toDTO(review);
    }
    public void remove(UUID id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Отзыв не найден"));

        Sight sight = review.getSight();

        if (review.getRating() != null) {
            double currentTotal = sight.getAverageRating() * sight.getReviewsCount();
            sight.setReviewsCount(sight.getReviewsCount() - 1);

            if (sight.getReviewsCount() == 0) {
                sight.setAverageRating(0.0);
            } else {
                sight.setAverageRating((currentTotal - review.getRating()) / sight.getReviewsCount());
            }
            sightRepository.save(sight);
        }

        reviewRepository.delete(review);
    }
}
