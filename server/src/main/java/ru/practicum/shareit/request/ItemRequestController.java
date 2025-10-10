package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.CreateRequestDto;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.ShortRequestResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final RequestService service;

    @PostMapping
    public ResponseEntity<ShortRequestResponse> createRequest(@RequestHeader(value = "X-Sharer-User-Id") long userId, @RequestBody CreateRequestDto description) {
        return ResponseEntity.ok(service.createRequest(userId, description));
    }


    @GetMapping
    public ResponseEntity<List<RequestDto>> getUsersRequests(@RequestHeader(value = "X-Sharer-User-Id") long userId) {
        return ResponseEntity.ok(service.getUsersRequests(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ShortRequestResponse>> getAllRequests(@RequestHeader(value = "X-Sharer-User-Id") long userId) {
        return ResponseEntity.ok(service.getAllRequests(userId));
    }

    @GetMapping("/{reqId}")
    public ResponseEntity<RequestDto> getRequestById(@PathVariable long reqId) {
        return ResponseEntity.ok(service.getRequestById(reqId));
    }

}
