package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RespondingItem {
    private Long id;
    private String name;
    private Long userId;
}
