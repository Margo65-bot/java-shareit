package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.client.HttpClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {
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
    void getById_ShouldReturnOk() throws Exception {
        long itemId = 1L;
        when(httpClient.get(eq("/items/1"), eq(null)))
                .thenReturn(mockResponse);

        mvc.perform(get("/items/{id}", itemId))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1}"));
    }

    @Test
    void getById_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        long invalidItemId = 0L;
        mvc.perform(get("/items/{id}", invalidItemId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteById_ShouldReturnOk() throws Exception {
        long itemId = 1L;
        when(httpClient.delete(eq("/items/1"), eq(null)))
                .thenReturn(mockResponse);

        mvc.perform(delete("/items/{id}", itemId))
                .andExpect(status().isOk());
    }

    @Test
    void deleteById_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        long invalidItemId = 0L;
        mvc.perform(delete("/items/{id}", invalidItemId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_ShouldReturnOk() throws Exception {
        long userId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        when(httpClient.post(eq("/items"), eq(userId), any(ItemDto.class)))
                .thenReturn(mockResponse);

        mvc.perform(post("/items")
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1}"));
    }

    @Test
    void create_WithInvalidUserId_ShouldReturnBadRequest() throws Exception {
        long invalidUserId = 0L;
        ItemDto itemDto = ItemDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        mvc.perform(post("/items")
                        .header(userIdHeader, invalidUserId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithInvalidItem_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("A".repeat(201)) // превышает 200 символов
                .description("Test Description")
                .available(true)
                .build();

        mvc.perform(post("/items")
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithLongDescription_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("Test Item")
                .description("A".repeat(301)) // превышает 300 символов
                .available(true)
                .build();

        mvc.perform(post("/items")
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithMissingHeader_ShouldReturnBadRequest() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        mvc.perform(post("/items")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_ShouldReturnOk() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("Updated Item")
                .description("Updated Description")
                .available(false)
                .build();

        when(httpClient.patch(eq("/items/1"), eq(userId), any(ItemDto.class)))
                .thenReturn(mockResponse);

        mvc.perform(patch("/items/{id}", itemId)
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1}"));
    }

    @Test
    void update_WithInvalidItemId_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;
        long invalidItemId = 0L;
        ItemDto itemDto = ItemDto.builder()
                .name("Updated Item")
                .description("Updated Description")
                .available(false)
                .build();

        mvc.perform(patch("/items/{id}", invalidItemId)
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_WithInvalidUserId_ShouldReturnBadRequest() throws Exception {
        long invalidUserId = 0L;
        long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("Updated Item")
                .description("Updated Description")
                .available(false)
                .build();

        mvc.perform(patch("/items/{id}", itemId)
                        .header(userIdHeader, invalidUserId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_WithInvalidItem_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        ItemDto itemDto = ItemDto.builder()
                .name("A".repeat(201))
                .description("Updated Description")
                .available(false)
                .build();

        mvc.perform(patch("/items/{id}", itemId)
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItems_ShouldReturnOk() throws Exception {
        long userId = 1L;
        when(httpClient.get(eq("/items"), eq(userId)))
                .thenReturn(mockResponse);

        mvc.perform(get("/items")
                        .header(userIdHeader, userId))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1}"));
    }

    @Test
    void getItems_WithInvalidUserId_ShouldReturnBadRequest() throws Exception {
        long invalidUserId = 0L;
        mvc.perform(get("/items")
                        .header(userIdHeader, invalidUserId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItems_WithMissingHeader_ShouldReturnBadRequest() throws Exception {
        mvc.perform(get("/items"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchItems_WithQuery_ShouldReturnOk() throws Exception {
        String query = "test";
        when(httpClient.get(eq("/items/search?text=test"), eq(null)))
                .thenReturn(mockResponse);

        mvc.perform(get("/items/search")
                        .param("text", query))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1}"));
    }

    @Test
    void searchItems_WithEmptyQuery_ShouldReturnOk() throws Exception {
        when(httpClient.get(eq("/items/search?text="), eq(null)))
                .thenReturn(mockResponse);

        mvc.perform(get("/items/search"))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems_WithSpecialCharacters_ShouldReturnOk() throws Exception {
        String query = "test+query with spaces";
        when(httpClient.get(eq("/items/search?text=test+query with spaces"), eq(null)))
                .thenReturn(mockResponse);

        mvc.perform(get("/items/search")
                        .param("text", query))
                .andExpect(status().isOk());
    }

    @Test
    void createComment_ShouldReturnOk() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        CommentDto commentDto = CommentDto.builder()
                .text("Great item!")
                .build();

        when(httpClient.post(eq("/items/1/comment"), eq(userId), any(CommentDto.class)))
                .thenReturn(mockResponse);

        mvc.perform(post("/items/{id}/comment", itemId)
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1}"));
    }

    @Test
    void createComment_WithInvalidUserId_ShouldReturnBadRequest() throws Exception {
        long invalidUserId = 0L;
        long itemId = 1L;
        CommentDto commentDto = CommentDto.builder()
                .text("Great item!")
                .build();

        mvc.perform(post("/items/{id}/comment", itemId)
                        .header(userIdHeader, invalidUserId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_WithInvalidItemId_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;
        long invalidItemId = 0L;
        CommentDto commentDto = CommentDto.builder()
                .text("Great item!")
                .build();

        mvc.perform(post("/items/{id}/comment", invalidItemId)
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_WithEmptyText_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        CommentDto commentDto = CommentDto.builder()
                .text("")
                .build();

        mvc.perform(post("/items/{id}/comment", itemId)
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_WithLongText_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        CommentDto commentDto = CommentDto.builder()
                .text("A".repeat(1001)) // превышает 1000 символов
                .build();

        mvc.perform(post("/items/{id}/comment", itemId)
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_WithNullText_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        CommentDto commentDto = CommentDto.builder()
                .text(null)
                .build();

        mvc.perform(post("/items/{id}/comment", itemId)
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_WithMissingHeader_ShouldReturnBadRequest() throws Exception {
        long itemId = 1L;
        CommentDto commentDto = CommentDto.builder()
                .text("Great item!")
                .build();

        mvc.perform(post("/items/{id}/comment", itemId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_WithFullCommentDto_ShouldReturnOk() throws Exception {
        long userId = 1L;
        long itemId = 1L;
        CommentDto commentDto = CommentDto.builder()
                .text("Great item!")
                .authorName("Test User")
                .created(LocalDateTime.now())
                .itemId(itemId)
                .build();

        when(httpClient.post(eq("/items/1/comment"), eq(userId), any(CommentDto.class)))
                .thenReturn(mockResponse);

        mvc.perform(post("/items/{id}/comment", itemId)
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk());
    }
}