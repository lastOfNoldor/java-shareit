package ru.practicum.shareit.request;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.CreateRequestDto;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.ShortItemDtoForRequest;
import ru.practicum.shareit.request.dto.ShortRequestResponse;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class RequestMapper {

    public static ItemRequest fromCreateRequestToEntity(CreateRequestDto dto, User user) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setDescription(dto.getDescription());
        itemRequest.setRequester(user);
        return itemRequest;
    }

    public static ShortRequestResponse fromEntityToShortResponse(ItemRequest request, long userId) {
        return ShortRequestResponse.builder().id(request.getId()).description(request.getDescription()).requesterId(userId).created(request.getCreated()).build();
    }

    public static RequestDto EntityToRequestDto(ItemRequest request, List<ShortItemDtoForRequest> info) {
        return RequestDto.builder().id(request.getId()).description(request.getDescription()).created(request.getCreated()).items(info).build();
    }

    public static ShortItemDtoForRequest itemToShortItemDtoForRequest(Item item, long ownerId) {
        return ShortItemDtoForRequest.builder().id(item.getId()).name(item.getName()).ownerId(ownerId).build();
    }
}
