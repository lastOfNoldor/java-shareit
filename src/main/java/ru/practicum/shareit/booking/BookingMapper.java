package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {
    public static Booking dtoToNewBooking(CreateBookingDto createBookingDto, Item item, User user) {
        return Booking.builder().start(createBookingDto.getStart()).end(createBookingDto.getEnd()).item(item).booker(user).status(BookingStatus.WAITING).build();
    }

    public static BookingDto bookingToDto(Booking saved) {
        return BookingDto.builder()
                .id(saved.getId())
                .start(saved.getStart())
                .end(saved.getEnd())
                .booker(User.builder()
                        .id(saved.getBooker().getId())
                        .build())
                .item(Item.builder()
                        .id(saved.getItem().getId())
                        .name(saved.getItem().getName())
                        .build())
                .status(saved.getStatus()).build();
    }

    public static BookingDto bookingToDto(Booking saved, Item item, User booker) {
        return BookingDto.builder()
                .id(saved.getId())
                .start(saved.getStart())
                .end(saved.getEnd())
                .booker(booker)
                .item(item)
                .status(saved.getStatus()).build();
    }

}
