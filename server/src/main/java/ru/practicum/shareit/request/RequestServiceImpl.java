package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.CreateRequestDto;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.ShortItemDtoForRequest;
import ru.practicum.shareit.request.dto.ShortRequestResponse;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {
    private final RequestRepository repository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Transactional
    @Override
    public ShortRequestResponse createRequest(long userId, CreateRequestDto description) {
        User author = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден при создании запроса"));
        ItemRequest itemRequest = RequestMapper.fromCreateRequestToEntity(description, author);
        ItemRequest saved = repository.save(itemRequest);
        return RequestMapper.fromEntityToShortResponse(saved, userId);
    }


    @Override
    public List<RequestDto> getUsersRequests(long userId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        List<ItemRequest> userRequests = repository.findAllByRequester_Id(userId, sort);
        if (userRequests.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> requestIds = userRequests.stream().map(ItemRequest::getId).collect(Collectors.toList());
        Map<Long, List<Item>> itemsByRequestId = itemRepository.findAllByRequestIdIn(requestIds).stream().collect(Collectors.groupingBy(item -> item.getRequestId(), LinkedHashMap::new, Collectors.toList()));
        return userRequests.stream().map(request -> {
            List<Item> items = itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList());
            List<ShortItemDtoForRequest> itemDtos = items.stream().map(item -> RequestMapper.itemToShortItemDtoForRequest(item, item.getOwner().getId())).collect(Collectors.toList());

            return RequestMapper.EntityToRequestDto(request, itemDtos);
        }).collect(Collectors.toList());
    }

    @Override
    public List<ShortRequestResponse> getAllRequests(long userId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        return repository.findAllByRequester_IdNot(userId, sort).stream().map(request -> RequestMapper.fromEntityToShortResponse(request, request.getRequester().getId())).collect(Collectors.toList());
    }

    @Override
    public RequestDto getRequestById(long reqId) {
        ItemRequest result = repository.findById(reqId).orElseThrow(() -> new NotFoundException("Запрос не найден."));
        List<Item> itemsByRequestId = itemRepository.findAllByRequestId(result.getId());
        List<ShortItemDtoForRequest> itemDtos = itemsByRequestId.stream().map(item -> RequestMapper.itemToShortItemDtoForRequest(item, item.getOwner().getId())).collect(Collectors.toList());
        return RequestMapper.EntityToRequestDto(result, itemDtos);

    }
}
