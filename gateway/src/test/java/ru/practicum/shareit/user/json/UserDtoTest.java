package ru.practicum.shareit.user.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserDtoTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeUserDto() throws Exception {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("Иван Иванов")
                .email("ivan@example.com")
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"name\":\"Иван Иванов\"");
        assertThat(json).contains("\"email\":\"ivan@example.com\"");
    }

    @Test
    void shouldDeserializeUserDto() throws Exception {
        String json = """
            {
                "id": 1,
                "name": "Иван Иванов",
                "email": "ivan@example.com"
            }
            """;

        UserDto dto = objectMapper.readValue(json, UserDto.class);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Иван Иванов");
        assertThat(dto.getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    void shouldDeserializeWithNullValues() throws Exception {
        String json = """
            {
                "id": 1,
                "name": null,
                "email": null
            }
            """;

        UserDto dto = objectMapper.readValue(json, UserDto.class);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isNull();
        assertThat(dto.getEmail()).isNull();
    }
}