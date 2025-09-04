package ru.practicum.shareit.request.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RespondingItem;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestDtoTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeItemRequestDto() throws Exception {
        RespondingItem item1 = RespondingItem.builder()
                .id(1L)
                .name("Дрель")
                .userId(123L)
                .build();

        RespondingItem item2 = RespondingItem.builder()
                .id(2L)
                .name("Молоток")
                .userId(123L)
                .build();

        LocalDateTime created = LocalDateTime.of(2024, 1, 15, 10, 30, 45);

        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужны инструменты для ремонта")
                .created(created)
                .items(List.of(item1, item2))
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"description\":\"Нужны инструменты для ремонта\"");
        assertThat(json).contains("\"created\":\"2024-01-15T10:30:45\"");
        assertThat(json).contains("\"items\"");
        assertThat(json).contains("\"name\":\"Дрель\"");
        assertThat(json).contains("\"name\":\"Молоток\"");
    }

    @Test
    void shouldDeserializeItemRequestDto() throws Exception {
        String json = """
                {
                    "id": 1,
                    "description": "Нужны инструменты для ремонта",
                    "created": "2024-01-15T10:30:45",
                    "items": [
                        {
                            "id": 1,
                            "name": "Дрель",
                            "userId": 123
                        },
                        {
                            "id": 2,
                            "name": "Молоток",
                            "userId": 123
                        }
                    ]
                }
                """;

        ItemRequestDto dto = objectMapper.readValue(json, ItemRequestDto.class);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Нужны инструменты для ремонта");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 45));
        assertThat(dto.getItems()).hasSize(2);
        assertThat(dto.getItems().get(0).getName()).isEqualTo("Дрель");
        assertThat(dto.getItems().get(1).getName()).isEqualTo("Молоток");
    }

    @Test
    void shouldDeserializeWithoutItems() throws Exception {
        String json = """
                {
                    "id": 1,
                    "description": "Простой запрос",
                    "created": "2024-01-15T10:30:45"
                }
                """;

        ItemRequestDto dto = objectMapper.readValue(json, ItemRequestDto.class);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Простой запрос");
        assertThat(dto.getCreated()).isNotNull();
        assertThat(dto.getItems()).isNull();
    }
}