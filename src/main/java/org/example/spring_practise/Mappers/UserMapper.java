package org.example.spring_practise.Mappers;

import org.example.spring_practise.DTO.UserRequestDTO;
import org.example.spring_practise.DTO.UserResponseDTO;
import org.example.spring_practise.Entities.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDTO toDTO(User user);

    User toEntity(UserRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntity(UserRequestDTO dto, @MappingTarget User user);
}
