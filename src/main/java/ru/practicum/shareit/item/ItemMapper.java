package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CreateItemDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Item;

public class ItemMapper {
    public static ItemDto itemToDto(Item item) {
        return ItemDto.builder().id(item.getId()).name(item.getName()).description(item.getDescription()).available(item.getAvailable()).owner(item.getOwner()).build();
    }

    public static Item DtoUpdateExistingItem(Item existingItem, UpdateItemDto updateItemDto) {
        if (updateItemDto.getName() != null) {
            existingItem.setName(updateItemDto.getName());
        }
        if (updateItemDto.getDescription() != null) {
            existingItem.setDescription(updateItemDto.getDescription());
        }
        if (updateItemDto.getAvailable() != null) {
            existingItem.setAvailable(updateItemDto.getAvailable());
        }
        return existingItem;
    }

    public static Item DtoToNewItem(CreateItemDto createItemDto) {
        return Item.builder().name(createItemDto.getName()).description(createItemDto.getDescription()).available(createItemDto.getAvailable()).build();
    }
}
