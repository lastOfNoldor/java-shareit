package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validator.CentralValidator;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CentralValidator validator;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    @Override
    public Collection<ItemDto> findAllUsersItems(Long userId) {
        log.info("Попытка получения списка всех вещей владельца с бронированиями и комментариями.");
        List<Item> allByOwnerId = itemRepository.findAllByOwner_Id(userId);
        List<ItemDto> result = new ArrayList<>();
        Map<Long, Booking> bookingsOfUsersItems = bookingRepository.findAllByItem_Owner_Id(userId).stream().collect(Collectors.toMap(booking -> booking.getItem().getId(), booking -> booking));
        List<Long> itemIds = bookingsOfUsersItems.keySet().stream().collect(Collectors.toList());
        Map<Long, List<Comment>> itemsComments = commentRepository.findAllByItemIdsIn(itemIds).stream().collect(Collectors.groupingBy(comment -> comment.getItem().getId()));
        for (Item item : allByOwnerId) {
            Booking currentItemBooking = bookingsOfUsersItems.get(item.getId());
            if (currentItemBooking != null) {
                List<Comment> currentItemComments = itemsComments.get(item.getId());
                result.add(ItemMapper.itemToDtoWithDatesAndComments(item, currentItemBooking.getStartDate(), currentItemBooking.getEndDate(), currentItemComments));
            } else {
                result.add(ItemMapper.itemToDto(item));
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    @Override
    public ItemDtoWithMultipleBookings findItemById(Long userId, Long id) {
        log.info("Попытка получения вещи по ID: {}", id);
        if (id == null) {
            throw new ValidationException("отсутствует Id вещи");
        }
        Item itemById = itemRepository.findWithOwnerById(id).orElseThrow(() -> new NotFoundException("item с Id" + id + "не найден"));
        LocalDateTime now = LocalDateTime.now();
        Optional<Booking> lastBooking = bookingRepository.findLastBooking(id, now);
        Optional<Booking> nextBooking = bookingRepository.findNextBooking(id, now);
        List<Comment> comments = commentRepository.findAllByItem_Id(id);
        log.info("Найдено комментариев для item_id {}: {}", id, comments.size());
        if (itemById.getOwner().getId().equals(userId)) {
            return ItemMapper.itemToDtoWithMultipleBookings(itemById, lastBooking.orElse(null), nextBooking.orElse(null), comments);
        } else {
            return ItemMapper.itemToDtoWithMultipleBookings(itemById, null, null, comments);
        }
    }

    @Transactional
    @Override
    public ItemDto createItem(String userIdStr, CreateItemDto createItemDto) {
        log.info("Попытка создания вещи пользователя ID: {}", userIdStr);
        Long userId = validator.userIdFormatValidation(userIdStr);
        Item createdItem = ItemMapper.dtoToNewItem(createItemDto);
        User userById = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User с Id" + userId + "не найден"));
        createdItem.setOwner(userById);
        Item resultItem = itemRepository.save(createdItem);
        return ItemMapper.itemToDto(resultItem, userById);
    }


    @Transactional
    @Override
    public UpdateItemDto updateItem(Long userId, Long itemId, UpdateItemDto updateItemDto) {
        log.info("Попытка обновления вещи с ID: {}", itemId);
        if (itemId == null) {
            throw new ValidationException("отсутствует Id вещи");
        }
        if (userId == null) {
            throw new ValidationException("отсутствует Id пользователя");
        }
        Item existingItem = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        validator.updatedItemAccess(existingItem, userId);
        Item updatedItem = ItemMapper.dtoUpdateExistingItem(existingItem, updateItemDto);
        Item resultItem = itemRepository.save(updatedItem);
        log.info("Успешное обновление вещи с ID: {}", itemId);
        return ItemMapper.itemToUpdateDto(resultItem);
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<UpdateItemDto> searchItem(String text) {
        log.info("Запрос поиска...");
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.searchInNameOrDescription(text).stream().filter(Item::getAvailable).map(ItemMapper::itemToUpdateDto).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CommentDto createComment(Long userId, Long itemId, CreateCommentDto createCommentDto) {
        log.info("Попытка создания комментария для вещи с ID: {}", itemId);
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        Booking commentedBooking = bookingRepository.findByBooker_IdAndItem_IdAndEndDateBefore(userId, itemId, LocalDateTime.now()).orElseThrow(() -> new ValidationException("Оставлять отзывы можно только после окончания аренды."));
        String authorName = commentedBooking.getBooker().getName();
        Comment comment = CommentMapper.createDtoToComment(createCommentDto);
        comment.setItem(item);
        comment.setAuthor(commentedBooking.getBooker());
        comment.setCreated(LocalDateTime.now());
        Comment saved = commentRepository.save(comment);
        return CommentMapper.commentToDto(saved, authorName);
    }

    @Transactional
    @Override
    public void deleteItem(Long id) {
        if (id == null) {
            throw new ValidationException("отсутствует Id вещи");
        }
        Optional<Item> byId = itemRepository.findById(id);
        itemRepository.delete(byId.orElseThrow(() -> new NotFoundException("Вещь не найдена")));
        log.info("Вещь с ID {} удалена", id);
    }


}
