package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookerDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {
    public static Booking dtoToNewBooking(CreateBookingDto createBookingDto, Item item, User user) {
        return Booking.builder().startDate(createBookingDto.getStart()).endDate(createBookingDto.getEnd()).item(item).booker(user).status(BookingStatus.WAITING).build();
    }

    public static BookingDto bookingToDto(Booking saved) {
        BookerDto booker = BookerDto.builder().id(saved.getBooker().getId()).build();
        ItemShortDto item = ItemShortDto.builder().id(saved.getItem().getId()).name(saved.getItem().getName()).build();
        return BookingDto.builder().id(saved.getId()).start(saved.getStartDate()).end(saved.getEndDate()).booker(booker).item(item).status(saved.getStatus()).build();
    }

    public static BookingDto bookingToDto(Booking saved, Item item, User booker) {
        BookerDto dtoBooker = BookerDto.builder().id(booker.getId()).build();
        ItemShortDto dtoItem = ItemShortDto.builder().id(item.getId()).name(item.getName()).build();
        return BookingDto.builder().id(saved.getId()).start(saved.getStartDate()).end(saved.getEndDate()).booker(dtoBooker).item(dtoItem).status(saved.getStatus()).build();
    }

}
