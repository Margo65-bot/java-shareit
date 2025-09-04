package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.client.HttpClient;
import ru.practicum.shareit.user.dto.UserDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private HttpClient httpClient;

    @Value("${shareit.api.auth.userheader}")
    private String userIdHeader;

    private final ResponseEntity<Object> mockResponse = ResponseEntity.ok().body("{\"id\": 1}");

    @Test
    void getAll_ShouldReturnOk() throws Exception {
        when(httpClient.get(eq("/users"), eq(null)))
                .thenReturn(mockResponse);

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1}"));
    }

    @Test
    void getById_ShouldReturnOk() throws Exception {
        long userId = 1L;
        when(httpClient.get(eq("/users/1"), eq(null)))
                .thenReturn(mockResponse);

        mvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1}"));
    }

    @Test
    void getById_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        long invalidUserId = 0L;
        mvc.perform(get("/users/{id}", invalidUserId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_WithNegativeId_ShouldReturnBadRequest() throws Exception {
        long negativeUserId = -1L;
        mvc.perform(get("/users/{id}", negativeUserId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteById_ShouldReturnOk() throws Exception {
        long userId = 1L;
        when(httpClient.delete(eq("/users/1"), eq(null)))
                .thenReturn(mockResponse);

        mvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk());
    }

    @Test
    void deleteById_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        long invalidUserId = 0L;
        mvc.perform(delete("/users/{id}", invalidUserId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_ShouldReturnOk() throws Exception {
        UserDto userDto = UserDto.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .build();

        when(httpClient.post(eq("/users"), eq(null), any(UserDto.class)))
                .thenReturn(mockResponse);

        mvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1}"));
    }

    @Test
    void create_WithFullUserDto_ShouldReturnOk() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .build();

        when(httpClient.post(eq("/users"), eq(null), any(UserDto.class)))
                .thenReturn(mockResponse);

        mvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());
    }

    @Test
    void create_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        UserDto userDto = UserDto.builder()
                .name("John Doe")
                .email("invalid-email")
                .build();

        mvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithLongName_ShouldReturnBadRequest() throws Exception {
        UserDto userDto = UserDto.builder()
                .name("A".repeat(101))
                .email("john.doe@example.com")
                .build();

        mvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithEmptyName_ShouldReturnOk() throws Exception {
        UserDto userDto = UserDto.builder()
                .name("")
                .email("john.doe@example.com")
                .build();

        when(httpClient.post(eq("/users"), eq(null), any(UserDto.class)))
                .thenReturn(mockResponse);

        mvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());
    }

    @Test
    void create_WithNullName_ShouldReturnOk() throws Exception {
        UserDto userDto = UserDto.builder()
                .name(null)
                .email("john.doe@example.com")
                .build();

        when(httpClient.post(eq("/users"), eq(null), any(UserDto.class)))
                .thenReturn(mockResponse);

        mvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());
    }

    @Test
    void create_WithOnlyEmail_ShouldReturnOk() throws Exception {
        UserDto userDto = UserDto.builder()
                .email("john.doe@example.com")
                .build();

        when(httpClient.post(eq("/users"), eq(null), any(UserDto.class)))
                .thenReturn(mockResponse);

        mvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());
    }

    @Test
    void create_WithOnlyName_ShouldReturnOk() throws Exception {
        UserDto userDto = UserDto.builder()
                .name("John Doe")
                .build();

        when(httpClient.post(eq("/users"), eq(null), any(UserDto.class)))
                .thenReturn(mockResponse);

        mvc.perform(post("/users")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());
    }

    @Test
    void update_ShouldReturnOk() throws Exception {
        long userId = 1L;
        UserDto userDto = UserDto.builder()
                .name("John Updated")
                .email("john.updated@example.com")
                .build();

        when(httpClient.patch(eq("/users/1"), eq(null), any(UserDto.class)))
                .thenReturn(mockResponse);

        mvc.perform(patch("/users/{id}", userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1}"));
    }

    @Test
    void update_WithPartialData_ShouldReturnOk() throws Exception {
        long userId = 1L;
        UserDto userDto = UserDto.builder()
                .name("John Updated")
                .build();

        when(httpClient.patch(eq("/users/1"), eq(null), any(UserDto.class)))
                .thenReturn(mockResponse);

        mvc.perform(patch("/users/{id}", userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());
    }

    @Test
    void update_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        long invalidUserId = 0L;
        UserDto userDto = UserDto.builder()
                .name("John Updated")
                .email("john.updated@example.com")
                .build();

        mvc.perform(patch("/users/{id}", invalidUserId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;
        UserDto userDto = UserDto.builder()
                .name("John Updated")
                .email("invalid-email")
                .build();

        mvc.perform(patch("/users/{id}", userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_WithLongName_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;
        UserDto userDto = UserDto.builder()
                .name("A".repeat(101))
                .email("john.updated@example.com")
                .build();

        mvc.perform(patch("/users/{id}", userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_WithMissingBody_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;

        mvc.perform(patch("/users/{id}", userId)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_WithOnlyEmail_ShouldReturnOk() throws Exception {
        long userId = 1L;
        UserDto userDto = UserDto.builder()
                .email("john.updated@example.com")
                .build();

        when(httpClient.patch(eq("/users/1"), eq(null), any(UserDto.class)))
                .thenReturn(mockResponse);

        mvc.perform(patch("/users/{id}", userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());
    }

    @Test
    void update_WithOnlyName_ShouldReturnOk() throws Exception {
        long userId = 1L;
        UserDto userDto = UserDto.builder()
                .name("John Updated")
                .build();

        when(httpClient.patch(eq("/users/1"), eq(null), any(UserDto.class)))
                .thenReturn(mockResponse);

        mvc.perform(patch("/users/{id}", userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());
    }

    @Test
    void update_WithNullFields_ShouldReturnOk() throws Exception {
        long userId = 1L;
        UserDto userDto = UserDto.builder()
                .name(null)
                .email(null)
                .build();

        when(httpClient.patch(eq("/users/1"), eq(null), any(UserDto.class)))
                .thenReturn(mockResponse);

        mvc.perform(patch("/users/{id}", userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_WhenHttpClientReturnsError_ShouldPropagateError() throws Exception {
        when(httpClient.get(eq("/users"), eq(null)))
                .thenReturn(ResponseEntity.notFound().build());

        mvc.perform(get("/users"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_WhenHttpClientReturnsError_ShouldPropagateError() throws Exception {
        long userId = 1L;

        when(httpClient.get(eq("/users/1"), eq(null)))
                .thenReturn(ResponseEntity.notFound().build());

        mvc.perform(get("/users/{id}", userId))
                .andExpect(status().isNotFound());
    }
}