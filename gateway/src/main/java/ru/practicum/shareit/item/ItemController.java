package ru.practicum.shareit.item;

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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.client.HttpClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private static final String API_PREFIX = "/items";
    private final HttpClient httpClient;

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(
            @PathVariable @Positive(message = "id вещи должен быть больше 0") long id
    ) {
        return httpClient.get(API_PREFIX + "/" + id, null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteById(
            @PathVariable @Positive(message = "id вещи должен быть больше 0") long id
    ) {
        return httpClient.delete(API_PREFIX + "/" + id, null);
    }

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader("${shareit.api.auth.userheader}") @Positive(message = "id пользователя должен быть больше 0") long userId,
            @Valid @RequestBody ItemDto itemDto
    ) {
        return httpClient.post(API_PREFIX, userId, itemDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(
            @PathVariable(name = "id") @Positive(message = "id вещи должен быть больше 0") long itemId,
            @RequestHeader("${shareit.api.auth.userheader}") @Positive(message = "id пользователя должен быть больше 0") long userId,
            @Valid @RequestBody ItemDto itemDto
    ) {
        return httpClient.patch(API_PREFIX + "/" + itemId, userId, itemDto);
    }

    @GetMapping
    public ResponseEntity<Object> getItems(
            @RequestHeader("${shareit.api.auth.userheader}") @Positive(message = "id вещи должен быть больше 0") long id
    ) {
        return httpClient.get(API_PREFIX, id);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam(name = "text", required = false, defaultValue = "") String query) {
        return httpClient.get(API_PREFIX + "/search?text=" + query, null);
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<Object> createComment(
            @RequestHeader("${shareit.api.auth.userheader}") @Positive(message = "id пользователя должен быть больше 0") long userId,
            @PathVariable(name = "id") @Positive(message = "id вещи должен быть больше 0") long itemId,
            @Valid @RequestBody CommentDto commentDto
    ) {
        return httpClient.post(API_PREFIX + "/" + itemId + "/comment", userId, commentDto);
    }
}
