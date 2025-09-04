package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingStateQueryParam;
import ru.practicum.shareit.client.HttpClient;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private static final String API_PREFIX = "/bookings";
    private final HttpClient httpClient;

    @GetMapping("/{id}")
    public ResponseEntity<Object> getByIdForOwnerOrBooker(
            @RequestHeader("${shareit.api.auth.userheader}") @Positive(message = "id пользователя должен быть больше 0") long userId,
            @PathVariable(name = "id") @Positive(message = "id бронирования должен быть больше 0") long bookingId
    ) {
        return httpClient.get(API_PREFIX + "/" + bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllForBooker(
            @RequestHeader("${shareit.api.auth.userheader}") @Positive(message = "id пользователя должен быть больше 0") long userId,
            @RequestParam(required = false, defaultValue = "ALL") BookingStateQueryParam state
    ) {
        return httpClient.get(API_PREFIX + "?state=" + state.toString(), userId);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllForOwner(
            @RequestHeader("${shareit.api.auth.userheader}") @Positive(message = "id пользователя должен быть больше 0") long userId,
            @RequestParam(required = false, defaultValue = "ALL") BookingStateQueryParam state
    ) {
        return httpClient.get(API_PREFIX + "/owner?state=" + state.toString(), userId);
    }

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader("${shareit.api.auth.userheader}") @Positive(message = "id пользователя должен быть больше 0") long userId,
            @RequestBody @Valid BookingCreateDto createDto
    ) {
        return httpClient.post(API_PREFIX, userId, createDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateStateByOwner(
            @RequestHeader("${shareit.api.auth.userheader}") @Positive(message = "id пользователя должен быть больше 0") long userId,
            @PathVariable(name = "id") @Positive(message = "id бронирования должен быть больше 0") long bookingId,
            @RequestParam @Pattern(regexp = "(?i)true|false") String approved
    ) {
        return httpClient.patch(API_PREFIX + "/" + bookingId + "?approved=" + approved, userId, null);
    }
}
