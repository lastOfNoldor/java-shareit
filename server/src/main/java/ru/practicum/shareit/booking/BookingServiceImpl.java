package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public BookingDto createBooking(Long userId, CreateBookingDto createBookingDto) {
        log.info("Создание бронирования для пользователя ID: {}, Item ID: {}", userId, createBookingDto.getItemId());
        Item bookingItem = itemRepository.findById(createBookingDto.getItemId()).orElseThrow(() -> {
            log.error("Item с ID {} не найден", createBookingDto.getItemId());
            return new NotFoundException("Item Id указан неверно");
        });

        User booker = userRepository.findById(userId).orElseThrow(() -> {
            log.error("User с ID {} не найден", userId);
            return new NotFoundException("User Id указан неверно");
        });

        if (!bookingItem.getAvailable()) {
            log.warn("Попытка забронировать недоступную вещь ID: {}", bookingItem.getId());
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        if (Objects.equals(bookingItem.getOwner().getId(), userId)) {
            log.warn("Попытка забронировать собственную вещь. User ID: {}, Item ID: {}", userId, bookingItem.getId());
            throw new NotFoundException("Нельзя забронировать собственную вещь");
        }

        Booking createdBooking = BookingMapper.dtoToNewBooking(createBookingDto, bookingItem, booker);
        Booking saved = bookingRepository.save(createdBooking);

        log.info("Бронирование создано успешно. ID: {}, Status: {}", saved.getId(), saved.getStatus());
        return BookingMapper.bookingToDto(saved, bookingItem, booker);
    }

    @Transactional
    @Override
    public BookingDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        log.info("Изменение статуса бронирования ID: {} на approved: {} пользователем ID: {}", bookingId, approved, userId);

        if (approved == null) {
            log.warn("Параметр approved не указан для бронирования ID: {}", bookingId);
            throw new IllegalArgumentException("Параметр approved обязателен");
        }
        Booking resultBooking = bookingRepository.findByIdAndItemOwnerId(bookingId, userId).orElseThrow(() -> {
            log.error("Бронирование ID: {} не найдено или пользователь ID: {} не является владельцем", bookingId, userId);
            return new ValidationException("Бронирование не найдено или у вас нет прав на изменение статуса");
        });

        if (resultBooking.getStatus() != BookingStatus.WAITING) {
            log.warn("Попытка изменить статус бронирования ID: {} с текущим статусом: {}", bookingId, resultBooking.getStatus());
            throw new ValidationException("Можно изменять только бронирования в статусе WAITING");
        }

        BookingStatus newStatus = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        resultBooking.setStatus(newStatus);

        log.info("Статус бронирования ID: {} изменен на: {}", bookingId, newStatus);

        return BookingMapper.bookingToDto(bookingRepository.findWithBookerAndOwnerById(bookingId).orElseThrow(() -> {
            log.error("Не удалось загрузить бронирование ID: {} после обновления", bookingId);
            return new DataAccessResourceFailureException("Этой ошибки произойти не должно.");
        }));
    }

    @Transactional(readOnly = true)
    @Override
    public BookingDto findBookingById(Long userId, Long bookingId) {
        log.debug("Поиск бронирования ID: {} пользователем ID: {}", bookingId, userId);
        Booking resultBooking = bookingRepository.findWithBookerAndOwnerById(bookingId).orElseThrow(() -> {
            log.warn("Бронирование ID: {} не найдено", bookingId);
            return new NotFoundException("Бронирование не найдено!");
        });

        User booker = resultBooking.getBooker();
        User owner = resultBooking.getItem().getOwner();

        if (!Objects.equals(userId, booker.getId()) && !Objects.equals(userId, owner.getId())) {
            log.warn("Попытка доступа к бронированию ID: {} пользователем ID: {} без прав", bookingId, userId);
            throw new NotFoundException("У вас нет доступа к данной брони");
        }

        log.debug("Бронирование ID: {} успешно найдено для пользователя ID: {}", bookingId, userId);
        return BookingMapper.bookingToDto(resultBooking);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDto> findAllUserBookingsWithState(Long bookerId, BookingServiceState state, Integer from, Integer size) {
        log.info("Поиск всех бронирований пользователя ID: {} с фильтром: {}", bookerId, state);
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "startDate"));
        Page<Booking> filteredBookings;
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }
        switch (state) {
            case PAST -> {
                log.debug("Поиск завершенных бронирований для пользователя ID: {}", bookerId);
                filteredBookings = bookingRepository.findAllByBooker_IdAndEndDateBefore(bookerId, now, pageable);
            }
            case CURRENT -> {
                log.debug("Поиск текущих бронирований для пользователя ID: {}", bookerId);
                filteredBookings = bookingRepository.findAllByBooker_IdAndStartDateBeforeAndEndDateAfter(bookerId, now, now, pageable);
            }
            case FUTURE -> {
                log.debug("Поиск будущих бронирований для пользователя ID: {}", bookerId);
                filteredBookings = bookingRepository.findAllByBooker_IdAndStartDateAfter(bookerId, now, pageable);
            }
            case WAITING -> {
                log.debug("Поиск ожидающих бронирований для пользователя ID: {}", bookerId);
                filteredBookings = bookingRepository.findAllByBooker_IdAndStatus(bookerId, BookingStatus.WAITING, pageable);
            }
            case REJECTED -> {
                log.debug("Поиск отклоненных бронирований для пользователя ID: {}", bookerId);
                filteredBookings = bookingRepository.findAllByBooker_IdAndStatus(bookerId, BookingStatus.REJECTED, pageable);
            }
            case ALL -> {
                log.debug("Поиск всех бронирований для пользователя ID: {}", bookerId);
                filteredBookings = bookingRepository.findAllByBooker_Id(bookerId, pageable);
            }
            default -> {
                log.error("Указан неизвестный статус фильтра: {}", state);
                throw new IllegalArgumentException("указан не существующий вариант параметра state");
            }
        }

        log.info("Найдено {} бронирований для пользователя ID: {} с фильтром: {}", filteredBookings.getTotalElements(), bookerId, state);
        return filteredBookings.map(BookingMapper::bookingToDto).getContent();
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDto> findAllBookingsOfUserItemsWithState(Long ownerId, BookingServiceState state) {
        log.info("Поиск бронирований вещей пользователя ID: {} с фильтром: {}", ownerId, state);
        LocalDateTime now = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Direction.DESC, "startDate");
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null");
        }
        List<Booking> relatedBookings;
        switch (state) {
            case PAST -> {
                log.debug("Поиск завершенных бронирований вещей владельца ID: {}", ownerId);
                relatedBookings = bookingRepository.findAllByItem_Owner_IdAndEndDateBefore(ownerId, now, sort);
            }
            case CURRENT -> {
                log.debug("Поиск текущих бронирований вещей владельца ID: {}", ownerId);
                relatedBookings = bookingRepository.findAllByItem_Owner_IdAndStartDateBeforeAndEndDateAfter(ownerId, now, now, sort);
            }
            case FUTURE -> {
                log.debug("Поиск будущих бронирований вещей владельца ID: {}", ownerId);
                relatedBookings = bookingRepository.findAllByItem_Owner_IdAndStartDateAfter(ownerId, now, sort);
            }
            case WAITING -> {
                log.debug("Поиск ожидающих бронирований вещей владельца ID: {}", ownerId);
                relatedBookings = bookingRepository.findAllByItem_Owner_IdAndStatus(ownerId, BookingStatus.WAITING, sort);
            }
            case REJECTED -> {
                log.debug("Поиск отклоненных бронирований вещей владельца ID: {}", ownerId);
                relatedBookings = bookingRepository.findAllByItem_Owner_IdAndStatus(ownerId, BookingStatus.REJECTED, sort);
            }
            case ALL -> {
                log.debug("Поиск всех бронирований вещей владельца ID: {}", ownerId);
                relatedBookings = bookingRepository.findAllByItem_Owner_Id(ownerId, sort);
            }
            default -> {
                log.error("Указаны неизвестный статус фильтра: {}", state);
                throw new IllegalArgumentException("указан не существующий вариант параметра state");
            }
        }
        if (relatedBookings.isEmpty()) {
            throw new NotFoundException("Вы не являетесь владельцем ни однйо вещи");
        }
        log.info("Найдено {} бронирований вещей владельца ID: {} с фильтром: {}", relatedBookings.size(), ownerId, state);
        return relatedBookings.stream().map(BookingMapper::bookingToDto).collect(Collectors.toList());
    }
}
