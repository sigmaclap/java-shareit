package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.ItemRequestNotFoundException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.request.entity.ItemRequest;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {

    private static final String ERROR_MESSAGE_USER_WITH_ID_404 = "User with id not found: ";

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemRequest createItemRequest(ItemRequest itemRequest) {
        return itemRequestRepository.save(itemRequest);
    }

    @Override
    public List<ItemRequest> getAllItemRequestOwner(Long userId) {
        validateExistsUser(userId);
        List<ItemRequest> itemRequestsOwner = itemRequestRepository.findAllByRequester_IdOrderByCreatedDesc(userId);
        if (itemRequestsOwner.isEmpty()) {
            return new ArrayList<>();
        }
        saveItemsInRequest(itemRequestsOwner);
        return itemRequestsOwner;
    }

    @Override
    public ItemRequest findRequestItemById(Long userId, Long requestId) {
        validateExistsUser(userId);
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new ItemRequestNotFoundException("Item request not found"));
        request.setItems(itemRepository.findAllByItemRequest_Id(request.getId()));
        return request;
    }

    @Override
    public List<ItemRequest> findAllUsersRequests(Long userId, Integer limit, Integer count) {
        validateExistsUser(userId);
        List<ItemRequest> itemRequestsOtherUsers = itemRequestRepository
                .findAllByRequester_IdNotOrderByCreatedDesc(userId, PageRequest.of(limit / count, count))
                .getContent();
        if (itemRequestsOtherUsers.isEmpty()) {
            return new ArrayList<>();
        }
        saveItemsInRequest(itemRequestsOtherUsers);
        return itemRequestsOtherUsers;
    }

    private void saveItemsInRequest(List<ItemRequest> itemRequestsOwner) {
        for (ItemRequest request : itemRequestsOwner) {
            List<Item> itemRequests = itemRepository.findAllByItemRequest_Id(request.getId());
            request.setItems(itemRequests);
        }
    }

    private void validateExistsUser(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.error(ERROR_MESSAGE_USER_WITH_ID_404 + userId);
            throw new UserNotFoundException(ERROR_MESSAGE_USER_WITH_ID_404 + userId);
        }
    }
}
