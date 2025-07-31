package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemDto {
    private Long id;

    @Size(max = 200, message = "Название не должно превышать 200 символов")
    private String name;

    @Size(max = 300, message = "Описание не должно превышать 300 символов")
    private String description;

    private Boolean available;
}
