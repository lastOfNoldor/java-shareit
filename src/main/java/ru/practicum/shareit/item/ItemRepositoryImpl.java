package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Qualifier("itemRepository")
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {
    private final HashMap<Long, Item> tempRepository = new HashMap<>();
    Long counter = 0L;

    @Override
    public Collection<Item> getAllUserItems(Long userId) {
        if (userId == null) {
            throw new ValidationException("отсутствует Id пользователя");
        }
        return tempRepository.values().stream().filter(item -> Objects.equals(item.getOwner().getId(), userId)).collect(Collectors.toList());
    }

    @Override
    public Optional<Item> getItemById(Long id) {
        return tempRepository.values().stream().filter(item -> item.getId().equals(id)).findFirst();
    }

    @Override
    public Item createItem(Item item) {
        item.setId(++counter);
        tempRepository.put(item.getId(), item);
        return item;
    }

    @Override
    public Item updateItem(Item item) {
        tempRepository.put(item.getId(), item);
        return item;
    }

    @Override
    public boolean deleteItem(Long id) {
        tempRepository.remove(id);
        return true;
    }

    @Override
    public Collection<Item> searchItem(String text) {
        return tempRepository.values().stream().filter(Item::getAvailable).filter(item -> (item.getName() != null && item.getName().toLowerCase().contains(text.toLowerCase())) || (item.getDescription() != null && item.getDescription().toLowerCase().contains(text.toLowerCase()))).toList();
    }
}
