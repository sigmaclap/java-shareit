package ru.practicum.shareit.request;

import ru.practicum.shareit.request.entity.ItemRequest;

import java.util.List;

public interface ItemRequestService {
    ItemRequest createItemRequest(ItemRequest itemRequest);

    List<ItemRequest> getAllItemRequestOwner(Long userId);

    ItemRequest findRequestItemById(Long userId, Long requestId);

    List<ItemRequest> findAllUsersRequests(Long userId, Integer limit, Integer count);
}
