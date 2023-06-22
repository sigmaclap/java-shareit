package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.entity.Comment;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService service;
    private final ItemMapper mapper;
    private final CommentMapper commentMapper;
    private static final String REQUEST_HEADER_SHARER_USER_ID = "X-Sharer-User-Id";

    @GetMapping
    public List<ItemDtoWithBooking>
    getAllItems(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                @RequestParam(name = "from", defaultValue = "0") Integer limit,
                @RequestParam(defaultValue = "20") Integer size) {
        return service.getAllItems(userId, limit, size);
    }

    @GetMapping("/{itemId}")
    public ItemDtoWithBooking getItemById(@PathVariable Long itemId,
                                          @RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId) {
        return service.getItemById(itemId, userId);
    }

    @PostMapping
    public ItemDto createItem(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                              @RequestBody ItemDto itemDto) {
        Item item = mapper.toItem(itemDto, userId);
        return mapper.toItemDto(service.createItem(userId, item));
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                              @RequestBody ItemDto itemDto, @PathVariable Long itemId) {
        Item item = mapper.toItem(itemDto, userId);
        return mapper.toItemDto(service.updateItem(userId, item, itemId));
    }

    @GetMapping("/search")
    public List<ItemDto>
    searchItemForText(@RequestParam String text,
                      @RequestParam(name = "from", defaultValue = "0")Integer limit,
                      @RequestParam(defaultValue = "20") Integer size) {
        return service.searchItemForText(text, limit, size).stream()
                .map(mapper::toItemDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                                    @RequestBody CommentDtoRequest commentDtoRequest,
                                    @PathVariable Long itemId) {
        Comment comment = commentMapper.toComment(commentDtoRequest);
        return commentMapper.toCommentDto(service.createComment(userId, comment, itemId));
    }
}
