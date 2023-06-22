package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.entity.Comment;
import ru.practicum.shareit.item.entity.Item;

import java.util.List;

public interface ItemService {
    List<ItemDtoWithBooking> getAllItems(Long userId, Integer limit, Integer size);

    ItemDtoWithBooking getItemById(Long itemId, Long userId);

    Item createItem(Long userId, Item item);

    Item updateItem(Long userId, Item item, Long itemId);

    List<Item> searchItemForText(String text, Integer limit, Integer size);

    Comment createComment(Long userId, Comment comment, Long itemId);
}
