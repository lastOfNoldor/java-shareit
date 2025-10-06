package ru.practicum.shareit.item;


import ru.practicum.shareit.item.dto.*;

import java.util.Collection;

public interface ItemService {


    Collection<ItemDto> findAllUsersItems(Long userId);

    ItemDtoWithMultipleBookings findItemById(Long userId, Long id);

    ItemDto createItem(String userId, CreateItemDto createItemDto);

    UpdateItemDto updateItem(Long userId, Long itemId, UpdateItemDto updateItemDto);

    void deleteItem(Long id);

    Collection<UpdateItemDto> searchItem(String text);

    CommentDto createComment(Long userId, Long itemId, CreateCommentDto createCommentDto);
}




