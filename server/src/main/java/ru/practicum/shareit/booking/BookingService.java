package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;

import java.util.List;

public interface BookingService {
    BookingDto createBooking(Long userId, CreateBookingDto createBookingDto);

    BookingDto approveBooking(Long userId, Long bookingId, Boolean approved);

    BookingDto findBookingById(Long userId, Long bookingId);

    List<BookingDto> findAllUserBookingsWithState(Long userId, BookingServiceState state, Integer from, Integer size);

    List<BookingDto> findAllBookingsOfUserItemsWithState(Long userId, BookingServiceState state);
}
