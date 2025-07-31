package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    @GetMapping("/{id}")
    public ItemDto getById(@PathVariable long id) {

        return itemService.getById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable long id) {
        itemService.deleteById(id);
    }

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") long userId, @Valid @RequestBody ItemDto itemDto) {
        return itemService.create(userId, itemDto);
    }

    @PatchMapping("/{id}")
    public ItemDto update(@PathVariable(name = "id") long itemId, @RequestHeader("X-Sharer-User-Id") long userId, @Valid @RequestBody ItemDto itemDto) {
        return itemService.update(itemId, userId, itemDto);
    }

    @GetMapping
    public List<ItemDto> getItems(@RequestHeader("X-Sharer-User-Id") Long id) {
        if (id == null) {
            return itemService.getAll();
        }
        return itemService.getItemsByUserId(id);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam(name = "text", required = false) String query) {
        return itemService.searchItems(query);
    }
}
