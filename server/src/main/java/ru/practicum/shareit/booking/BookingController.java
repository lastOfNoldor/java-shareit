package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@RequestHeader(value = "X-Sharer-User-Id", required = false) String userId, @RequestBody CreateBookingDto createBookingDto) {
        return ResponseEntity.ok(bookingService.createBooking(userId, createBookingDto));
    }

    @PatchMapping("{bookingId}")
    public ResponseEntity<BookingDto> approveBooking(@RequestHeader(value = "X-Sharer-User-Id", required = false) String userId, @PathVariable Long bookingId, @RequestParam("approved") Boolean approved) {
        return ResponseEntity.ok(bookingService.approveBooking(userId, bookingId, approved));
    }

    @GetMapping("{bookingId}")
    public ResponseEntity<BookingDto> findBookingById(@RequestHeader(value = "X-Sharer-User-Id", required = false) String userId, @PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.findBookingById(userId, bookingId));
    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> findAllUserBookingsWithState(@RequestHeader(value = "X-Sharer-User-Id", required = false) String userId, @RequestParam(name = "state", defaultValue = "all") String stateParam, @RequestParam(name = "from", defaultValue = "0") Integer from, @RequestParam(name = "size", defaultValue = "10") Integer size) {
        BookingServiceState state = BookingServiceState.from(stateParam).orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        return ResponseEntity.ok(bookingService.findAllUserBookingsWithState(userId, state, from, size));
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingDto>> findAllBookingsOfUserItemsWithState(@RequestHeader(value = "X-Sharer-User-Id", required = false) String userId, @RequestParam(value = "state", defaultValue = "ALL") BookingServiceState state) {
        return ResponseEntity.ok(bookingService.findAllBookingsOfUserItemsWithState(userId, state));
    }
}
