package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    List<ItemRequestDto> getAllByUserId(long userId);

    List<ItemRequestDto> getAllOther(long userId);

    ItemRequestDto getById(long id);

    ItemRequestDto create(long userId, ItemRequestDto requestDto);
}
