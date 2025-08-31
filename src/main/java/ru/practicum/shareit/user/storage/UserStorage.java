package ru.practicum.shareit.user.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage extends JpaRepository<User, Long> {
    List<User> findByEmailContainingIgnoreCase(String email);
}
