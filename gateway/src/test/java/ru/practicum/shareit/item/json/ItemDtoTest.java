package ru.practicum.shareit.item.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemDtoTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeItemDto() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 16, 10, 0);

        ItemDto.BookingInfo lastBooking = new ItemDto.BookingInfo(1L, start, end);
        ItemDto.BookingInfo nextBooking = new ItemDto.BookingInfo(2L,
                LocalDateTime.of(2024, 1, 17, 10, 0),
                LocalDateTime.of(2024, 1, 18, 10, 0));

        CommentDto comment = CommentDto.builder()
                .id(1L)
                .text("Хорошая вещь")
                .authorName("Петр")
                .created(LocalDateTime.of(2024, 1, 14, 15, 30))
                .itemId(123L)
                .build();

        ItemDto dto = ItemDto.builder()
                .id(123L)
                .name("Дрель")
                .description("Мощная дрель с ударным механизмом")
                .available(true)
                .requestId(456L)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(List.of(comment))
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":123");
        assertThat(json).contains("\"name\":\"Дрель\"");
        assertThat(json).contains("\"description\":\"Мощная дрель с ударным механизмом\"");
        assertThat(json).contains("\"available\":true");
        assertThat(json).contains("\"requestId\":456");
        assertThat(json).contains("\"lastBooking\"");
        assertThat(json).contains("\"nextBooking\"");
        assertThat(json).contains("\"comments\"");
        assertThat(json).contains("\"itemId\":1");
        assertThat(json).contains("\"start\":\"2024-01-15T10:00:00\"");
    }

    @Test
    void shouldDeserializeItemDto() throws Exception {
        String json = """
            {
                "id": 123,
                "name": "Дрель",
                "description": "Мощная дрель с ударным механизмом",
                "available": true,
                "requestId": 456,
                "lastBooking": {
                    "itemId": 1,
                    "start": "2024-01-15T10:00:00",
                    "end": "2024-01-16T10:00:00"
                },
                "nextBooking": {
                    "itemId": 2,
                    "start": "2024-01-17T10:00:00",
                    "end": "2024-01-18T10:00:00"
                },
                "comments": [
                    {
                        "id": 1,
                        "text": "Хорошая вещь",
                        "authorName": "Петр",
                        "created": "2024-01-14T15:30:00",
                        "itemId": 123
                    }
                ]
            }
            """;

        ItemDto dto = objectMapper.readValue(json, ItemDto.class);

        assertThat(dto.getId()).isEqualTo(123L);
        assertThat(dto.getName()).isEqualTo("Дрель");
        assertThat(dto.getDescription()).isEqualTo("Мощная дрель с ударным механизмом");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isEqualTo(456L);

        assertThat(dto.getLastBooking()).isNotNull();
        assertThat(dto.getLastBooking().getItemId()).isEqualTo(1L);
        assertThat(dto.getLastBooking().getStart()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 0));

        assertThat(dto.getNextBooking()).isNotNull();
        assertThat(dto.getNextBooking().getItemId()).isEqualTo(2L);

        assertThat(dto.getComments()).hasSize(1);
        assertThat(dto.getComments().get(0).getText()).isEqualTo("Хорошая вещь");
    }

    @Test
    void shouldDeserializeMinimalItemDto() throws Exception {
        String json = """
            {
                "name": "Молоток",
                "description": "Простой молоток",
                "available": true
            }
            """;

        ItemDto dto = objectMapper.readValue(json, ItemDto.class);

        assertThat(dto.getId()).isNull();
        assertThat(dto.getName()).isEqualTo("Молоток");
        assertThat(dto.getDescription()).isEqualTo("Простой молоток");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getRequestId()).isNull();
        assertThat(dto.getLastBooking()).isNull();
        assertThat(dto.getNextBooking()).isNull();
        assertThat(dto.getComments()).isNull();
    }

    @Test
    void shouldHandleNullCollections() throws Exception {
        String json = """
            {
                "name": "Отвертка",
                "description": "Крестовая отвертка",
                "available": true,
                "comments": null
            }
            """;

        ItemDto dto = objectMapper.readValue(json, ItemDto.class);

        assertThat(dto.getName()).isEqualTo("Отвертка");
        assertThat(dto.getComments()).isNull();
    }

    @Test
    void shouldHandleEmptyCollections() throws Exception {
        String json = """
            {
                "name": "Пила",
                "description": "Ручная пила",
                "available": false,
                "comments": []
            }
            """;

        ItemDto dto = objectMapper.readValue(json, ItemDto.class);

        assertThat(dto.getName()).isEqualTo("Пила");
        assertThat(dto.getComments()).isEmpty();
    }
}