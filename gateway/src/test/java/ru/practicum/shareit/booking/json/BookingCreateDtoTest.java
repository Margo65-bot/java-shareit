package ru.practicum.shareit.booking.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.dto.BookingCreateDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingCreateDtoTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeBookingCreateDto() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 16, 10, 0);
        BookingCreateDto dto = BookingCreateDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .itemId(123L)
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"start\":\"2024-01-15T10:00:00\"");
        assertThat(json).contains("\"end\":\"2024-01-16T10:00:00\"");
        assertThat(json).contains("\"itemId\":123");
    }

    @Test
    void shouldDeserializeBookingCreateDto() throws Exception {
        // Given
        String json = """
            {
                "id": 1,
                "start": "2024-01-15T10:00:00",
                "end": "2024-01-16T10:00:00",
                "itemId": 123
            }
            """;

        BookingCreateDto dto = objectMapper.readValue(json, BookingCreateDto.class);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 16, 10, 0));
        assertThat(dto.getItemId()).isEqualTo(123L);
    }

    @Test
    void shouldHandleNullValues() throws Exception {
        String json = """
            {
                "start": "2024-01-15T10:00:00",
                "end": "2024-01-16T10:00:00",
                "itemId": 123
            }
            """;

        BookingCreateDto dto = objectMapper.readValue(json, BookingCreateDto.class);

        assertThat(dto.getId()).isNull();
        assertThat(dto.getStart()).isNotNull();
        assertThat(dto.getEnd()).isNotNull();
        assertThat(dto.getItemId()).isNotNull();
    }
}