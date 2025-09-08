package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
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
    private ItemService itemService;

    private final Long userId = 1L;
    private final Long itemId = 1L;
    private final Long commentId = 1L;

    @Test
    void getById_ValidRequest_ShouldReturnItem() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(itemId)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        when(itemService.getById(itemId))
                .thenReturn(itemDto);

        mvc.perform(get("/items/{id}", itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Test Item"));
    }

    @Test
    void deleteById_ValidRequest_ShouldReturnOk() throws Exception {
        mvc.perform(delete("/items/{id}", itemId))
                .andExpect(status().isOk());
    }

    @Test
    void create_ValidRequest_ShouldReturnCreatedItem() throws Exception {
        ItemDto createDto = ItemDto.builder()
                .name("New Item")
                .description("New Description")
                .available(true)
                .build();

        ItemDto responseDto = ItemDto.builder()
                .id(itemId)
                .name("New Item")
                .description("New Description")
                .available(true)
                .build();

        when(itemService.create(eq(userId), any(ItemDto.class)))
                .thenReturn(responseDto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("New Item"));
    }

    @Test
    void update_ValidRequest_ShouldReturnUpdatedItem() throws Exception {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Item")
                .description("Updated Description")
                .available(false)
                .build();

        ItemDto responseDto = ItemDto.builder()
                .id(itemId)
                .name("Updated Item")
                .description("Updated Description")
                .available(false)
                .build();

        when(itemService.update(eq(itemId), eq(userId), any(ItemDto.class)))
                .thenReturn(responseDto);

        mvc.perform(patch("/items/{id}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value("Updated Item"));
    }

    @Test
    void update_WithPartialData_ShouldReturnUpdatedItem() throws Exception {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Name Only")
                .build();

        ItemDto responseDto = ItemDto.builder()
                .id(itemId)
                .name("Updated Name Only")
                .description("Original Description")
                .available(true)
                .build();

        when(itemService.update(eq(itemId), eq(userId), any(ItemDto.class)))
                .thenReturn(responseDto);

        mvc.perform(patch("/items/{id}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name Only"));
    }

    @Test
    void getItemsByUserId_ValidRequest_ShouldReturnItems() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(itemId)
                .name("User Item")
                .description("User Description")
                .available(true)
                .build();

        when(itemService.getItemsByUserId(userId))
                .thenReturn(List.of(itemDto));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemId))
                .andExpect(jsonPath("$[0].name").value("User Item"));
    }

    @Test
    void searchItems_WithQuery_ShouldReturnMatchingItems() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(itemId)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();

        when(itemService.searchItems("drill"))
                .thenReturn(List.of(itemDto));

        mvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemId))
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void searchItems_WithEmptyQuery_ShouldReturnEmptyList() throws Exception {
        when(itemService.searchItems(""))
                .thenReturn(List.of());

        mvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void searchItems_WithBlankQuery_ShouldReturnEmptyList() throws Exception {
        when(itemService.searchItems("   "))
                .thenReturn(List.of());

        mvc.perform(get("/items/search")
                        .param("text", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void searchItems_WithoutQueryParam_ShouldUseDefaultEmpty() throws Exception {
        when(itemService.searchItems(""))
                .thenReturn(List.of());

        mvc.perform(get("/items/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void createComment_ValidRequest_ShouldReturnComment() throws Exception {
        CommentDto createDto = CommentDto.builder()
                .text("Great item!")
                .build();

        CommentDto responseDto = CommentDto.builder()
                .id(commentId)
                .text("Great item!")
                .authorName("Test User")
                .created(LocalDateTime.now())
                .itemId(itemId)
                .build();

        when(itemService.createComment(eq(userId), eq(itemId), any(CommentDto.class)))
                .thenReturn(responseDto);

        mvc.perform(post("/items/{id}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.text").value("Great item!"))
                .andExpect(jsonPath("$.authorName").value("Test User"));
    }

    @Test
    void create_WithRequestId_ShouldReturnItemWithRequest() throws Exception {
        ItemDto createDto = ItemDto.builder()
                .name("Item with Request")
                .description("Description")
                .available(true)
                .requestId(10L)
                .build();

        ItemDto responseDto = ItemDto.builder()
                .id(itemId)
                .name("Item with Request")
                .description("Description")
                .available(true)
                .requestId(10L)
                .build();

        when(itemService.create(eq(userId), any(ItemDto.class)))
                .thenReturn(responseDto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(10L));
    }

    @Test
    void update_WithOnlyAvailable_ShouldReturnUpdatedItem() throws Exception {
        ItemDto updateDto = ItemDto.builder()
                .available(false)
                .build();

        ItemDto responseDto = ItemDto.builder()
                .id(itemId)
                .name("Original Name")
                .description("Original Description")
                .available(false)
                .build();

        when(itemService.update(eq(itemId), eq(userId), any(ItemDto.class)))
                .thenReturn(responseDto);

        mvc.perform(patch("/items/{id}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void update_WithOnlyDescription_ShouldReturnUpdatedItem() throws Exception {
        ItemDto updateDto = ItemDto.builder()
                .description("Updated Description Only")
                .build();

        ItemDto responseDto = ItemDto.builder()
                .id(itemId)
                .name("Original Name")
                .description("Updated Description Only")
                .available(true)
                .build();

        when(itemService.update(eq(itemId), eq(userId), any(ItemDto.class)))
                .thenReturn(responseDto);

        mvc.perform(patch("/items/{id}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated Description Only"));
    }

    @Test
    void getItemsByUserId_WithEmptyResult_ShouldReturnEmptyList() throws Exception {
        when(itemService.getItemsByUserId(userId))
                .thenReturn(List.of());

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}