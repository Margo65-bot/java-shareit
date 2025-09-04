package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.client.HttpClient;
import ru.practicum.shareit.user.dto.UserDto;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private static final String API_PREFIX = "/users";
    private final HttpClient httpClient;

    @GetMapping
    public ResponseEntity<Object> getAll() {
        return httpClient.get(API_PREFIX, null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable @Positive(message = "id пользователя должен быть больше 0") long id
    ) {
        return httpClient.get(API_PREFIX + "/" + id, null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteById(
            @PathVariable @Positive(message = "id пользователя должен быть больше 0") long id
    ) {
        return httpClient.delete(API_PREFIX + "/" + id, null);
    }

    @PostMapping
    public ResponseEntity<Object> create(
            @Valid @RequestBody UserDto userDto
    ) {
        return httpClient.post(API_PREFIX, null, userDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable @Positive(message = "id пользователя должен быть больше 0") long id,
            @Valid @RequestBody UserDto userDto
    ) {
        return httpClient.patch(API_PREFIX + "/" + id, null, userDto);
    }
}
