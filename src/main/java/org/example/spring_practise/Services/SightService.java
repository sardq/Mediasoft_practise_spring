package org.example.spring_practise.Services;

import lombok.RequiredArgsConstructor;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SightService {

    private final SightRepository sightRepository;
    private final SightMapper sightMapper;
    private final ReviewRepository reviewRepository;

    public List<SightResponseDTO> getNearbySights(double lat, double lon, int radius,
                                                  SightCategory category, double minRating,
                                                  SortBy sortBy, int limit) {
        String categoryStr = category != null ? category.name() : null;
        List<SightProjection> results = sightRepository.findNearbySights(
                lat, lon, radius, categoryStr, minRating, sortBy.name(), limit);

        return results.stream()
                .map(res -> new SightResponseDTO(
                        res.getId(),
                        res.getName(),
                        SightCategory.valueOf(res.getCategory()),
                        res.getAverageRating(),
                        res.getDistanceMeters()
                ))
                .toList();
    }

    public SightDetailsResponseDTO getById(UUID id) {
        Sight sight = sightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Достопримечательность не найдена"));
        return sightMapper.toDetailsDTO(sight);
    }
    public SightDetailsResponseDTO create(SightRequestDTO dto) {
        Sight sight = sightMapper.toEntity(dto);
        return sightMapper.toDetailsDTO(sightRepository.save(sight));
    }

    public SightDetailsResponseDTO update(UUID id, SightRequestDTO dto) {
        Sight existing = sightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Достопримечательность не найдена"));

        sightMapper.updateEntity(dto, existing);
        return sightMapper.toDetailsDTO(sightRepository.save(existing));
    }

    public void remove(UUID id) {
        if (!sightRepository.existsById(id)) {
            throw new RuntimeException("Достопримечательность не найдена");
        }
        List<Review> reviews = reviewRepository.findBySightId(id);
        reviewRepository.deleteAll(reviews);

        sightRepository.deleteById(id);
    }
    public Page<SightResponseDTO> findAll(int page, int size) {
        return sightRepository.findAll(PageRequest.of(page, size))
                .map(sight -> sightMapper.toResponseWithDistance(sight, null));
    }
}
