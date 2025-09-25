package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;

import java.util.Collection;
import java.util.List;

public interface BookingService {
    BookingDto createBooking(String userId, CreateBookingDto createBookingDto);

    BookingDto approveBooking(String userId,Long bookingId, Boolean approved);

    BookingDto findBookingById(String userId, Long bookingId);

    List<BookingDto> findAllUserBookingsWithState(String userId, BookingServiceState state);

    List<BookingDto> findAllBookingsOfUserItemsWithState(String userId, BookingServiceState state);
}
