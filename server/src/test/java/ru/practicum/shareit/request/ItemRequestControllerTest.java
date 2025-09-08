package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
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
    private ItemRequestService itemRequestService;

    private final Long userId = 1L;
    private final Long requestId = 1L;

    @Test
    void create_ValidRequest_ShouldReturnCreatedRequest() throws Exception {
        ItemRequestDto createDto = ItemRequestDto.builder()
                .description("Need a drill for home repairs")
                .build();

        ItemRequestDto responseDto = ItemRequestDto.builder()
                .id(requestId)
                .description("Need a drill for home repairs")
                .created(LocalDateTime.now())
                .build();

        when(itemRequestService.create(eq(userId), any(ItemRequestDto.class)))
                .thenReturn(responseDto);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value("Need a drill for home repairs"));
    }

    @Test
    void getById_ValidRequest_ShouldReturnRequest() throws Exception {
        ItemRequestDto responseDto = ItemRequestDto.builder()
                .id(requestId)
                .description("Need a drill for home repairs")
                .created(LocalDateTime.now())
                .build();

        when(itemRequestService.getById(requestId))
                .thenReturn(responseDto);

        mvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(requestId))
                .andExpect(jsonPath("$.description").value("Need a drill for home repairs"));
    }

    @Test
    void getAllByUserId_ValidRequest_ShouldReturnRequests() throws Exception {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("Need a drill for home repairs")
                .created(LocalDateTime.now())
                .build();

        when(itemRequestService.getAllByUserId(userId))
                .thenReturn(List.of(requestDto));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(requestId))
                .andExpect(jsonPath("$[0].description").value("Need a drill for home repairs"));
    }

    @Test
    void getAllByUserId_WithEmptyResult_ShouldReturnEmptyList() throws Exception {
        when(itemRequestService.getAllByUserId(userId))
                .thenReturn(List.of());

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getAllOther_ValidRequest_ShouldReturnRequests() throws Exception {
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("Need a drill for home repairs")
                .created(LocalDateTime.now())
                .build();

        when(itemRequestService.getAllOther(userId))
                .thenReturn(List.of(requestDto));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(requestId))
                .andExpect(jsonPath("$[0].description").value("Need a drill for home repairs"));
    }

    @Test
    void getAllOther_WithEmptyResult_ShouldReturnEmptyList() throws Exception {
        when(itemRequestService.getAllOther(userId))
                .thenReturn(List.of());

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}