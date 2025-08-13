package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryItemStorage implements ItemStorage {
    Map<Long, Item> items = new HashMap<>();

    @Override
    public List<Item> getAll() {
        return items.values().stream().toList();
    }

    @Override
    public Item getById(long id) {
        if (!items.containsKey(id)) {
            throw new NotFoundException("Вещь с id " + id + " не найдена");
        }
        return items.get(id);
    }

    @Override
    public void deleteById(long id) {
        items.remove(id);
    }

    @Override
    public Item create(Item item) {
        item.setId(getNextId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        long id = item.getId();
        if (!items.containsKey(id)) {
            throw new NotFoundException("Вещь с id " + id + " не найдена");
        }
        items.put(id, item);
        return item;
    }

    @Override
    public List<Item> getItemsByUserId(long id) {
        return items.values().stream()
                .filter(item -> item.getOwnerId().equals(id))
                .toList();
    }

    @Override
    public List<Item> searchItems(String query) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>();
        }
        String lowerQuery = query.toLowerCase();
        return items.values().stream()
                .filter(item -> (item.getName().toLowerCase().contains(lowerQuery) ||
                        item.getDescription().equals(lowerQuery)) &&
                        item.getAvailable())
                .toList();
    }

    private long getNextId() {
        long currentMaxId = items.keySet().stream()
                .max(Long::compareTo)
                .orElse(0L);
        return ++currentMaxId;
    }
}
