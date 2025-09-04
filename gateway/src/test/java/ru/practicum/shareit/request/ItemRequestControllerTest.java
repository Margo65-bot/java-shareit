package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.client.HttpClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RespondingItem;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTest {
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
    void getAllByUserId_ShouldReturnOk() throws Exception {
        long userId = 1L;

        when(httpClient.get(eq("/requests"), eq(userId)))
                .thenReturn(mockResponse);

        mvc.perform(get("/requests")
                        .header(userIdHeader, userId))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1}"));
    }

    @Test
    void getAllByUserId_WithInvalidUserId_ShouldReturnBadRequest() throws Exception {
        long invalidUserId = 0L;
        mvc.perform(get("/requests")
                        .header(userIdHeader, invalidUserId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllByUserId_WithMissingHeader_ShouldReturnBadRequest() throws Exception {
        mvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllOther_ShouldReturnOk() throws Exception {
        long userId = 1L;
        when(httpClient.get(eq("/requests"), eq(userId)))
                .thenReturn(mockResponse);

        mvc.perform(get("/requests/all")
                        .header(userIdHeader, userId))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1}"));
    }

    @Test
    void getAllOther_WithInvalidUserId_ShouldReturnBadRequest() throws Exception {
        long invalidUserId = 0L;
        mvc.perform(get("/requests/all")
                        .header(userIdHeader, invalidUserId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllOther_WithMissingHeader_ShouldReturnBadRequest() throws Exception {
        mvc.perform(get("/requests/all"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_ShouldReturnOk() throws Exception {
        long requestId = 1L;
        when(httpClient.get(eq("/requests/1"), eq(null)))
                .thenReturn(mockResponse);

        mvc.perform(get("/requests/{id}", requestId))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1}"));
    }

    @Test
    void getById_WithInvalidId_ShouldReturnOk() throws Exception {
        long requestId = 0L;
        when(httpClient.get(eq("/requests/0"), eq(null)))
                .thenReturn(mockResponse);

        mvc.perform(get("/requests/{id}", requestId))
                .andExpect(status().isOk());
    }

    @Test
    void create_ShouldReturnOk() throws Exception {
        long userId = 1L;
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill for home repairs")
                .build();

        when(httpClient.post(eq("/requests"), eq(userId), any(ItemRequestDto.class)))
                .thenReturn(mockResponse);

        mvc.perform(post("/requests")
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1}"));
    }

    @Test
    void create_WithFullRequestDto_ShouldReturnOk() throws Exception {
        long userId = 1L;
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill for home repairs")
                .created(LocalDateTime.now())
                .items(List.of(
                        RespondingItem.builder()
                                .id(1L)
                                .name("Drill")
                                .userId(2L)
                                .build()
                ))
                .build();

        when(httpClient.post(eq("/requests"), eq(userId), any(ItemRequestDto.class)))
                .thenReturn(mockResponse);

        mvc.perform(post("/requests")
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());
    }

    @Test
    void create_WithInvalidUserId_ShouldReturnBadRequest() throws Exception {
        long invalidUserId = 0L;
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill for home repairs")
                .build();

        mvc.perform(post("/requests")
                        .header(userIdHeader, invalidUserId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithEmptyDescription_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("")
                .build();

        mvc.perform(post("/requests")
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithBlankDescription_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("   ")
                .build();

        mvc.perform(post("/requests")
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithNullDescription_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description(null)
                .build();

        mvc.perform(post("/requests")
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithLongDescription_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("A".repeat(501))
                .build();

        mvc.perform(post("/requests")
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithMissingHeader_ShouldReturnBadRequest() throws Exception {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill for home repairs")
                .build();

        mvc.perform(post("/requests")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithMissingBody_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;
        mvc.perform(post("/requests")
                        .header(userIdHeader, userId)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithInvalidContentType_ShouldReturnUnsupportedMediaType() throws Exception {
        long userId = 1L;
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill for home repairs")
                .build();

        mvc.perform(post("/requests")
                        .header(userIdHeader, userId)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void getById_WithStringId_ShouldReturnBadRequest() throws Exception {
        mvc.perform(get("/requests/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithOnlyRequiredFields_ShouldReturnOk() throws Exception {
        long userId = 1L;
        String requestJson = "{\"description\": \"Need a drill\"}";
        when(httpClient.post(eq("/requests"), eq(userId), any(ItemRequestDto.class)))
                .thenReturn(mockResponse);

        mvc.perform(post("/requests")
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    void create_WithExtraFields_ShouldReturnOk() throws Exception {
        long userId = 1L;
        String requestJson = "{\"description\": \"Need a drill\", \"extraField\": \"value\"}";

        when(httpClient.post(eq("/requests"), eq(userId), any(ItemRequestDto.class)))
                .thenReturn(mockResponse);

        mvc.perform(post("/requests")
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(requestJson))
                .andExpect(status().isOk());
    }
}