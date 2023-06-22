package ru.practicum.gateway.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.gateway.item.dto.CommentDtoRequest;
import ru.practicum.gateway.item.dto.ItemDto;
import ru.practicum.gateway.validated.Marker;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private static final String REQUEST_HEADER_SHARER_USER_ID = "X-Sharer-User-Id";
    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object>
    getAllItems(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                @RequestParam(name = "from", defaultValue = "0") @Min(0) Integer limit,
                @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer size) {
        return itemClient.getAllItems(userId, limit, size);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable Long itemId,
                                              @RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId) {
        return itemClient.getItemById(itemId, userId);
    }

    @PostMapping
    @Validated(Marker.OnCreate.class)
    public ResponseEntity<Object> createItem(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                                             @Valid @RequestBody ItemDto itemDto) {
        log.info("Create item ={}", itemDto);
        return itemClient.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    @Validated(Marker.OnUpdate.class)
    public ResponseEntity<Object> updateItem(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                                             @Valid @RequestBody ItemDto itemDto, @PathVariable Long itemId) {
        log.info("Update item ={}", itemDto);
        return itemClient.updateItem(userId, itemDto, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object>
    searchItemForText(@RequestParam String text,
                      @RequestParam(name = "from", defaultValue = "0") @Min(0) Integer limit,
                      @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer size) {
        log.info("Get items with text {}, from={}, size={}", text, limit, size);
        return itemClient.searchItemForText(text, limit, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object>
    createComment(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                  @Valid @RequestBody CommentDtoRequest commentDtoRequest,
                  @PathVariable Long itemId) {
        log.info("Create comment for item with id {}", itemId);
        return itemClient.createComment(userId, commentDtoRequest, itemId);
    }
}
