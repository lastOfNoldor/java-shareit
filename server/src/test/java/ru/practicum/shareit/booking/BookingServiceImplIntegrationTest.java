package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User booker;
    private User otherUser;
    private Item availableItem;
    private Item unavailableItem;

    @BeforeEach
    void setUp() {

        owner = userRepository.save(User.builder().name("Owner").email("owner@email.com").build());

        booker = userRepository.save(User.builder().name("Booker").email("booker@email.com").build());

        otherUser = userRepository.save(User.builder().name("Other User").email("other@email.com").build());

        availableItem = itemRepository.save(Item.builder().name("Available Item").description("Available for booking").available(true).owner(owner).build());

        unavailableItem = itemRepository.save(Item.builder().name("Unavailable Item").description("Not available for booking").available(false).owner(owner).build());
    }

    @Test
    void createBooking_WithNonExistentItem_ShouldThrowNotFoundException() {
        CreateBookingDto createBookingDto = new CreateBookingDto();
        createBookingDto.setItemId(999L);
        createBookingDto.setStart(LocalDateTime.now().plusDays(1));
        createBookingDto.setEnd(LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.createBooking(booker.getId(), createBookingDto)).isInstanceOf(NotFoundException.class).hasMessageContaining("Item Id указан неверно");
    }

    @Test
    void createBooking_WithNonExistentUser_ShouldThrowNotFoundException() {
        CreateBookingDto createBookingDto = new CreateBookingDto();
        createBookingDto.setItemId(availableItem.getId());
        createBookingDto.setStart(LocalDateTime.now().plusDays(1));
        createBookingDto.setEnd(LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.createBooking(999L, createBookingDto)).isInstanceOf(NotFoundException.class).hasMessageContaining("User Id указан неверно");
    }

    @Test
    void createBooking_WithUnavailableItem_ShouldThrowValidationException() {
        CreateBookingDto createBookingDto = new CreateBookingDto();
        createBookingDto.setItemId(unavailableItem.getId());
        createBookingDto.setStart(LocalDateTime.now().plusDays(1));
        createBookingDto.setEnd(LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.createBooking(booker.getId(), createBookingDto)).isInstanceOf(ValidationException.class).hasMessageContaining("Вещь недоступна для бронирования");
    }

    @Test
    void createBooking_WithOwnItem_ShouldThrowNotFoundException() {
        CreateBookingDto createBookingDto = new CreateBookingDto();
        createBookingDto.setItemId(availableItem.getId());
        createBookingDto.setStart(LocalDateTime.now().plusDays(1));
        createBookingDto.setEnd(LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.createBooking(owner.getId(), createBookingDto)).isInstanceOf(NotFoundException.class).hasMessageContaining("Нельзя забронировать собственную вещь");
    }

    @Test
    void createBooking_WithValidData_ShouldCreateBookingWithWaitingStatus() {
        CreateBookingDto createBookingDto = new CreateBookingDto();
        createBookingDto.setItemId(availableItem.getId());
        createBookingDto.setStart(LocalDateTime.now().plusDays(1));
        createBookingDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto result = bookingService.createBooking(booker.getId(), createBookingDto);

        assertThat(result, allOf(hasProperty("id", notNullValue()), hasProperty("status", is(BookingStatus.WAITING)), hasProperty("booker", hasProperty("id", is(booker.getId()))), hasProperty("item", hasProperty("id", is(availableItem.getId())))));
    }

    @Test
    void approveBooking_WithNullApproved_ShouldThrowIllegalArgumentException() {
        Booking booking = bookingRepository.save(Booking.builder().startDate(LocalDateTime.now().plusDays(1)).endDate(LocalDateTime.now().plusDays(2)).item(availableItem).booker(booker).status(BookingStatus.WAITING).build());

        assertThatThrownBy(() -> bookingService.approveBooking(owner.getId(), booking.getId(), null)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Параметр approved обязателен");
    }

    @Test
    void approveBooking_WithNonOwner_ShouldThrowValidationException() {
        Booking waitingBooking = bookingRepository.save(Booking.builder().startDate(LocalDateTime.now().plusDays(1)).endDate(LocalDateTime.now().plusDays(2)).item(availableItem).booker(booker).status(BookingStatus.WAITING).build());

        assertThatThrownBy(() -> bookingService.approveBooking(otherUser.getId(), waitingBooking.getId(), true)).isInstanceOf(ValidationException.class).hasMessageContaining("нет прав на изменение статуса");
    }

    @Test
    void approveBooking_WithNonWaitingStatus_ShouldThrowValidationException() {
        Booking approvedBooking = bookingRepository.save(Booking.builder().startDate(LocalDateTime.now().plusDays(1)).endDate(LocalDateTime.now().plusDays(2)).item(availableItem).booker(booker).status(BookingStatus.APPROVED).build());

        assertThatThrownBy(() -> bookingService.approveBooking(owner.getId(), approvedBooking.getId(), true)).isInstanceOf(ValidationException.class).hasMessageContaining("Можно изменять только бронирования в статусе WAITING");
    }

    @Test
    void approveBooking_RejectBooking_ShouldSetRejectedStatus() {
        Booking waitingBooking = bookingRepository.save(Booking.builder().startDate(LocalDateTime.now().plusDays(1)).endDate(LocalDateTime.now().plusDays(2)).item(availableItem).booker(booker).status(BookingStatus.WAITING).build());

        BookingDto result = bookingService.approveBooking(owner.getId(), waitingBooking.getId(), false);

        assertThat(result, allOf(hasProperty("id", is(waitingBooking.getId())), hasProperty("status", is(BookingStatus.REJECTED))));
    }

    @Test
    void findBookingById_WithNonExistentBooking_ShouldThrowNotFoundException() {
        assertThatThrownBy(() -> bookingService.findBookingById(booker.getId(), 999L)).isInstanceOf(NotFoundException.class).hasMessageContaining("Бронирование не найдено");
    }

    @Test
    void findBookingById_WithUnauthorizedUser_ShouldThrowNotFoundException() {
        Booking booking = bookingRepository.save(Booking.builder().startDate(LocalDateTime.now().plusDays(1)).endDate(LocalDateTime.now().plusDays(2)).item(availableItem).booker(booker).status(BookingStatus.WAITING).build());

        assertThatThrownBy(() -> bookingService.findBookingById(otherUser.getId(), booking.getId())).isInstanceOf(NotFoundException.class).hasMessageContaining("нет доступа");
    }

    @ParameterizedTest
    @EnumSource(value = BookingServiceState.class, names = {"REJECTED", "CURRENT", "PAST"})
    void findAllUserBookingsWithState_WithDifferentStates_ShouldReturnFilteredBookings(BookingServiceState state) {
        Booking matchingBooking = createBookingForState(state, booker);
        Booking nonMatchingBooking = createBookingForDifferentState(state, booker);

        List<BookingDto> result = bookingService.findAllUserBookingsWithState(booker.getId(), state, 0, 10);

        assertThat(result, hasSize(1));
        assertThat(result.get(0), hasProperty("id", is(matchingBooking.getId())));
    }

    @Test
    void findAllUserBookingsWithState_WithPagination_ShouldReturnCorrectPage() {
        for (int i = 0; i < 5; i++) {
            bookingRepository.save(Booking.builder().startDate(LocalDateTime.now().plusDays(i)).endDate(LocalDateTime.now().plusDays(i + 1)).item(availableItem).booker(booker).status(BookingStatus.APPROVED).build());
        }

        List<BookingDto> result = bookingService.findAllUserBookingsWithState(booker.getId(), BookingServiceState.ALL, 2, 2);

        assertThat(result, hasSize(2));
    }

    @ParameterizedTest
    @EnumSource(value = BookingServiceState.class, names = {"REJECTED", "CURRENT", "WAITING"})
    void findAllBookingsOfUserItemsWithState_WithDifferentStates_ShouldReturnFilteredBookings(BookingServiceState state) {
        Booking matchingBooking = createBookingForState(state, booker);
        Booking nonMatchingBooking = createBookingForDifferentState(state, booker);

        List<BookingDto> result = bookingService.findAllBookingsOfUserItemsWithState(owner.getId(), state);

        assertThat(result, hasSize(1));
        assertThat(result.get(0), hasProperty("id", is(matchingBooking.getId())));
    }

    @Test
    void findAllBookingsOfUserItemsWithState_WithNoItems_ShouldThrowNotFoundException() {
        assertThatThrownBy(() -> bookingService.findAllBookingsOfUserItemsWithState(otherUser.getId(), BookingServiceState.ALL)).isInstanceOf(NotFoundException.class).hasMessageContaining("Вы не являетесь владельцем ни однйо вещи");
    }

    private Booking createBookingForState(BookingServiceState state, User booker) {
        LocalDateTime now = LocalDateTime.now();

        return switch (state) {
            case CURRENT -> createBooking(now.minusHours(1), now.plusHours(1), BookingStatus.APPROVED, booker);
            case PAST -> createBooking(now.minusDays(2), now.minusDays(1), BookingStatus.APPROVED, booker);
            case REJECTED -> createBooking(now.plusDays(1), now.plusDays(2), BookingStatus.REJECTED, booker);
            case WAITING -> createBooking(now.plusDays(1), now.plusDays(2), BookingStatus.WAITING, booker);
            default -> createBooking(now.plusDays(1), now.plusDays(2), BookingStatus.APPROVED, booker);
        };
    }

    private Booking createBookingForDifferentState(BookingServiceState state, User booker) {
        return switch (state) {
            case REJECTED -> createBookingForState(BookingServiceState.WAITING, booker);
            case WAITING, ALL -> createBookingForState(BookingServiceState.REJECTED, booker);
            case CURRENT -> createBookingForState(BookingServiceState.FUTURE, booker);
            case PAST -> createBookingForState(BookingServiceState.CURRENT, booker);
            case FUTURE -> createBookingForState(BookingServiceState.PAST, booker);
        };
    }

    private Booking createBooking(LocalDateTime start, LocalDateTime end, BookingStatus status, User booker) {
        return bookingRepository.save(Booking.builder().startDate(start).endDate(end).item(availableItem).booker(booker).status(status).build());
    }
}
