package org.example.spring_practise.Repositories;

import lombok.NonNull;
import org.example.spring_practise.Entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    @NonNull
    Page<Review> findBySightId(UUID sightId, @NonNull Pageable pageable);
    List<Review> findBySightId(UUID sightId);
    List<Review> findByUserId(UUID userId);
    Optional<Review> findBySightIdAndUserId(UUID sightId, UUID userId);
}