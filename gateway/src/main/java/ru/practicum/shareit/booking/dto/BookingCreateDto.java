package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingCreateDto {
    private Long id;

    @NotNull(message = "Дата начала бронирования не может быть пустой")
    @Future(message = "Бронирование доступно на предстоящие даты и время")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания бронирования не может быть пустой")
    @Future(message = "Бронирование доступно на предстоящие даты и время")
    private LocalDateTime end;

    @NotNull(message = "Вещь для бронирования должна быть заполнена")
    private Long itemId;

    @AssertTrue(message = "Дата окончания бронирования должна быть позже даты начала")
    private boolean isEndDateAfterStartDate() {
        if (start == null || end == null) {
            return true;
        }
        return end.isAfter(start);
    }
}
