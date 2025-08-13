package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    List<Item> getAll();

    Item getById(long id);

    void deleteById(long id);

    Item create(Item item);

    Item update(Item item);

    List<Item> getItemsByUserId(long id);

    List<Item> searchItems(String query);
}
