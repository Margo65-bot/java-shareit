package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public List<User> getAll() {
        return users.values().stream().toList();
    }

    @Override
    public User getById(long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        return users.get(id);
    }

    @Override
    public void deleteById(long id) {
        users.remove(id);
    }

    @Override
    public User create(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        long id = user.getId();
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        users.put(id, user);
        return user;
    }

    @Override
    public Set<User> getByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .collect(Collectors.toSet());
    }

    private long getNextId() {
        long currentMaxId = users.keySet().stream()
                .max(Long::compareTo)
                .orElse(0L);
        return ++currentMaxId;
    }
}
