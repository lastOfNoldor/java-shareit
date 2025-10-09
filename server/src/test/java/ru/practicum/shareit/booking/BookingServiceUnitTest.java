// CHECKSTYLE:OFF
package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.UserRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
class BookingServiceUnitTest {

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Test
    void findAllBookingsOfUserItemsWithState_WithNullState_ShouldThrowIllegalArgumentException() {
        Long ownerId = 1L;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> bookingService.findAllBookingsOfUserItemsWithState(ownerId, null));

        assertThat(exception.getMessage(), is("State cannot be null"));
    }
}
// CHECKSTYLE:ON