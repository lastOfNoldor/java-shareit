package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {

        // Создание тестовых пользователей
        owner = userRepository.save(User.builder().name("Owner").email("owner@email.com").build());

        booker = userRepository.save(User.builder().name("Booker").email("booker@email.com").build());

        item = itemRepository.save(Item.builder().name("Test Item").description("Test Description").available(true).owner(owner).build());

        booking = bookingRepository.save(Booking.builder().startDate(LocalDateTime.now().plusDays(1)).endDate(LocalDateTime.now().plusDays(3)).item(item).booker(booker).status(BookingStatus.WAITING).build());
    }

    @Test
    void createBooking_WithoutUserId_ShouldReturnBadRequest() throws Exception {
        String bookingJson = """
                {
                    "start": "%s",
                    "end": "%s",
                    "itemId": %d
                }
                """.formatted(LocalDateTime.now().plusDays(1).toString(), LocalDateTime.now().plusDays(2).toString(), item.getId());

        mockMvc.perform(post("/bookings").contentType(MediaType.APPLICATION_JSON).content(bookingJson)).andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking_WithOwnerUser_ShouldApproveBooking() throws Exception {
        mockMvc.perform(patch("/bookings/{bookingId}", booking.getId()).header("X-Sharer-User-Id", owner.getId()).param("approved", "true").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void approveBooking_WithNonOwnerUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(patch("/bookings/{bookingId}", booking.getId()).header("X-Sharer-User-Id", booker.getId()) // не владелец
                .param("approved", "true").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    void findBookingById_WithAuthorizedUser_ShouldReturnBooking() throws Exception {
        mockMvc.perform(get("/bookings/{bookingId}", booking.getId()).header("X-Sharer-User-Id", booker.getId())).andExpect(status().isOk()).andExpect(jsonPath("$.id", is(booking.getId().intValue())));

        mockMvc.perform(get("/bookings/{bookingId}", booking.getId()).header("X-Sharer-User-Id", owner.getId())).andExpect(status().isOk()).andExpect(jsonPath("$.id", is(booking.getId().intValue())));
    }

    @Test
    void findBookingById_WithUnauthorizedUser_ShouldReturnNotFound() throws Exception {
        User otherUser = userRepository.save(User.builder().name("Other User").email("other@email.com").build());

        mockMvc.perform(get("/bookings/{bookingId}", booking.getId()).header("X-Sharer-User-Id", otherUser.getId())).andExpect(status().isNotFound());
    }


    @Test
    void findAllUserBookingsWithState_WithFutureState_ShouldReturnFutureBookings() throws Exception {
        Booking pastBooking = bookingRepository.save(Booking.builder().startDate(LocalDateTime.now().minusDays(3)).endDate(LocalDateTime.now().minusDays(1)).item(item).booker(booker).status(BookingStatus.APPROVED).build());

        mockMvc.perform(get("/bookings").header("X-Sharer-User-Id", booker.getId()).param("state", "FUTURE").param("from", "0").param("size", "10")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1))) // только будущее бронирование
                .andExpect(jsonPath("$[0].id", is(booking.getId().intValue())));
    }

    @Test
    void findAllUserBookingsWithState_WithInvalidState_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings").header("X-Sharer-User-Id", booker.getId()).param("state", "INVALID_STATE").param("from", "0").param("size", "10")).andExpect(status().isBadRequest());
    }

    @Test
    void findAllBookingsOfUserItemsWithState_WithOwner_ShouldReturnBookings() throws Exception {
        mockMvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", owner.getId()).param("state", "ALL")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].id", is(booking.getId().intValue()))).andExpect(jsonPath("$[0].item.id", is(item.getId().intValue())));
    }

    @Test
    void findAllBookingsOfUserItemsWithState_WithNonOwner_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", booker.getId()).param("state", "ALL")).andExpect(status().isNotFound());
    }

    @Test
    void findAllBookingsOfUserItemsWithState_WithWaitingState_ShouldReturnWaitingBookings() throws Exception {
        Booking approvedBooking = bookingRepository.save(Booking.builder().startDate(LocalDateTime.now().plusDays(2)).endDate(LocalDateTime.now().plusDays(4)).item(item).booker(booker).status(BookingStatus.APPROVED).build());

        mockMvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", owner.getId()).param("state", "WAITING")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1))) // только waiting бронирование
                .andExpect(jsonPath("$[0].id", is(booking.getId().intValue()))).andExpect(jsonPath("$[0].status", is("WAITING")));
    }


    @Test
    void createBooking_WithInvalidDates_ShouldReturnBadRequest() throws Exception {
        String bookingJson = """
                {
                    "start": "%s",
                    "end": "%s",
                    "itemId": %d
                }
                """.formatted(LocalDateTime.now().plusDays(2).toString(), LocalDateTime.now().plusDays(1).toString(), // end раньше start
                item.getId());

        mockMvc.perform(post("/bookings").header("X-Sharer-User-Id", booker.getId()).contentType(MediaType.APPLICATION_JSON).content(bookingJson)).andExpect(status().isBadRequest());
    }

    @Test
    void findAllBookingsOfUserItemsWithState_WithPastState_ShouldReturnPastBookings() throws Exception {
        User owner = userRepository.save(new User(null, "Owner", "owner@email.com"));
        User booker = userRepository.save(new User(null, "Booker", "booker@email.com"));
        Item item = itemRepository.save(Item.builder().name("Test Item").description("Test Description").available(true).owner(owner).build());

        Booking pastBooking = bookingRepository.save(Booking.builder().startDate(LocalDateTime.now().minusDays(3)).endDate(LocalDateTime.now().minusDays(1)).item(item).booker(booker).status(BookingStatus.APPROVED).build());

        mockMvc.perform(get("/bookings/owner").header("X-Sharer-User-Id", owner.getId()).param("state", "PAST")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(pastBooking.getId())).andExpect(jsonPath("$[0].status").value("APPROVED"));
    }

}