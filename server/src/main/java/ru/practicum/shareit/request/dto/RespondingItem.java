package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RespondingItem {
    private Long id;
    private String name;
    private Long userId;
    @JsonIgnore
    private Long requestId;
}
