package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService requestService;

    @GetMapping
    public List<ItemRequestDto> getAllByUserId(
            @RequestHeader("${shareit.api.auth.userheader}") long userId
    ) {
        return requestService.getAllByUserId(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllOther(
            @RequestHeader("${shareit.api.auth.userheader}") long userId
    ) {
        return requestService.getAllOther(userId);
    }

    @GetMapping("/{id}")
    public ItemRequestDto getById(
            @PathVariable(name = "id") long id
    ) {
        return requestService.getById(id);
    }

    @PostMapping
    public ItemRequestDto create(
            @RequestHeader("${shareit.api.auth.userheader}") long userId, @RequestBody ItemRequestDto requestDto
    ) {
        return requestService.create(userId, requestDto);
    }
}
