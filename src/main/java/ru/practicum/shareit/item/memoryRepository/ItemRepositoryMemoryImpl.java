package ru.practicum.shareit.item.memoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.user.memoryRepository.UserRepositoryMemory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ItemRepositoryMemoryImpl implements ItemRepositoryMemory {

    private static final String ERROR_USER_NOT_FOUND = "User ID not found";
    private final Map<Long, List<Item>> itemMap = new HashMap<>();
    private final UserRepositoryMemory repository;
    private long itemGenerationId = 0;


    @Override
    public List<Item> getAllItems(Long userId) {
        if (!itemMap.containsKey(userId)) {
            log.error(ERROR_USER_NOT_FOUND);
            throw new ItemNotFoundException(ERROR_USER_NOT_FOUND);
        }
        return itemMap.get(userId);
    }

    @Override
    public Item getItemById(Long itemId) {
        return itemMap.values().stream()
                .flatMap(List::stream)
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ItemNotFoundException("Item not found for user"));
    }

    @Override
    public Item createItem(Long userId, Item item) {
        if (!repository.getUserMap().containsKey(userId)) {
            log.error(ERROR_USER_NOT_FOUND);
            throw new UserNotFoundException(ERROR_USER_NOT_FOUND);
        }
        itemMap.compute(userId, (usersId, userItems) -> {
            if (userItems == null) {
                userItems = new ArrayList<>();
            }
            item.setId(++itemGenerationId);
            item.setOwner(repository.findUserById(userId));
            userItems.add(item);
            return userItems;
        });
        return getItemById(item.getId());
    }

    @Override
    public Item updateItem(Long userId, Item item, Long itemId) {
        validationDataForUpdateItem(item, itemId);
        if (!itemMap.containsKey(userId)) {
            log.error("Item ID not found");
            throw new ItemNotFoundException("Item ID not found");
        }
        if (isCheckOwnerItem(userId, itemId)) {
            item.setId(itemId);
            item.setOwner(repository.findUserById(userId));
            List<Item> updatingItem = List.of(item);
            itemMap.merge(userId, updatingItem, (oldVal, newVal) -> newVal);
        }
        return getItemById(itemId);

    }

    @Override
    public List<Item> searchItemForText(String text) {
        if (text.isEmpty() || text.isBlank()) {
            return new ArrayList<>();
        }
        return itemMap.values().stream()
                .flatMap(List::stream)
                .filter(item -> isExistsText(text, item))
                .collect(Collectors.toList());

    }

    private boolean isExistsText(String text, Item item) {
        return (item.getName().toLowerCase().contains(text.toLowerCase())
                || item.getDescription().toLowerCase().contains(text.toLowerCase()))
                && item.getAvailable().equals(true);
    }

    private void validationDataForUpdateItem(Item item, Long itemId) {
        if (item.getName() == null) {
            item.setName(getItemById(itemId).getName());
        }
        if (item.getDescription() == null) {
            item.setDescription(getItemById(itemId).getDescription());
        }
        if (item.getAvailable() == null) {
            item.setAvailable(getItemById(itemId).getAvailable());
        }
    }

    private boolean isCheckOwnerItem(Long userId, Long itemId) {
        return getItemById(itemId).getOwner().equals(repository.findUserById(userId));
    }
}
