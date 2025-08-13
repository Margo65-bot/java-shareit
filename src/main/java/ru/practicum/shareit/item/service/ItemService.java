package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> getAll();

    ItemDto getById(long id);

    void deleteById(long id);

    ItemDto create(long userId, ItemDto itemDto);

    ItemDto update(long itemId, long userId, ItemDto itemDto);

    List<ItemDto> getItemsByUserId(long id);

    List<ItemDto> searchItems(String query);
}
