package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.CreateBookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.List;

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

    @Override
    public BookingDto findBookingById(String userIdStr, Long bookingId) {
        Long userId = Long.parseLong(userIdStr);
        return null;
    }

    @Override
    public Collection<BookingDto> findAllUserBookingsWithState(String userIdStr, BookingState state) {
        Long userId = Long.parseLong(userIdStr);
        return List.of();
    }

    @Override
    public Collection<BookingDto> findAllBookingsOfUserItemsWithState(String userIdStr, BookingState state) {
        Long userId = Long.parseLong(userIdStr);
        return List.of();
    }
}
