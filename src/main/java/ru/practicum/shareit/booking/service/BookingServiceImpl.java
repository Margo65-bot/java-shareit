package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDtoMapper;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingStateQueryParam;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoMapper;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingStorage bookingStorage;
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final UserService userService;

    @Override
    public BookingResponseDto getByIdForOwnerOrBooker(long userId, long bookingId) {
        log.info("Запрос на получение бронирования с id {} от пользователя с id {}", bookingId, userId);
        userNotExistsThrowNotFound(userId);

        Booking booking = getBookingOrThrowNotFound(bookingId);

        if (booking.getItem().getUser().getId() != userId && booking.getUser().getId() != userId) {
            throw new ConditionsNotMetException("Пользователь с id " + userId
                    + "не является владельцем или арендатором вещи с id " + booking.getItem().getId());
        }

        return BookingDtoMapper.mapToResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAllForBooker(long userId, BookingStateQueryParam state) {
        log.info("Запрос на получение всех бронирований пользователя с id {} в состоянии - {}", userId, state.name());
        userNotExistsThrowNotFound(userId);

        List<Booking> bookings = switch (state) {
            case ALL -> bookingStorage.findAllByUserIdOrderByStartDesc(userId);
            case CURRENT -> bookingStorage.findCurrentByUserIdOrderByStartDesc(userId);
            case PAST -> bookingStorage.findPastByUserIdOrderByStartDesc(userId);
            case FUTURE -> bookingStorage.findFutureByUserIdOrderByStartDesc(userId);
            case WAITING -> bookingStorage.findWaitingByUserIdOrderByStartDesc(userId);
            case REJECTED -> bookingStorage.findRejectedByUserIdOrderByStartDesc(userId);
        };

        return bookings.stream()
                .map(BookingDtoMapper::mapToResponseDto)
                .toList();
    }

    @Override
    public List<BookingResponseDto> getAllForOwner(long userId, BookingStateQueryParam state) {
        log.info("Запрос на получение всех бронирований вещей пользователя с id {} в состоянии - {}", userId, state.name());
        userNotExistsThrowNotFound(userId);

        List<Booking> bookings = switch (state) {
            case ALL -> bookingStorage.findAllByItemOwnerIdOrderByStartDesc(userId);
            case CURRENT -> bookingStorage.findCurrentByItemOwnerIdOrderByStartDesc(userId);
            case PAST -> bookingStorage.findPastByItemOwnerIdOrderByStartDesc(userId);
            case FUTURE -> bookingStorage.findFutureByItemOwnerIdOrderByStartDesc(userId);
            case WAITING -> bookingStorage.findWaitingByItemOwnerIdOrderByStartDesc(userId);
            case REJECTED -> bookingStorage.findRejectedByItemOwnerIdOrderByStartDesc(userId);
        };

        return bookings.stream()
                .map(BookingDtoMapper::mapToResponseDto)
                .toList();
    }

    @Override
    public BookingResponseDto create(long userId, BookingCreateDto createDto) {
        log.info("Запрос на создание бронирования от пользователя с id {}. Данные бронирования - {}", userId, createDto);
        UserDto booker = userService.getById(userId);
        Item item = itemStorage.findById(createDto.getItemId()).orElseThrow(() -> new NotFoundException("Вещь с id " + createDto.getItemId() + " не найдена"));
        if (!item.getAvailable()) {
            throw new ConditionsNotMetException("Вещь с id " + createDto.getItemId() + " не доступна для аренды");
        }
        Booking booking = BookingDtoMapper.mapCreateDtoToModel(createDto);

        booking.setUser(UserDtoMapper.mapToModel(booker));
        booking.setItem(item);
        booking.setStatus(BookingState.WAITING);

        bookingStorage.save(booking);
        return BookingDtoMapper.mapToResponseDto(booking);
    }

    @Override
    public BookingResponseDto updateStateByOwner(long userId, long bookingId, boolean approved) {
        log.info("Запрос на подтверждение бронирования: id пользователя - {}, id бронирования - {}, approved - {}", userId, bookingId, approved);
        Booking booking = getBookingOrThrowNotFound(bookingId);

        if (booking.getItem().getUser().getId() != userId) {
            throw new ConditionsNotMetException("Пользователь с id " + userId
                    + " не является владельцем вещи с id " + booking.getItem().getId());
        }

        if (approved) {
            booking.setStatus(BookingState.APPROVED);
        } else {
            booking.setStatus(BookingState.REJECTED);
        }

        bookingStorage.save(booking);
        return BookingDtoMapper.mapToResponseDto(booking);
    }

    private void userNotExistsThrowNotFound(long id) {
        if (!userStorage.existsById(id)) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }

    private Booking getBookingOrThrowNotFound(long id) {
        return bookingStorage.findByIdWithUserAndItemWithOwner(id)
                .orElseThrow(() -> new NotFoundException("Аренда с id " + id + " не найдена"));
    }
}
