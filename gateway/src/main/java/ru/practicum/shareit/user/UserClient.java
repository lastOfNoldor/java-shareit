package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;

import java.util.Map;

@Service
public class UserClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public UserClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX)).requestFactory(() -> new HttpComponentsClientHttpRequestFactory()).build());
    }

    public ResponseEntity<Object> bookItem(long userId, BookItemRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> getBookings(long userId, BookingState state, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of("state", state.name(), "from", from, "size", size);
        return get("?state={state}&from={from}&size={size}", userId, parameters);
    }


    public ResponseEntity<Object> getBooking(long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> updateBooking(long userId, Long bookingId, Boolean approved) {
        Map<String, Object> parameters = Map.of("approved", approved);
        return patch("/" + bookingId, userId, parameters, null);
    }

    public ResponseEntity<Object> getBookingsWithState(String path, long userId, BookingState state) {
        Map<String, Object> parameters = Map.of("state", state.name());
        return get(path + "?state={state}", userId, parameters);
    } //TODO


    public ResponseEntity<Object> getUsers() {
        return get("");
    }

    public ResponseEntity<Object> getUser(Long id) {
        return get("/" + id);
    }


    public ResponseEntity<Object> createUser(@Valid CreateUserDto createUserDto) {
        return post("", createUserDto);
    }

    public ResponseEntity<Object> updateUser(Long id, @Valid UpdateUserDto user) {
        return patch("/" + id, user);
    }

    public ResponseEntity<Object> deleteUser(Long id) {
        return delete("/" + id);
    }
}
