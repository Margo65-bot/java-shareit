package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
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
        return userStorage.findAll().stream()
                .map(UserDtoMapper::mapToDto)
                .toList();
    }

    @Override
    public UserDto getById(long id) {
        log.info("Запрос на получение пользователя с id {}", id);
        return UserDtoMapper.mapToDto(userStorage.findById(id).orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден")));
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

        if (!userStorage.findByEmailContainingIgnoreCase(email).isEmpty()) {
            throw new UserEmailConflictException("Пользователь с электронной почтой " + email + " уже существует");
        }

        if (userDto.getName() == null || userDto.getName().isBlank()) {
            userDto.setName(email);
        }

        User user = userStorage.save((UserDtoMapper.mapToModel(userDto)));
        return UserDtoMapper.mapToDto(user);
    }

    @Override
    public UserDto update(long id, UserDto userDto) {
        log.info("Запрос на обновление пользователя с данными: {}", userDto);

        UserDto userFind = getById(id);

        if (userDto.getName() == null || userDto.getName().isBlank()) {
            userDto.setName(userFind.getName());
        }

        String email = userDto.getEmail();

        if (email == null || email.isBlank()) {
            userDto.setEmail(userFind.getEmail());
        }

        List<User> usersWithSameEmail = userStorage.findByEmailContainingIgnoreCase(email)
                .stream().filter(user -> user.getId() != id)
                .toList();

        if (!usersWithSameEmail.isEmpty()) {
            throw new UserEmailConflictException("Пользователь с электронной почтой " + email + " уже существует");
        }

        userDto.setId(id);

        User user = userStorage.save(UserDtoMapper.mapToModel(userDto));
        return UserDtoMapper.mapToDto(user);
    }
}
