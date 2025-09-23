package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;

import java.util.Collection;

public interface BookingService {
    BookingDto createBooking(String userId, CreateBookingDto createBookingDto);

    BookingDto approveBooking(String userId,Long bookingId, Boolean approved);

    BookingDto findBookingById(String userId, Long bookingId);

    Collection<BookingDto> findAllUserBookingsWithState(String userId, BookingState state);

    Collection<BookingDto> findAllBookingsOfUserItemsWithState(String userId, BookingState state);
}
