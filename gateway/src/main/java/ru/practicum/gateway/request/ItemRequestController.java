package ru.practicum.gateway.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.gateway.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private static final String REQUEST_HEADER_SHARER_USER_ID = "X-Sharer-User-Id";
    private final ItemRequestClient itemRequestClient;

    @GetMapping
    public ResponseEntity<Object>
    getAllItemRequestOwner(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId) {
        log.info("Get all item request by owner " + userId);
        return itemRequestClient.getAllItemRequestOwner(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findRequestItemById(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                                                      @PathVariable Long requestId) {
        log.info("Get item request id: " + requestId);
        return itemRequestClient.findRequestItemById(userId, requestId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object>
    findAllUsersRequests(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                         @RequestParam(name = "from", defaultValue = "0") @Min(0) Integer limit,
                         @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer size) {
        log.info("Get all users from items requests {}, from={}, size={}", userId, limit, size);
        return itemRequestClient.findAllUsersRequests(userId, limit, size);
    }

    @PostMapping
    public ResponseEntity<Object> createItemRequest(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                                                    @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Create Item Request {}, userId = {}", itemRequestDto, userId);
        return itemRequestClient.createItemRequest(itemRequestDto, userId);
    }
}
