package ru.practicum.shareit.item.mapper;

import lombok.Generated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoForRequestList;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.user.UserService;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Generated
public class ItemMapper {
    private final UserService service;
    private final ItemRequestService requestService;

    public ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .owner(item.getOwner() != null ? item.getOwner().getId() : null)
                .requestId(item.getItemRequest() != null ? item.getItemRequest().getId() : null)
                .build();
    }

    public Item toItem(ItemDto itemDto, Long userId) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(itemDto.getOwner() != null ? service.findUserById(itemDto.getOwner()) : null)
                .itemRequest(itemDto.getRequestId() != null ?
                        requestService.findRequestItemById(userId, itemDto.getRequestId()) : null)
                .build();
    }

    public ItemDtoWithBooking toItemDtoBooking(Item item, List<CommentDto> comments) {
        return ItemDtoWithBooking.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .comments(comments)
                .build();
    }

    public ItemDtoForRequestList toItemRequestDto(Item item) {
        return ItemDtoForRequestList.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId((item.getItemRequest().getId() != null ? item.getItemRequest().getId() : null))
                .build();
    }
}
