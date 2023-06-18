package ru.practicum.shareit.request.mapper;

import lombok.Generated;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.request.entity.ItemRequest;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Generated
public class ItemRequestMapper {
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;

    public ItemRequest toItemRequest(ItemRequestDto itemRequestDto, Long userId) {
        return ItemRequest.builder()
                .description(itemRequestDto.getDescription())
                .requester(userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException("User not found for create request")))
                .build();
    }

    public ItemResponseDto itemRequestDto(ItemRequest request) {
        return ItemResponseDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(request.getItems() != null ?
                        request.getItems().stream()
                                .map(itemMapper::toItemRequestDto)
                                .collect(Collectors.toList()) : new ArrayList<>())
                .build();
    }
}
