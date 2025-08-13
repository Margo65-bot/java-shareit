package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.UserEmailConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public List<UserDto> getAll() {
        log.info("Запрос на получение списка пользователей");
        return userStorage.getAll().stream()
                .map(UserDtoMapper::mapToDto)
                .toList();
    }

    @Override
    public UserDto getById(long id) {
        log.info("Запрос на получение пользователя с id {}", id);
        return UserDtoMapper.mapToDto(userStorage.getById(id));
    }

    @Override
    public void deleteById(long id) {
        log.info("Запрос на удаление пользователя с id {}", id);
        userStorage.deleteById(id);
    }

    @Override
    public UserDto create(UserDto userDto) {
        log.info("Запрос на создание пользователя с данными: {}", userDto);

        String email = userDto.getEmail();

        if (email == null || email.isBlank()) {
            throw new ConditionsNotMetException("Электронная почта не может быть пустой");
        }

        if (!userStorage.getByEmail(email).isEmpty()) {
            throw new UserEmailConflictException("Пользователь с электронной почтой " + email + " уже существует");
        }

        User user = userStorage.create(UserDtoMapper.mapToModel(userDto));
        return UserDtoMapper.mapToDto(user);
    }

    @Override
    public UserDto update(long id, UserDto userDto) {
        log.info("Запрос на обновление пользователя с данными: {}", userDto);

        User user = userStorage.getById(id);

        if (userDto.getName() == null || userDto.getName().isBlank()) {
            userDto.setName(user.getName());
        }

        String email = userDto.getEmail();

        if (email == null || email.isBlank()) {
            userDto.setEmail(user.getEmail());
        }

        if (!userStorage.getByEmail(email).isEmpty()) {
            throw new UserEmailConflictException("Пользователь с электронной почтой " + email + " уже существует");
        }

        userDto.setId(id);

        user = userStorage.update(UserDtoMapper.mapToModel(userDto));
        return UserDtoMapper.mapToDto(user);
    }
}
