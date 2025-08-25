package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ItemDto {
    private Long id;

    @Size(max = 200, message = "Название не должно превышать 200 символов")
    private String name;

    @Size(max = 300, message = "Описание не должно превышать 300 символов")
    private String description;

    private Boolean available;

    private BookingInfo lastBooking;

    private BookingInfo nextBooking;

    private List<CommentDto> comments;

    @Data
    @AllArgsConstructor
    public static class BookingInfo {
        private LocalDateTime start;
        private LocalDateTime end;
    }
}
