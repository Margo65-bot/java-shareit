package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.client.HttpClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private static final String API_PREFIX = "/requests";
    private final HttpClient httpClient;

    @GetMapping
    public ResponseEntity<Object> getAllByUserId(
            @RequestHeader("${shareit.api.auth.userheader}") @Positive(message = "id пользователя должен быть больше 0") long userId
    ) {
        return httpClient.get(API_PREFIX, userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllOther(
            @RequestHeader("${shareit.api.auth.userheader}") @Positive(message = "id пользователя должен быть больше 0") long userId
    ) {
        return httpClient.get(API_PREFIX, userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable(name = "id") long id) {
        return httpClient.get(API_PREFIX + "/" + id, null);
    }

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader("${shareit.api.auth.userheader}") @Positive(message = "id пользователя должен быть больше 0") long userId,
            @Valid @RequestBody ItemRequestDto requestDto
    ) {
        return httpClient.post(API_PREFIX, userId, requestDto);
    }
}
