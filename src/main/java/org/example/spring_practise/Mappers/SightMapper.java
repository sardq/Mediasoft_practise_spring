package org.example.spring_practise.Mappers;

import org.example.spring_practise.DTO.SightDetailsResponseDTO;
import org.example.spring_practise.DTO.SightRequestDTO;
import org.example.spring_practise.DTO.SightResponseDTO;
import org.example.spring_practise.Entities.Sight;
import org.mapstruct.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

@Mapper(componentModel = "spring")
public interface SightMapper {

    SightDetailsResponseDTO toDetailsDTO(Sight sight);

    default SightResponseDTO toResponseWithDistance(Sight sight, Double distanceMeters) {
        if (sight == null) {
            return null;
        }
        return new SightResponseDTO(
                sight.getId(),
                sight.getName(),
                sight.getCategory(),
                sight.getAverageRating(),
                distanceMeters
        );
    }
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location", expression = "java(createPoint(dto.getLon(), dto.getLat()))")
    @Mapping(target = "averageRating", constant = "0.0")
    @Mapping(target = "reviewsCount", constant = "0")
    Sight toEntity(SightRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location", expression = "java(createPoint(dto.getLon(), dto.getLat()))")
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "reviewsCount", ignore = true)
    void updateEntity(SightRequestDTO dto, @MappingTarget Sight sight);

    default Point createPoint(Double lon, Double lat) {
        if (lon == null || lat == null) return null;
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        return geometryFactory.createPoint(new Coordinate(lon, lat));
    }
}