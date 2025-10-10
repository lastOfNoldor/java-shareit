package ru.practicum.shareit.booking;

import java.util.Optional;

public enum BookingServiceState {
    ALL, CURRENT, PAST, FUTURE, WAITING, REJECTED;


    public static Optional<BookingServiceState> from(String stringState) {
        for (BookingServiceState state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
