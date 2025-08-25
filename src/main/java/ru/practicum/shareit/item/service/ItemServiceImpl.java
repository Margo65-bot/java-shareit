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
import java.util.Optional;

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
        commentDto.setComments(getListOfCommentDto(id));
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
        return itemStorage.findAllByUserId(id).stream()
                .map(i -> {
                    ItemDto infoDto = ItemDtoMapper.mapToDto(i);
                    setBookingInfo(infoDto, i.getId());
                    infoDto.setComments(getListOfCommentDto(i.getId()));
                    return infoDto;
                }).toList();
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

    private void setBookingInfo(ItemDto infoDto, long id) {
        Optional<ItemDto.BookingInfo> lastBooking = bookingStorage.findLastBookingByItemId(id);
        Optional<ItemDto.BookingInfo> nextBooking = bookingStorage.findNextBookingByItemId(id);
        lastBooking.ifPresent(infoDto::setLastBooking);
        nextBooking.ifPresent(infoDto::setNextBooking);
    }

    private Item getItemOrThrowNotFound(long id) {
        return itemStorage.findById(id).orElseThrow(() -> new NotFoundException("Вещь с id " + id + " не найдена"));
    }

    private List<CommentDto> getListOfCommentDto(long id) {
        List<Comment> comments = commentStorage.findAllByItemIdWithUserAndItem(id);
        return comments.stream()
                .map(CommentDtoMapper::mapToDto)
                .toList();
    }
}
