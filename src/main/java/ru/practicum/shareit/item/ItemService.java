package ru.practicum.shareit.item;


import ru.practicum.shareit.item.dto.CreateItemDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import java.util.Collection;

public interface ItemService {


    Collection<ItemDto> findAllUsersItems(Long userId);

    ItemDto findItemById(Long id);

    ItemDto createItem(String userId, CreateItemDto createItemDto);

    ItemDto updateItem(Long userId, Long itemId, UpdateItemDto updateItemDto);

    void deleteItem(Long id);

    Collection<ItemDto> searchItem(String text);
}




