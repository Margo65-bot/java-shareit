package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingStateQueryParam;

import java.util.List;

public interface BookingService {
    BookingResponseDto getByIdForOwnerOrBooker(long userId, long bookingId);

    List<BookingResponseDto> getAllForBooker(long userId, BookingStateQueryParam state);

    List<BookingResponseDto> getAllForOwner(long userId, BookingStateQueryParam state);

    BookingResponseDto create(long userId, BookingCreateDto createDto);

    BookingResponseDto updateStateByOwner(long userId, long bookingId, boolean approved);
}
