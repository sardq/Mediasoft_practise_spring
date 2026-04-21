package org.example.spring_practise.Mappers;

import org.example.spring_practise.DTO.ReviewResponseDTO;
import org.example.spring_practise.Entities.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "userId", source = "user.id")
    ReviewResponseDTO toDTO(Review review);
}