package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.Instant;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl  implements BookingService{
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;


    @Override
    public BookingDto createBooking(String userIdStr, CreateBookingDto createBookingDto) {
        Long userId = Long.parseLong(userIdStr);
        Item bookingItem = itemRepository.findById(createBookingDto.getItemId()).orElseThrow(() -> new NotFoundException("Item Id указан неверно"));
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User Id указан неверно"));
        Booking createdBooking = BookingMapper.dtoToNewBooking(createBookingDto, bookingItem, user);
        return BookingMapper.bookingToDto(bookingRepository.save(createdBooking));
    }

    @Override
    public BookingDto approveBooking(String userIdStr, Long bookingId, Boolean approved) {
        if (approved == null) {
            throw new IllegalArgumentException("Параметр approved обязателен");
        }
        Long userId = Long.parseLong(userIdStr);
        Booking resultBooking = bookingRepository.findByIdAndItemOwnerId(bookingId, userId).orElseThrow(() -> new NotFoundException("Бронирование не найдено или у вас нет прав на изменение статуса"));
        if (resultBooking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Можно изменять только бронирования в статусе WAITING");
        }
        if (approved) {
            resultBooking.setStatus(BookingStatus.APPROVED);
        } else {
            resultBooking.setStatus(BookingStatus.REJECTED);
        }
        return BookingMapper.bookingToDto(bookingRepository.save(resultBooking));
    }

    @Transactional(readOnly = true)
    @Override
    public BookingDto findBookingById(String userIdStr, Long bookingId) {
        Long userId = Long.parseLong(userIdStr);
        Booking resultBooking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Бронирование не найдено!"));
        User booker = resultBooking.getBooker();
        User owner = resultBooking.getItem().getOwner();
        if (!Objects.equals(userId, booker.getId()) && !Objects.equals(userId,owner.getId())) {
            throw new NotFoundException("У вас нет доступа к данной брони");
        }
        return BookingMapper.bookingToDto(resultBooking);
    }

    @Override
    public Collection<BookingDto> findAllUserBookingsWithState(String userIdStr, BookingState state) {
        Long userId = Long.parseLong(userIdStr);
        List<Booking> usersBookings = bookingRepository.findAllByBookerId(userId);
        switch (state) {
            case PAST -> usersBookings.stream().filter(booking -> booking.getStart().isBefore(ChronoLocalDateTime.from(Instant.now()))).filter(booking -> booking.getEnd().isBefore(ChronoLocalDateTime.from(Instant.now()))).collect(Collectors.toList());
        }
        return List.of();
    }

    @Override
    public Collection<BookingDto> findAllBookingsOfUserItemsWithState(String userIdStr, BookingState state) {
        Long userId = Long.parseLong(userIdStr);
        List<Booking> allRelatedBookings = bookingRepository.findAllByItemOwnerId(userId);
        return List.of();
    }
}
