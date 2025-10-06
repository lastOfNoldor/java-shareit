package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ItemMapper {
    public static ItemDto itemToDto(Item item) {
        return ItemDto.builder().id(item.getId()).name(item.getName()).description(item.getDescription()).available(item.getAvailable()).ownerId(item.getOwner().getId()).build();
    }

    public static ItemDto itemToDto(Item item, User user) {
        return ItemDto.builder().id(item.getId()).name(item.getName()).description(item.getDescription()).available(item.getAvailable()).ownerId(item.getOwner().getId()).build();
    }

    public static Item dtoUpdateExistingItem(Item existingItem, UpdateItemDto updateItemDto) {
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

    public static Item dtoToNewItem(CreateItemDto createItemDto) {
        return Item.builder().name(createItemDto.getName()).description(createItemDto.getDescription()).available(createItemDto.getAvailable()).requestId(createItemDto.getRequestId()).build();
    }

    public static ItemDtoWithDatesAndComments itemToDtoWithDatesAndComments(Item item, LocalDateTime start, LocalDateTime end, List<Comment> comments) {
        List<CommentDto> dtos = comments.stream().map(comment -> CommentMapper.commentToDto(comment, comment.getAuthor().getName())).collect(Collectors.toList());
        return ItemDtoWithDatesAndComments.builder().id(item.getId()).name(item.getName()).description(item.getDescription()).available(item.getAvailable()).start(start).end(end).ownerId(item.getOwner().getId()).comments(dtos).requestId(item.getRequestId()).build();
    }

    public static UpdateItemDto itemToUpdateDto(Item resultItem) {
        return UpdateItemDto.builder().id(resultItem.getId()).name(resultItem.getName()).description(resultItem.getDescription()).available(resultItem.getAvailable()).requestId(resultItem.getRequestId()).build();
    }

    public static ItemDtoWithMultipleBookings itemToDtoWithMultipleBookings(Item item, Booking lastBooking, Booking nextBooking, List<Comment> comments) {
        List<CommentDto> dtos = comments.stream().map(comment -> CommentMapper.commentToDto(comment, comment.getAuthor().getName())).collect(Collectors.toList());
        ShortBookingDto lastBookingDto = null;
        if (lastBooking != null) {
            lastBookingDto = ShortBookingDto.builder().start(lastBooking.getStartDate()).end(lastBooking.getEndDate()).build();
        }

        ShortBookingDto nextBookingDto = null;
        if (nextBooking != null) {
            nextBookingDto = ShortBookingDto.builder().start(nextBooking.getStartDate()).end(nextBooking.getEndDate()).build();
        }

        return ItemDtoWithMultipleBookings.builder().id(item.getId()).name(item.getName()).description(item.getDescription()).available(item.getAvailable()).lastBooking(lastBookingDto).nextBooking(nextBookingDto).comments(dtos).requestId(item.getRequestId()).build();
    }
}
