package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.entity.Comment;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.validated.Marker;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;


@Validated
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
                @RequestParam(name = "from", required = false, defaultValue = "0") @Min(0) Integer limit,
                @RequestParam(required = false, defaultValue = "20") @Min(1) @Max(50) Integer size) {
        return service.getAllItems(userId, limit, size);
    }

    @GetMapping("/{itemId}")
    public ItemDtoWithBooking getItemById(@PathVariable("itemId") Long itemId,
                                          @RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId) {
        return service.getItemById(itemId, userId);
    }

    @PostMapping
    @Validated(Marker.OnCreate.class)
    public ItemDto createItem(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                              @Valid @RequestBody ItemDto itemDto) {
        Item item = mapper.toItem(itemDto, userId);
        return mapper.toItemDto(service.createItem(userId, item));
    }

    @PatchMapping("/{itemId}")
    @Validated(Marker.OnUpdate.class)
    public ItemDto updateItem(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                              @Valid @RequestBody ItemDto itemDto, @PathVariable("itemId") Long itemId) {
        Item item = mapper.toItem(itemDto, userId);
        return mapper.toItemDto(service.updateItem(userId, item, itemId));
    }

    @GetMapping("/search")
    public List<ItemDto>
    searchItemForText(@RequestParam("text") String text,
                      @RequestParam(name = "from", required = false, defaultValue = "0") @Min(0) Integer limit,
                      @RequestParam(required = false, defaultValue = "20") @Min(1) @Max(50) Integer size) {
        return service.searchItemForText(text, limit, size).stream()
                .map(mapper::toItemDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                                    @Valid @RequestBody CommentDtoRequest commentDtoRequest,
                                    @PathVariable("itemId") Long itemId) {
        Comment comment = commentMapper.toComment(commentDtoRequest);
        return commentMapper.toCommentDto(service.createComment(userId, comment, itemId));
    }
}
