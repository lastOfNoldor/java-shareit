package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.CreateRequestDto;

/**
 * TODO Sprint add-item-requests.
 */

@Controller
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(value = "X-Sharer-User-Id") long userId, @RequestBody @Valid CreateRequestDto description) {
        return requestClient.createRequest(userId, description);
    }

    @GetMapping
    public ResponseEntity<Object> getUsersRequests(@RequestHeader(value = "X-Sharer-User-Id") long userId) {
        return requestClient.getUsersRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader(value = "X-Sharer-User-Id") long userId) {
        return requestClient.getAllRequests("/all", userId);
    }

    @GetMapping("/{reqId}")
    public ResponseEntity<Object> getRequestById(@PathVariable long reqId) {
        return requestClient.getRequestById(reqId);
    }

}
