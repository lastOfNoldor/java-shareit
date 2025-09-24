package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;

import java.util.Collection;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@RequestHeader(value = "X-Sharer-User-Id", required = false) String userId, @RequestBody @Valid CreateBookingDto createBookingDto) {
        return ResponseEntity.ok(bookingService.createBooking(userId, createBookingDto));
    }

    @PatchMapping("{bookingId}")
    public ResponseEntity<BookingDto> approveBooking(@RequestHeader(value = "X-Sharer-User-Id", required = false) String userId,@PathVariable Long bookingId, @RequestParam("approved") Boolean approved) {
        return ResponseEntity.ok(bookingService.approveBooking(userId, bookingId, approved));
    }

    @GetMapping("{bookingId}")
    public ResponseEntity<BookingDto> findBookingById(@RequestHeader(value = "X-Sharer-User-Id", required = false) String userId,@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.findBookingById(userId,bookingId));
    }


    @GetMapping
    public  ResponseEntity<Collection<BookingDto>> findAllUserBookingsWithState(@RequestHeader(value = "X-Sharer-User-Id", required = false) String userId,@RequestParam(value = "state", defaultValue = "ALL") BookingState state) {
        return ResponseEntity.ok(bookingService.findAllUserBookingsWithState(userId, state));
    }
    //TODO  "Какие вещи Я бронировал"

    @GetMapping("/owner")
    public ResponseEntity<Collection<BookingDto>> findAllBookingsOfUserItemsWithState(@RequestHeader(value = "X-Sharer-User-Id", required = false) String userId,@RequestParam(value = "state", defaultValue = "ALL") BookingState state) {
        return ResponseEntity.ok(bookingService.findAllBookingsOfUserItemsWithState(userId, state));
    }
    //TODO "Кто бронировал МОИ вещи"
}
