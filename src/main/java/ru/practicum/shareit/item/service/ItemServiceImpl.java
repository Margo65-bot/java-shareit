package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.storage.CommentStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoMapper;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final CommentStorage commentStorage;
    private final UserService userService;
    private final BookingStorage bookingStorage;

    @Override
    public ItemDto getById(long id) {
        log.info("Запрос на получение вещи с id {}", id);
        ItemDto commentDto = ItemDtoMapper.mapToDto(getItemOrThrowNotFound(id));
        commentDto.setComments(commentStorage.findAllByItemId(id).stream()
                .map(CommentDtoMapper::mapToDto)
                .toList());
        return commentDto;
    }

    @Override
    public void deleteById(long id) {
        log.info("Запрос на удаление вещи с id {}", id);
        itemStorage.deleteById(id);
    }

    @Override
    public ItemDto create(long userId, ItemDto itemDto) {
        log.info("Запрос на создание вещи с данными: {}", itemDto);

        UserDto user = userService.getById(userId);

        if ((itemDto.getName() == null || itemDto.getName().isBlank()) ||
                (itemDto.getDescription() == null || itemDto.getDescription().isBlank())) {
            throw new ConditionsNotMetException("Название и описание вещи не должно быть пустым");
        }

        if (itemDto.getAvailable() == null) {
            throw new ConditionsNotMetException("Статус доступа к аренде не может быть пустым");
        }

        Item item = ItemDtoMapper.mapToModel(itemDto);
        item.setUser(UserDtoMapper.mapToModel(user));

        return ItemDtoMapper.mapToDto(itemStorage.save(item));
    }

    @Override
    public ItemDto update(long itemId, long userId, ItemDto itemDto) {
        log.info("Запрос на обновление вещи с данными: {}; id пользователя - {}", itemDto, userId);

        UserDto user = userService.getById(userId);
        Item item = itemStorage.findByIdWithUser(itemId).orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));

        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            itemDto.setName(item.getName());
        }

        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            itemDto.setDescription(item.getDescription());
        }

        if (itemDto.getAvailable() == null) {
            itemDto.setAvailable(item.getAvailable());
        }

        if (!item.getUser().getId().equals(user.getId())) {
            throw new ConditionsNotMetException("Пользователь с id " + user.getId()
                    + "не является владельцем вещи с id " + item.getId());
        }

        itemDto.setId(itemId);
        item = ItemDtoMapper.mapToModel(itemDto);
        item.setUser(UserDtoMapper.mapToModel(user));

        return ItemDtoMapper.mapToDto(itemStorage.save(item));
    }

    @Override
    public List<ItemDto> getItemsByUserId(long id) {
        log.info("Запрос на получение вещей пользователя с id {}", id);
        if (!userStorage.existsById(id)) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        List<ItemDto> items = itemStorage.findAllByUserId(id).stream()
                .map(ItemDtoMapper::mapToDto)
                .toList();
        Map<Long, List<CommentDto>> commentsMap = commentStorage.findAllByItemOwnerId(id).stream()
                .map(CommentDtoMapper::mapToDto)
                .collect(Collectors.groupingBy(CommentDto::getItemId));


        items = setBookingInfo(items);
        items.forEach(itemDto -> itemDto.setComments(commentsMap.getOrDefault(itemDto.getId(), List.of())));
        return items;
    }

    @Override
    public List<ItemDto> searchItems(String query) {
        log.info("Запрос на поиск вещей с подстрокой {}", query);
        if (query.isBlank()) {
            return List.of();
        }
        return itemStorage.findByQuery(query).stream()
                .map(ItemDtoMapper::mapToDto)
                .toList();
    }

    @Override
    public CommentDto createComment(long userId, long itemId, CommentDto commentDto) {
        log.info("Запрос на создание комментария для вещи с id {} пользователем с id {}. Данные комментария - {}", itemId, userId, commentDto);
        Item item = getItemOrThrowNotFound(itemId);
        UserDto user = userService.getById(userId);

        if (!bookingStorage.existsApprovedPastBooking(itemId, userId)) {
            throw new ConditionsNotMetException("Пользователь не брал вещь c id " + itemId + " в аренду, либо время аренды еще не завершено");
        }

        Comment comment = CommentDtoMapper.mapToModel(commentDto);
        comment.setItem(item);
        comment.setUser(UserDtoMapper.mapToModel(user));
        comment.setCreated(LocalDateTime.now());
        commentStorage.save(comment);

        CommentDto commentRes = CommentDtoMapper.mapToDto(comment);
        commentRes.setAuthorName(user.getName());
        return commentRes;
    }

    private List<ItemDto> setBookingInfo(List<ItemDto> itemDtoList) {
        List<Long> itemIds = itemDtoList.stream()
                .map(ItemDto::getId)
                .toList();

        Map<Long, ItemDto.BookingInfo> lastBookingInfoMap = bookingStorage.findAllLastBookingByItemIds(itemIds).stream()
                .collect(Collectors.toMap(ItemDto.BookingInfo::getItemId, Function.identity()));
        Map<Long, ItemDto.BookingInfo> nextBookingInfoMap = bookingStorage.findAllNextBookingByItemIds(itemIds).stream()
                .collect(Collectors.toMap(ItemDto.BookingInfo::getItemId, Function.identity()));

        return itemDtoList.stream()
                .peek(itemDto -> {
                    long itemId = itemDto.getId();
                    ItemDto.BookingInfo lastBooking = lastBookingInfoMap.get(itemId);
                    if (lastBooking != null) {
                        itemDto.setLastBooking(lastBooking);
                    }
                    ItemDto.BookingInfo nextBooking = nextBookingInfoMap.get(itemId);
                    if (nextBooking != null) {
                        itemDto.setNextBooking(nextBooking);
                    }
                })
                .toList();
    }

    private Item getItemOrThrowNotFound(long id) {
        return itemStorage.findById(id).orElseThrow(() -> new NotFoundException("Вещь с id " + id + " не найдена"));
    }
}
