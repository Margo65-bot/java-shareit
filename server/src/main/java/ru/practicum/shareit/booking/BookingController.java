package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
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
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingStateQueryParam;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;

    @GetMapping("/{id}")
    public BookingResponseDto getByIdForOwnerOrBooker(
            @RequestHeader("${shareit.api.auth.userheader}") long userId,
            @PathVariable(name = "id") long bookingId
    ) {
        return bookingService.getByIdForOwnerOrBooker(userId, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> getAllForBooker(
            @RequestHeader("${shareit.api.auth.userheader}") long userId,
            @RequestParam(required = false, defaultValue = "ALL") BookingStateQueryParam state
    ) {
        return bookingService.getAllForBooker(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getAllForOwner(
            @RequestHeader("${shareit.api.auth.userheader}") long userId,
            @RequestParam(required = false, defaultValue = "ALL") BookingStateQueryParam state
    ) {
        return bookingService.getAllForOwner(userId, state);
    }

    @PostMapping
    public BookingResponseDto create(
            @RequestHeader("${shareit.api.auth.userheader}") long userId,
            @RequestBody BookingCreateDto createDto
    ) {
        return bookingService.create(userId, createDto);
    }

    @PatchMapping("/{id}")
    public BookingResponseDto updateStateByOwner(
            @RequestHeader("${shareit.api.auth.userheader}") long userId,
            @PathVariable(name = "id") long bookingId,
            @RequestParam boolean approved
    ) {
        return bookingService.updateStateByOwner(userId, bookingId, approved);
    }
}
