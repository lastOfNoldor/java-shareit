package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {
    public static Booking dtoToNewBooking(CreateBookingDto createBookingDto, Item item, User user) {
        return Booking.builder().start(createBookingDto.getStart()).end(createBookingDto.getEnd()).item(item).booker(user).status(BookingStatus.WAITING).build();
    }

    public static BookingDto bookingToDto(Booking saved) {


    }
}
