package ru.practicum.shareit.item.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.item.dto.CommentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CommentDtoTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeCommentDto() throws Exception {
        LocalDateTime created = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
        CommentDto dto = CommentDto.builder()
                .id(1L)
                .text("Отличная вещь! Очень доволен покупкой.")
                .authorName("Иван Иванов")
                .created(created)
                .itemId(123L)
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"text\":\"Отличная вещь! Очень доволен покупкой.\"");
        assertThat(json).contains("\"authorName\":\"Иван Иванов\"");
        assertThat(json).contains("\"created\":\"2024-01-15T10:30:45\"");
        assertThat(json).contains("\"itemId\":123");
    }

    @Test
    void shouldDeserializeCommentDto() throws Exception {
        String json = """
                {"id": 1,
                "text": "Отличная вещь! Очень доволен покупкой.",
                "authorName": "Иван Иванов",
                "created": "2024-01-15T10:30:45",
                "itemId": 123
                }
                """;

        CommentDto dto = objectMapper.readValue(json, CommentDto.class);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getText()).isEqualTo("Отличная вещь! Очень доволен покупкой.");
        assertThat(dto.getAuthorName()).isEqualTo("Иван Иванов");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 45));
        assertThat(dto.getItemId()).isEqualTo(123L);
    }

    @Test
    void shouldHandleNullValues() throws Exception {
        String json = """
                {"text": "Комментарий",
                "authorName": null,
                "created": null,
                "itemId": 123
                }
                """;

        CommentDto dto = objectMapper.readValue(json, CommentDto.class);

        assertThat(dto.getText()).isEqualTo("Комментарий");
        assertThat(dto.getAuthorName()).isNull();
        assertThat(dto.getCreated()).isNull();
        assertThat(dto.getItemId()).isEqualTo(123L);
    }
}