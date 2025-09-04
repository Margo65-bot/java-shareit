package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public UserDto getById(
            @PathVariable long id
    ) {
        return userService.getById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteById(
            @PathVariable long id
    ) {
        userService.deleteById(id);
    }

    @PostMapping
    public UserDto create(
            @RequestBody UserDto userDto
    ) {
        return userService.create(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto update(
            @PathVariable long id,
            @RequestBody UserDto userDto
    ) {
        return userService.update(id, userDto);
    }
}
