package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemRepository {
    Collection<Item> getAllUserItems(Long userId);

    Optional<Item> getItemById(Long id);

    Item createItem(Item item);

    Item updateItem(Item item);

    boolean deleteItem(Long id);

    Collection<Item> searchItem(String text);
}
