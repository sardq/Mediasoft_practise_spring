package org.example.spring_practise.Repositories;

import org.example.spring_practise.DTO.SightProjection;
import org.example.spring_practise.Entities.Sight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SightRepository extends JpaRepository<Sight, UUID> {
    @Query(value = """
        SELECT 
            s.id as id, 
            s.name as name, 
            s.category as category, 
            s.average_rating as averageRating, 
            ST_Distance(s.location, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography) as distanceMeters
        FROM sights s
        WHERE ST_DWithin(s.location, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, :radius)
        AND (:category IS NULL OR s.category = :category)
        AND s.average_rating >= :minRating
        ORDER BY 
            CASE WHEN :sortBy = 'DISTANCE' THEN ST_Distance(s.location, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography) END ASC,
            CASE WHEN :sortBy = 'RATING' THEN s.average_rating END DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<SightProjection> findNearbySights(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radius") int radius,
            @Param("category") String category,
            @Param("minRating") double minRating,
            @Param("sortBy") String sortBy,
            @Param("limit") int limit
    );
}

