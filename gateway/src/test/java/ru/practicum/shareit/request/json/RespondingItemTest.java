package ru.practicum.shareit.request.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.request.dto.RespondingItem;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class RespondingItemTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeRespondingItem() throws Exception {
        RespondingItem item = RespondingItem.builder()
                .id(1L)
                .name("Дрель")
                .userId(123L)
                .build();

        String json = objectMapper.writeValueAsString(item);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Дрель\"");
        assertThat(json).contains("\"userId\":123");
    }

    @Test
    void shouldDeserializeRespondingItem() throws Exception {
        String json = "{\"id\": 1, \"name\": \"Дрель\", \"userId\": 123}";

        RespondingItem item = objectMapper.readValue(json, RespondingItem.class);

        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getName()).isEqualTo("Дрель");
        assertThat(item.getUserId()).isEqualTo(123L);
    }

    @Test
    void shouldDeserializeWithNullValues() throws Exception {
        String json = "{\"id\": 1, \"name\": null, \"userId\": null}";

        RespondingItem item = objectMapper.readValue(json, RespondingItem.class);

        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getName()).isNull();
        assertThat(item.getUserId()).isNull();
    }
}