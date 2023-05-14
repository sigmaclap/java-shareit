package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;

    @Override
    public List<Item> getAllItems(Long userId) {
        return repository.getAllItems(userId);
    }

    @Override
    public Item getItemById(Long itemId) {
        return repository.getItemById(itemId);
    }

    @Override
    public Item createItem(Long userId, Item item) {
        return repository.createItem(userId, item);
    }

    @Override
    public Item updateItem(Long userId, Item item, Long itemId) {
        return repository.updateItem(userId, item, itemId);
    }

    @Override
    public List<Item> searchItemForText(String text) {
        return repository.searchItemForText(text);
    }
}
