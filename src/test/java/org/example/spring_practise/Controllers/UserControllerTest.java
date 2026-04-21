package org.example.spring_practise.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.spring_practise.DTO.UserRequestDTO;
import org.example.spring_practise.DTO.UserResponseDTO;
import org.example.spring_practise.Services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UUID userId;
    private UserResponseDTO userResponseDTO;
    private UserRequestDTO userRequestDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        userResponseDTO = new UserResponseDTO(userId, "john_doe", "john@example.com");

        userRequestDTO = new UserRequestDTO("john_doe","john@example.com");
    }

    @Test
    void getAllUsers_WithDefaultPagination_ShouldUseDefaults() throws Exception {
        when(userService.findAll(eq(0), eq(20))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());

        verify(userService, times(1)).findAll(0, 20);
    }

    @Test
    void getAllUsers_WithCustomPagination_ShouldPassParameters() throws Exception {
        when(userService.findAll(eq(3), eq(50))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/users")
                        .param("page", "3")
                        .param("size", "50"))
                .andExpect(status().isOk());

        verify(userService, times(1)).findAll(3, 50);
    }

    @Test
    void getAllUsers_WithNegativePage_ShouldStillPassToService() throws Exception {
        when(userService.findAll(eq(-1), eq(20))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/users")
                        .param("page", "-1"))
                .andExpect(status().isOk());

        verify(userService, times(1)).findAll(-1, 20);
    }

    @Test
    void getAllUsers_WithZeroSize_ShouldPassToService() throws Exception {
        when(userService.findAll(eq(0), eq(0))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/users")
                        .param("size", "0"))
                .andExpect(status().isOk());

        verify(userService, times(1)).findAll(0, 0);
    }

    @Test
    void getAllUsers_WithLargeSize_ShouldPassToService() throws Exception {
        when(userService.findAll(eq(0), eq(1000))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/users")
                        .param("size", "1000"))
                .andExpect(status().isOk());

        verify(userService, times(1)).findAll(0, 1000);
    }

    @Test
    void getAllUsers_WhenNoUsersExist_ShouldReturnEmptyPage() throws Exception {
        Page<UserResponseDTO> emptyPage = Page.empty();
        when(userService.findAll(eq(0), eq(20))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(userService, times(1)).findAll(0, 20);
    }


    @Test
    void getUser_WithInvalidUUID_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/users/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).getById(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "123e4567-e89b-12d3-a456-426614174000",
            "123e4567-e89b-12d3-a456-426614174001",
            "123e4567-e89b-12d3-a456-426614174002"
    })
    void getUser_WithDifferentValidUUIDs_ShouldWork(String uuidString) throws Exception {
        UUID uuid = UUID.fromString(uuidString);
        UserResponseDTO user = new UserResponseDTO(uuid,"test_user", "test_email");

        when(userService.getById(uuid)).thenReturn(user);

        mockMvc.perform(get("/api/users/{id}", uuidString))
                .andExpect(status().isOk());

        verify(userService, times(1)).getById(uuid);
    }


    @Test
    void createUser_WithValidData_ShouldReturnCreated() throws Exception {
        when(userService.create(any(UserRequestDTO.class))).thenReturn(userResponseDTO);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService, times(1)).create(any(UserRequestDTO.class));
    }

    @Test
    void createUser_WithMissingUsername_ShouldReturnBadRequest() throws Exception {
        userRequestDTO = new UserRequestDTO(null,"john@example.com");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any());
    }

    @Test
    void createUser_WithBlankUsername_ShouldReturnBadRequest() throws Exception {
        userRequestDTO = new UserRequestDTO("","john@example.com");


        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any());
    }

    @Test
    void createUser_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        userRequestDTO = new UserRequestDTO("john_doe","invalid-email");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any());
    }

    @Test
    void createUser_WithMissingEmail_ShouldReturnBadRequest() throws Exception {
        userRequestDTO = new UserRequestDTO("john_doe",null);
        ;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any());
    }


    @Test
    void createUser_WithVeryLongFields_ShouldWork() throws Exception {
        String longText = "A".repeat(255);
        userRequestDTO = new UserRequestDTO(longText,"john@example.com");


        when(userService.create(any(UserRequestDTO.class))).thenReturn(userResponseDTO);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isOk());

        verify(userService, times(1)).create(any(UserRequestDTO.class));
    }


    @Test
    void updateUser_WithValidData_ShouldReturnUpdated() throws Exception {
        when(userService.update(eq(userId), any(UserRequestDTO.class)))
                .thenReturn(userResponseDTO);

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService, times(1)).update(eq(userId), any(UserRequestDTO.class));
    }

    @Test
    void updateUser_WithPartialData_ShouldWork() throws Exception {
        UserRequestDTO partialUpdate = new UserRequestDTO("john_updated","john.updated@example.com" );


        UserResponseDTO updatedUser = new UserResponseDTO(userId, "john_updated","john.updated@example.com");

        when(userService.update(eq(userId), any(UserRequestDTO.class)))
                .thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john_updated"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));

        verify(userService, times(1)).update(eq(userId), any(UserRequestDTO.class));
    }

    @Test
    void deleteUser_WithValidId_ShouldReturnOk() throws Exception {
        doNothing().when(userService).remove(userId);

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isOk());

        verify(userService, times(1)).remove(userId);
    }


    @Test
    void deleteUser_WithInvalidUUID_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).remove(any());
    }

    @Test
    void deleteUser_ShouldReturnOkEvenWhenUserExists() throws Exception {
        doNothing().when(userService).remove(userId);

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isOk());

        verify(userService, times(1)).remove(userId);
    }


    @Test
    void updateUser_WithEmptyRequestBody_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).update(any(), any());
    }
}