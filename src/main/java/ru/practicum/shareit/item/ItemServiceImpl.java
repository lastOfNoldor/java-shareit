package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CreateItemDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validator.CentralValidator;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final CentralValidator centralValidator;
    private final UserRepository userRepository;

    @Override
    public Collection<ItemDto> findAllUsersItems(Long userId) {
        log.info("Попытка получения списка всех вещей владельца.");
        return itemRepository.findAllByUserId(userId).stream().map(ItemMapper::itemToDto).collect(Collectors.toList());

    }

    @Override
    public ItemDto findItemById(Long id) {
        log.info("Попытка получения вещи по ID: {}", id);
        if (id == null) {
            throw new ValidationException("отсутствует Id вещи");
        }
        Optional<Item> itemById = itemRepository.findById(id);
        return ItemMapper.itemToDto(itemById.orElseThrow(() -> new NotFoundException("item с Id" + id + "не найден")));
    }

    @Override
    public ItemDto createItem(String userIdStr, CreateItemDto createItemDto) {
        log.info("Попытка создания вещи пользователя ID: {}", userIdStr);
        Long userId = Long.parseLong(userIdStr);
        Item createdItem = ItemMapper.dtoToNewItem(createItemDto);
        User userById = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User с Id" + userId + "не найден"));
        createdItem.setOwner(userById);
        Item resultItem = itemRepository.save(createdItem);
        return ItemMapper.itemToDto(resultItem);
    }


    @Override
    public ItemDto updateItem(Long userId, Long itemId, UpdateItemDto updateItemDto) {
        log.info("Попытка обновления вещи с ID: {}", itemId);
        if (itemId == null) {
            throw new ValidationException("отсутствует Id вещи");
        }
        if (userId == null) {
            throw new ValidationException("отсутствует Id пользователя");
        }
        Item existingItem = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        centralValidator.updatedItemAccess(existingItem, userId);
        Item updatedItem = ItemMapper.dtoUpdateExistingItem(existingItem, updateItemDto);
        Item resultItem = itemRepository.save(updatedItem);
        log.info("Успешное обновление вещи с ID: {}", itemId);
        return ItemMapper.itemToDto(resultItem);
    }

    @Override
    public Collection<ItemDto> searchItem(String text) {
        log.info("Запрос поиска...");
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.searchInNameOrDescription(text).stream().map(ItemMapper::itemToDto).collect(Collectors.toList());
    }

    @Override
    public void deleteItem(Long id) {
        if (id == null) {
            throw new ValidationException("отсутствует Id вещи");
        }
//        itemRepository.deleteItem(id);
        log.info("Пользователь с ID {} удален", id);

    }


}
