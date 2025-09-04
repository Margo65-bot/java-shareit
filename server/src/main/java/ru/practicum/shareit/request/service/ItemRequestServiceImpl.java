package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoMapper;
import ru.practicum.shareit.request.dto.RespondingItem;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.dto.UserDtoMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestStorage itemRequestStorage;
    private final ItemStorage itemStorage;
    private final UserService userService;
    private final UserStorage userStorage;

    @Override
    public List<ItemRequestDto> getAllByUserId(long userId) {
        log.info("Запрос на получение списка заявок пользователя с id {} вместе с данными об ответах на них", userId);
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        List<ItemRequestDto> requestDtoList = itemRequestStorage.findByUserIdOrderByCreatedDesc(userId).stream()
                .map(ItemRequestDtoMapper::mapToDto)
                .toList();
        return setRespondingItem(requestDtoList);
    }

    @Override
    public List<ItemRequestDto> getAllOther(long userId) {
        log.info("Запрос на получение списка заявок без пользователя с id {}", userId);
        return itemRequestStorage.findAllByUserIdNotOrderByCreatedDesc(userId).stream()
                .map(ItemRequestDtoMapper::mapToDto)
                .toList();
    }

    @Override
    public ItemRequestDto getById(long id) {
        log.info("Запрос на получение заявки с id {}", id);
        ItemRequest request = itemRequestStorage.findById(id).orElseThrow(() -> new NotFoundException("Запрос с id " + id + " не найден"));
        ItemRequestDto requestDto = ItemRequestDtoMapper.mapToDto(request);
        requestDto.setItems(itemStorage.findAllByRequestId(id));
        return requestDto;
    }

    @Override
    public ItemRequestDto create(long userId, ItemRequestDto requestDto) {
        log.info("Запрос на создание заявки от пользователя с id {} с данными: {}", userId, requestDto);
        ItemRequest request = ItemRequestDtoMapper.mapToModel(requestDto);
        request.setCreated(LocalDateTime.now());
        User user = UserDtoMapper.mapToModel(userService.getById(userId));
        request.setUser(user);
        request = itemRequestStorage.save(request);
        return ItemRequestDtoMapper.mapToDto(request);
    }

    private List<ItemRequestDto> setRespondingItem(List<ItemRequestDto> requestDtoList) {
        List<Long> itemRequestIds = requestDtoList.stream()
                .map(ItemRequestDto::getId)
                .toList();

        Map<Long, List<RespondingItem>> respondingList = itemStorage.findAllByRequestIdIn(itemRequestIds).stream()
                .collect(Collectors.groupingBy(RespondingItem::getRequestId));

        requestDtoList.forEach(r -> r.setItems(respondingList.getOrDefault(r.getId(), List.of())));

        return requestDtoList;
    }
}
