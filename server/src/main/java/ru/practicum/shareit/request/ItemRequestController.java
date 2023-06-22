package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.request.entity.ItemRequest;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;
    private final ItemRequestMapper itemRequestMapper;
    private static final String REQUEST_HEADER_SHARER_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ItemResponseDto createItemRequest(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                                             @RequestBody ItemRequestDto itemRequestDto) {
        ItemRequest itemRequest = itemRequestMapper.toItemRequest(itemRequestDto, userId);
        return itemRequestMapper.itemRequestDto(itemRequestService.createItemRequest(itemRequest));
    }

    @GetMapping
    public List<ItemResponseDto> getAllItemRequestOwner(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId) {
        return itemRequestService.getAllItemRequestOwner(userId).stream()
                .map(itemRequestMapper::itemRequestDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{requestId}")
    public ItemResponseDto findRequestItemById(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                                               @PathVariable Long requestId) {
        return itemRequestMapper.itemRequestDto(itemRequestService.findRequestItemById(userId, requestId));
    }

    @GetMapping("/all")
    public List<ItemResponseDto>
    findAllUsersRequests(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                         @RequestParam(name = "from", defaultValue = "0") Integer limit,
                         @RequestParam(defaultValue = "20") Integer size) {
        return itemRequestService.findAllUsersRequests(userId, limit, size).stream()
                .map(itemRequestMapper::itemRequestDto)
                .collect(Collectors.toList());
    }
}
