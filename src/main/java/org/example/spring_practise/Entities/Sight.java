package org.example.spring_practise.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.spring_practise.Enums.SightCategory;
import org.locationtech.jts.geom.Point;

import java.util.UUID;

@Entity
@Table(name ="Sights")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sight {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Enumerated(EnumType.STRING)
    SightCategory category;
    private String name;
    private String description;
    @Column(columnDefinition = "geometry(Point,4326)")
    private Point location;
    @Column(name ="average_rating")
    private Double averageRating = 0.0;
    @Column(name = "reviews_count")
    private Integer reviewsCount = 0;
}
