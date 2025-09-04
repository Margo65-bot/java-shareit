package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    private final Long userId = 1L;

    @Test
    void create_ValidRequest_ShouldReturnCreatedUser() throws Exception {
        UserDto createDto = UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();

        UserDto responseDto = UserDto.builder()
                .id(userId)
                .name("John Doe")
                .email("john@example.com")
                .build();

        when(userService.create(any(UserDto.class)))
                .thenReturn(responseDto);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void getById_ValidRequest_ShouldReturnUser() throws Exception {
        UserDto responseDto = UserDto.builder()
                .id(userId)
                .name("John Doe")
                .email("john@example.com")
                .build();

        when(userService.getById(userId))
                .thenReturn(responseDto);

        mvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void getAll_ShouldReturnAllUsers() throws Exception {
        UserDto user1 = UserDto.builder().id(1L).name("User1").email("user1@example.com").build();
        UserDto user2 = UserDto.builder().id(2L).name("User2").email("user2@example.com").build();

        when(userService.getAll())
                .thenReturn(List.of(user1, user2));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("User1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("User2"));
    }

    @Test
    void getAll_WithEmptyResult_ShouldReturnEmptyList() throws Exception {
        when(userService.getAll())
                .thenReturn(List.of());

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void update_ValidRequest_ShouldReturnUpdatedUser() throws Exception {
        UserDto updateDto = UserDto.builder()
                .name("John Updated")
                .email("john.updated@example.com")
                .build();

        UserDto responseDto = UserDto.builder()
                .id(userId)
                .name("John Updated")
                .email("john.updated@example.com")
                .build();

        when(userService.update(eq(userId), any(UserDto.class)))
                .thenReturn(responseDto);

        mvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));
    }

    @Test
    void update_WithOnlyName_ShouldReturnUpdatedUser() throws Exception {
        UserDto updateDto = UserDto.builder()
                .name("John Updated")
                .build();

        UserDto responseDto = UserDto.builder()
                .id(userId)
                .name("John Updated")
                .email("original@example.com")
                .build();

        when(userService.update(eq(userId), any(UserDto.class)))
                .thenReturn(responseDto);

        mvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.email").value("original@example.com"));
    }

    @Test
    void update_WithOnlyEmail_ShouldReturnUpdatedUser() throws Exception {
        UserDto updateDto = UserDto.builder()
                .email("updated@example.com")
                .build();

        UserDto responseDto = UserDto.builder()
                .id(userId)
                .name("Original Name")
                .email("updated@example.com")
                .build();

        when(userService.update(eq(userId), any(UserDto.class)))
                .thenReturn(responseDto);

        mvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Original Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void update_WithBlankName_ShouldUseOriginalName() throws Exception {
        UserDto updateDto = UserDto.builder()
                .name("   ")
                .email("updated@example.com")
                .build();

        UserDto responseDto = UserDto.builder()
                .id(userId)
                .name("Original Name")
                .email("updated@example.com")
                .build();

        when(userService.update(eq(userId), any(UserDto.class)))
                .thenReturn(responseDto);

        mvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Original Name"));
    }

    @Test
    void update_WithBlankEmail_ShouldUseOriginalEmail() throws Exception {
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email("   ")
                .build();

        UserDto responseDto = UserDto.builder()
                .id(userId)
                .name("Updated Name")
                .email("original@example.com")
                .build();

        when(userService.update(eq(userId), any(UserDto.class)))
                .thenReturn(responseDto);

        mvc.perform(patch("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("original@example.com"));
    }
}