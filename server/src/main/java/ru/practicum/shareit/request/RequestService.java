package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.CreateRequestDto;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.ShortRequestResponse;

import java.util.List;

public interface RequestService {
    ShortRequestResponse createRequest(long userId, CreateRequestDto description);

    List<RequestDto> getUsersRequests(long userId);

    List<ShortRequestResponse> getAllRequests(long userId);

    RequestDto getRequestById(long reqId);

}
