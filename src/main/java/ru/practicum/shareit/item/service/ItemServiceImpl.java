package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public List<ItemDto> getAll() {
        log.info("Запрос на получение списка вещей");
        return itemStorage.getAll().stream()
                .map(ItemDtoMapper::mapToDto)
                .toList();
    }

    @Override
    public ItemDto getById(long id) {
        log.info("Запрос на получение вещи с id {}", id);
        return ItemDtoMapper.mapToDto(itemStorage.getById(id));
    }

    @Override
    public void deleteById(long id) {
        log.info("Запрос на удаление вещи с id {}", id);
        itemStorage.deleteById(id);
    }

    @Override
    public ItemDto create(long userId, ItemDto itemDto) {
        log.info("Запрос на создание вещи с данными: {}", itemDto);

        User user = userStorage.getById(userId);

        if ((itemDto.getName() == null || itemDto.getName().isBlank()) ||
        (itemDto.getDescription() == null || itemDto.getDescription().isBlank())) {
            throw new ConditionsNotMetException("Название и описание вещи не должно быть пустым");
        }

        if (itemDto.getAvailable() == null) {
            throw new ConditionsNotMetException("Статус доступа к аренде не может быть пустым");
        }

        Item item = ItemDtoMapper.mapToModel(itemDto);
        item.setOwnerId(user.getId());

        return ItemDtoMapper.mapToDto(itemStorage.create(item));
    }

    @Override
    public ItemDto update(long itemId, long userId, ItemDto itemDto) {
        log.info("Запрос на обновление вещи с данными: {}; id пользователя - {}", itemDto, userId);

        User user = userStorage.getById(userId);
        Item item = itemStorage.getById(itemId);

        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            itemDto.setName(item.getName());
        }

        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            itemDto.setDescription(item.getDescription());
        }

        if (itemDto.getAvailable() == null) {
            itemDto.setAvailable(item.getAvailable());
        }

        if (!item.getOwnerId().equals(user.getId())) {
            throw new ConditionsNotMetException("Пользователь с id " + user.getId()
                    + "не является владельцем вещи с id " + item.getId());
        }

        itemDto.setId(itemId);
        item = ItemDtoMapper.mapToModel(itemDto);
        item.setOwnerId(user.getId());

        return ItemDtoMapper.mapToDto(itemStorage.update(item));
    }

    @Override
    public List<ItemDto> getItemsByUserId(long id) {
        log.info("Запрос на получение вещей пользователя с id {}", id);
        User user = userStorage.getById(id);
        return itemStorage.getItemsByUserId(user.getId()).stream()
                .map(ItemDtoMapper::mapToDto)
                .toList();
    }

    @Override
    public List<ItemDto> searchItems(String query) {
        log.info("Запрос на поиск вещей с подстрокой {}", query);
        return itemStorage.searchItems(query).stream()
                .map(ItemDtoMapper::mapToDto)
                .toList();
    }

}
