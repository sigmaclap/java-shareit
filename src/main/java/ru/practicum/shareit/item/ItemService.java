package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    List<Item> getAllItems(Long userId);

    Item getItemById(Long itemId);

    Item createItem(Long userId, Item item);

    Item updateItem(Long userId, Item item, Long itemId);

    List<Item> searchItemForText(String text);
}
