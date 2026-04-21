package org.example.spring_practise.DTO;

import java.util.UUID;

public interface SightProjection {
    UUID getId();
    String getName();
    String getCategory();
    Double getAverageRating();
    Double getDistanceMeters();
}