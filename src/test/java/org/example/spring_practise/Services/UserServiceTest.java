package org.example.spring_practise.Services;

import org.example.spring_practise.DTO.UserRequestDTO;
import org.example.spring_practise.DTO.UserResponseDTO;
import org.example.spring_practise.Entities.Review;
import org.example.spring_practise.Entities.User;
import org.example.spring_practise.Mappers.UserMapper;
import org.example.spring_practise.Repositories.ReviewRepository;
import org.example.spring_practise.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewService reviewService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User user;
    private UserRequestDTO userRequestDTO;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setUsername("john_doe");
        user.setEmail("john@example.com");

        userRequestDTO = new UserRequestDTO("john_doe", "john@example.com");
        userResponseDTO = new UserResponseDTO(userId, "john_doe", "john@example.com");

    }


    @Test
    void findAll_ShouldReturnPageOfUserResponseDTO() {
         
        Page<User> userPage = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

         
        Page<UserResponseDTO> result = userService.findAll(0, 20);

         
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(userId);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("john_doe");
        assertThat(result.getContent().get(0).getEmail()).isEqualTo("john@example.com");

        verify(userRepository, times(1)).findAll(PageRequest.of(0, 20));
        verify(userMapper, times(1)).toDTO(user);
    }

    @Test
    void findAll_WithEmptyPage_ShouldReturnEmptyPage() {
         
        Page<User> emptyPage = Page.empty();
        when(userRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

         
        Page<UserResponseDTO> result = userService.findAll(0, 20);

         
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findAll(PageRequest.of(0, 20));
        verify(userMapper, never()).toDTO(any());
    }

    @Test
    void findAll_WithCustomPagination_ShouldUseCorrectPageable() {
         
        when(userRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

         
        userService.findAll(3, 50);

         
        verify(userRepository, times(1)).findAll(PageRequest.of(3, 50));
    }






    @Test
    void getById_WhenUserExists_ShouldReturnUserResponseDTO() {
         
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

         
        UserResponseDTO result = userService.getById(userId);

         
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("john_doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");


        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).toDTO(user);
    }

    @Test
    void getById_WhenUserNotFound_ShouldThrowException() {
         
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Пользователь не найден");

        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, never()).toDTO(any());
    }

    @Test
    void create_WithValidData_ShouldSaveAndReturnUserResponseDTO() {
         
        when(userRepository.existsByUsername(userRequestDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(userRequestDTO.getEmail())).thenReturn(false);
        when(userMapper.toEntity(userRequestDTO)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

         
        UserResponseDTO result = userService.create(userRequestDTO);

         
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("john_doe");

        verify(userRepository, times(1)).existsByUsername("john_doe");
        verify(userRepository, times(1)).existsByEmail("john@example.com");
        verify(userMapper, times(1)).toEntity(userRequestDTO);
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).toDTO(user);
    }

    @Test
    void create_WhenUsernameAlreadyExists_ShouldThrowException() {
         
        when(userRepository.existsByUsername(userRequestDTO.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(userRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Пользователь с таким email или username уже существует");

        verify(userRepository, times(1)).existsByUsername("john_doe");
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toEntity(any());
    }

    @Test
    void create_WhenEmailAlreadyExists_ShouldThrowException() {
         
        when(userRepository.existsByUsername(userRequestDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(userRequestDTO.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(userRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Пользователь с таким email или username уже существует");

        verify(userRepository, times(1)).existsByUsername("john_doe");
        verify(userRepository, times(1)).existsByEmail("john@example.com");
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toEntity(any());
    }

    @Test
    void update_WhenUserExists_ShouldUpdateAndReturnUserResponseDTO() {
         
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userMapper).updateEntity(userRequestDTO, user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

         
        UserResponseDTO result = userService.update(userId, userRequestDTO);

         
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);

        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).updateEntity(userRequestDTO, user);
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).toDTO(user);
    }

    @Test
    void update_WhenUserNotFound_ShouldThrowException() {
         
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(userId, userRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Пользователь не найден");

        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, never()).updateEntity(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_WithSameData_ShouldWork() {
         
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userMapper).updateEntity(userRequestDTO, user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

         
        UserResponseDTO result = userService.update(userId, userRequestDTO);

         
        assertThat(result).isNotNull();
        verify(userMapper, times(1)).updateEntity(userRequestDTO, user);
    }



    @Test
    void remove_WhenUserExists_ShouldDeleteUserAndRelatedReviews() {
         
        UUID reviewId1 = UUID.randomUUID();
        UUID reviewId2 = UUID.randomUUID();

        Review review1 = new Review();
        review1.setId(reviewId1);
        Review review2 = new Review();
        review2.setId(reviewId2);

        List<Review> reviews = Arrays.asList(review1, review2);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(reviewRepository.findByUserId(userId)).thenReturn(reviews);
        doNothing().when(reviewService).remove(reviewId1);
        doNothing().when(reviewService).remove(reviewId2);
        doNothing().when(userRepository).deleteById(userId);

         
        userService.remove(userId);

         
        verify(userRepository, times(1)).existsById(userId);
        verify(reviewRepository, times(1)).findByUserId(userId);
        verify(reviewService, times(1)).remove(reviewId1);
        verify(reviewService, times(1)).remove(reviewId2);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void remove_WhenUserExistsWithNoReviews_ShouldDeleteUserOnly() {
         
        when(userRepository.existsById(userId)).thenReturn(true);
        when(reviewRepository.findByUserId(userId)).thenReturn(List.of());
        doNothing().when(userRepository).deleteById(userId);

         
        userService.remove(userId);

         
        verify(userRepository, times(1)).existsById(userId);
        verify(reviewRepository, times(1)).findByUserId(userId);
        verify(reviewService, never()).remove(any());
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void remove_WhenUserNotFound_ShouldThrowException() {
         
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> userService.remove(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Пользователь не найден");

        verify(userRepository, times(1)).existsById(userId);
        verify(reviewRepository, never()).findByUserId(any());
        verify(reviewService, never()).remove(any());
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void remove_WithMultipleReviews_ShouldDeleteAllReviews() {
         
        List<Review> reviews = Arrays.asList(
                mock(Review.class),
                mock(Review.class),
                mock(Review.class),
                mock(Review.class)
        );

        when(userRepository.existsById(userId)).thenReturn(true);
        when(reviewRepository.findByUserId(userId)).thenReturn(reviews);
        doNothing().when(reviewService).remove(any());

         
        userService.remove(userId);

         
        verify(reviewService, times(4)).remove(any());
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void remove_ShouldHandleExceptionDuringReviewDeletion() {
         
        UUID reviewId = UUID.randomUUID();
        Review review = new Review();
        review.setId(reviewId);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(reviewRepository.findByUserId(userId)).thenReturn(List.of(review));
        doThrow(new RuntimeException("Review deletion failed"))
                .when(reviewService).remove(reviewId);

        assertThatThrownBy(() -> userService.remove(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Review deletion failed");

        verify(userRepository, times(1)).existsById(userId);
        verify(reviewRepository, times(1)).findByUserId(userId);
        verify(reviewService, times(1)).remove(reviewId);
        verify(userRepository, never()).deleteById(userId);
    }


    @Test
    void create_WithUsernameWithSpaces_ShouldWork() {
        userRequestDTO = new UserRequestDTO("john_doe_123", "john@example.com");

        when(userRepository.existsByUsername("john_doe_123")).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(userRequestDTO)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

         
        UserResponseDTO result = userService.create(userRequestDTO);

         
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).existsByUsername("john_doe_123");
    }

    @Test
    void create_WithEmailWithPlusSign_ShouldWork() {
        userRequestDTO = new UserRequestDTO("john_doe", "john+test@example.com");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("john+test@example.com")).thenReturn(false);
        when(userMapper.toEntity(userRequestDTO)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

         
        UserResponseDTO result = userService.create(userRequestDTO);

         
        assertThat(result).isNotNull();
        verify(userRepository, times(1)).existsByEmail("john+test@example.com");
    }

    @Test
    void update_WithUsernameChange_ShouldWork() {
         
        UserRequestDTO updateDTO = new UserRequestDTO("new_username", "john@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userMapper).updateEntity(updateDTO, user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userResponseDTO);

         
        UserResponseDTO result = userService.update(userId, updateDTO);

         
        assertThat(result).isNotNull();
        verify(userMapper, times(1)).updateEntity(updateDTO, user);
    }

    @Test
    void remove_ShouldBeIdempotent() {
         
        when(userRepository.existsById(userId)).thenReturn(true);
        when(reviewRepository.findByUserId(userId)).thenReturn(List.of());
        doNothing().when(userRepository).deleteById(userId);

         
        userService.remove(userId);

        userService.remove(userId);

        verify(userRepository, times(2)).existsById(userId);
        verify(userRepository, times(2)).deleteById(userId);
    }

    @Test
    void findAll_WithLargePageSize_ShouldWork() {

        when(userRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());


        userService.findAll(0, 1000);


        verify(userRepository, times(1)).findAll(PageRequest.of(0, 1000));
    }
}