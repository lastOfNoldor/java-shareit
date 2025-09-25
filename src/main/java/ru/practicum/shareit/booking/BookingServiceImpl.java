package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
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
import ru.practicum.shareit.validator.CentralValidator;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final CentralValidator validator;


    @Override
    public BookingDto createBooking(String userIdStr, CreateBookingDto createBookingDto) {
        Long userId = validator.userIdFormatValidation(userIdStr);
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
        Long userId = validator.userIdFormatValidation(userIdStr);
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
        Long userId = validator.userIdFormatValidation(userIdStr);
        Booking resultBooking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Бронирование не найдено!"));
        User booker = resultBooking.getBooker();
        User owner = resultBooking.getItem().getOwner();
        if (!Objects.equals(userId, booker.getId()) && !Objects.equals(userId,owner.getId())) {
            throw new NotFoundException("У вас нет доступа к данной брони");
        }
        return BookingMapper.bookingToDto(resultBooking);
    }

    @Override
    public List<BookingDto> findAllUserBookingsWithState(String userIdStr, BookingServiceState state) {
        Long bookerId = validator.userIdFormatValidation(userIdStr);
        LocalDateTime now = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> filteredBookings;
        switch (state) {
            case PAST -> filteredBookings = bookingRepository.findAllByBooker_IdAndEndBefore(bookerId, now, sort);
            case CURRENT -> filteredBookings = bookingRepository.findAllByBooker_IdAndStartBeforeAndEndAfter(bookerId,now,now,sort);
            case FUTURE -> filteredBookings = bookingRepository.findAllByBooker_IdAndStartAfter(bookerId,now,sort);
            case WAITING -> filteredBookings = bookingRepository.findAllByBooker_IdAndStatus(bookerId,BookingStatus.WAITING,sort);
            case REJECTED -> filteredBookings = bookingRepository.findAllByBooker_IdAndStatus(bookerId,BookingStatus.REJECTED,sort);
            case ALL -> filteredBookings = bookingRepository.findAllByBooker_Id(bookerId,sort);
            default ->  throw new IllegalArgumentException("указан не существующий вариант параметра state");
        }
        return filteredBookings.stream().map(BookingMapper::bookingToDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> findAllBookingsOfUserItemsWithState(String userIdStr, BookingServiceState state) {
        Long ownerId = Long.parseLong(userIdStr);
        LocalDateTime now = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> relatedBookings;
        switch (state) {
            case PAST -> relatedBookings = bookingRepository.findAllByItem_Owner_IdAndEndBefore(ownerId, now, sort);
            case CURRENT -> relatedBookings = bookingRepository.findAllByItem_Owner_IdAndStartBeforeAndEndAfter(ownerId,now,now,sort);
            case FUTURE -> relatedBookings = bookingRepository.findAllByItem_Owner_IdAndStartAfter(ownerId,now,sort);
            case WAITING -> relatedBookings = bookingRepository.findAllByItem_Owner_IdAndStatus(ownerId,BookingStatus.WAITING,sort);
            case REJECTED -> relatedBookings = bookingRepository.findAllByItem_Owner_IdAndStatus(ownerId,BookingStatus.REJECTED,sort);
            case ALL -> relatedBookings = bookingRepository.findAllByItem_Owner_Id(ownerId, sort);
            default ->  throw new IllegalArgumentException("указан не существующий вариант параметра state");
        }
        return relatedBookings.stream().map(BookingMapper::bookingToDto).collect(Collectors.toList());
    }


}
