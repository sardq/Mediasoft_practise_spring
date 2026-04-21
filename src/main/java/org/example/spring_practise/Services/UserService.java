package org.example.spring_practise.Services;

import lombok.RequiredArgsConstructor;
import org.example.spring_practise.DTO.UserRequestDTO;
import org.example.spring_practise.DTO.UserResponseDTO;
import org.example.spring_practise.Entities.User;
import org.example.spring_practise.Mappers.UserMapper;
import org.example.spring_practise.Repositories.ReviewRepository;
import org.example.spring_practise.Repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewService reviewService;
    private final UserMapper userMapper;

    public Page<UserResponseDTO> findAll(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size))
                .map(userMapper::toDTO);
    }

    public UserResponseDTO getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return userMapper.toDTO(user);
    }

    public UserResponseDTO create(UserRequestDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername()) || userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Пользователь с таким email или username уже существует");
        }
        User saved = userRepository.save(userMapper.toEntity(dto));
        return userMapper.toDTO(saved);
    }

    public UserResponseDTO update(UUID id, UserRequestDTO dto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        userMapper.updateEntity(dto, existing);
        return userMapper.toDTO(userRepository.save(existing));
    }

    public void remove(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Пользователь не найден");
        }
        reviewRepository.findByUserId(id).forEach(review -> {
            reviewService.remove(review.getId());
        });
        userRepository.deleteById(id);
    }
}