package ru.practicum.shareit.item.memoryRepository;

import ru.practicum.shareit.item.entity.Item;

import java.util.List;

public interface ItemRepositoryMemory {
    List<Item> getAllItems(Long userId);

    Item getItemById(Long itemId);

    Item createItem(Long userId, Item item);

    Item updateItem(Long userId, Item item, Long itemId);

    List<Item> searchItemForText(String text);
}
