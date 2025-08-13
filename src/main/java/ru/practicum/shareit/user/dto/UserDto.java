package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Long id;

    @Size(max = 100, message = "Имя не должно превышать 100 символов")
    private String name;

    @Email(message = "Электронная почта должна соответствовать своему формату")
    private String email;
}
