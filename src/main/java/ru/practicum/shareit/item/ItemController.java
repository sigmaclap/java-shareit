package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.validated.Marker;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;


@Validated
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService service;
    private final ItemMapper mapper;
    private static final String REQUEST_HEADER_SHARER_USER_ID = "X-Sharer-User-Id";

    @GetMapping
    public List<ItemDto> getAllItems(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId) {
        return service.getAllItems(userId)
                .stream()
                .map(mapper::toItemDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable("itemId") Long itemId) {
        return mapper.toItemDto(service.getItemById(itemId));
    }

    @PostMapping
    @Validated(Marker.OnCreate.class)
    public ItemDto createItem(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                              @Valid @RequestBody ItemDto itemDto) {
        Item item = mapper.toItem(itemDto);
        return mapper.toItemDto(service.createItem(userId, item));
    }

    @PatchMapping("/{itemId}")
    @Validated(Marker.OnUpdate.class)
    public ItemDto updateItem(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                              @Valid @RequestBody ItemDto itemDto, @PathVariable("itemId") Long itemId) {
        Item item = mapper.toItem(itemDto);
        return mapper.toItemDto(service.updateItem(userId, item, itemId));
    }

    @GetMapping("/search")
    public List<ItemDto> searchItemForText(@RequestParam("text") String text) {
        return service.searchItemForText(text).stream()
                .map(mapper::toItemDto)
                .collect(Collectors.toList());
    }
}
