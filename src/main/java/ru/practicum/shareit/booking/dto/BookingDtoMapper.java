package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.user.dto.UserDtoMapper;

public class BookingDtoMapper {
    public static Booking mapCreateDtoToModel(BookingCreateDto createDto) {
        return Booking.builder()
                .id(createDto.getId())
                .start(createDto.getStart())
                .end(createDto.getEnd())
                .build();
    }

    public static BookingResponseDto mapToResponseDto(Booking booking) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(UserDtoMapper.mapToDto(booking.getUser()))
                .item(ItemDtoMapper.mapToDto(booking.getItem()))
                .build();
    }
}
