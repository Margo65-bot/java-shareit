package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Set;

public interface UserStorage {
    List<User> getAll();

    User getById(long id);

    void deleteById(long id);

    User create(User user);

    User update(User user);

    Set<User> getByEmail(String email);
}
