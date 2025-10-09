package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User(null, "Owner", "owner@email.com"));
        booker = userRepository.save(new User(null, "Booker", "booker@email.com"));

        item = itemRepository.save(Item.builder().name("Test Item").description("Test Description").available(true).owner(owner).build());
    }

    @Test
    void createItem_WithValidData_ShouldReturnItem() throws Exception {
        String itemJson = """
                {
                    "name": "New Item",
                    "description": "New Description",
                    "available": true
                }
                """;

        mockMvc.perform(post("/items").header("X-Sharer-User-Id", owner.getId()).contentType(MediaType.APPLICATION_JSON).content(itemJson)).andExpect(status().isOk()).andExpect(jsonPath("$.id").exists()).andExpect(jsonPath("$.name").value("New Item")).andExpect(jsonPath("$.description").value("New Description")).andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void findAllItems_ShouldReturnUserItems() throws Exception {
        Item secondItem = itemRepository.save(Item.builder().name("Second Item").description("Second Description").available(true).owner(owner).build());

        mockMvc.perform(get("/items").header("X-Sharer-User-Id", owner.getId())).andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].name").value("Test Item")).andExpect(jsonPath("$[1].name").value("Second Item"));
    }

    @Test
    void findItemById_WhenUserIsOwner_ShouldReturnWithBookings() throws Exception {
        Booking pastBooking = bookingRepository.save(Booking.builder().startDate(LocalDateTime.now().minusDays(2)).endDate(LocalDateTime.now().minusDays(1)).item(item).booker(booker).status(BookingStatus.APPROVED).build());

        Booking futureBooking = bookingRepository.save(Booking.builder().startDate(LocalDateTime.now().plusDays(1)).endDate(LocalDateTime.now().plusDays(2)).item(item).booker(booker).status(BookingStatus.APPROVED).build());

        mockMvc.perform(get("/items/{itemId}", item.getId()).header("X-Sharer-User-Id", owner.getId())).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(item.getId())).andExpect(jsonPath("$.name").value("Test Item")).andExpect(jsonPath("$.lastBooking").exists()).andExpect(jsonPath("$.nextBooking").exists());
    }

    @Test
    void findItemById_WhenUserIsNotOwner_ShouldReturnWithoutBookings() throws Exception {
        mockMvc.perform(get("/items/{itemId}", item.getId()).header("X-Sharer-User-Id", booker.getId())).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(item.getId())).andExpect(jsonPath("$.name").value("Test Item")).andExpect(jsonPath("$.lastBooking").doesNotExist()).andExpect(jsonPath("$.nextBooking").doesNotExist());
    }

    @Test
    void updateItem_ShouldUpdateAndReturnItem() throws Exception {
        String updateJson = """
                {
                    "name": "Updated Name",
                    "description": "Updated Description"
                }
                """;

        mockMvc.perform(patch("/items/{itemId}", item.getId()).header("X-Sharer-User-Id", owner.getId()).contentType(MediaType.APPLICATION_JSON).content(updateJson)).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Updated Name")).andExpect(jsonPath("$.description").value("Updated Description"));
    }

    @Test
    void searchItem_WithMatchingText_ShouldReturnItems() throws Exception {
        String searchText = "test";

        mockMvc.perform(get("/items/search").param("text", searchText)).andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].name").value("Test Item"));
    }

    @Test
    void searchItem_WithEmptyText_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/items/search").param("text", "")).andExpect(status().isOk()).andExpect(jsonPath("$").isArray()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createComment_WithValidData_ShouldReturnComment() throws Exception {
        Booking pastBooking = bookingRepository.save(Booking.builder().startDate(LocalDateTime.now().minusDays(3)).endDate(LocalDateTime.now().minusDays(1)).item(item).booker(booker).status(BookingStatus.APPROVED).build());

        String commentJson = """
                {
                    "text": "Great item!"
                }
                """;

        mockMvc.perform(post("/items/{itemId}/comment", item.getId()).header("X-Sharer-User-Id", booker.getId()).contentType(MediaType.APPLICATION_JSON).content(commentJson)).andExpect(status().isOk()).andExpect(jsonPath("$.text").value("Great item!")).andExpect(jsonPath("$.authorName").value("Booker"));
    }

    @Test
    void createComment_WithoutBooking_ShouldReturnBadRequest() throws Exception {
        String commentJson = """
                {
                    "text": "Great item!"
                }
                """;

        mockMvc.perform(post("/items/{itemId}/comment", item.getId()).header("X-Sharer-User-Id", owner.getId()) // владелец не бронировал свою вещь
                .contentType(MediaType.APPLICATION_JSON).content(commentJson)).andExpect(status().isBadRequest());
    }

    @Test
    void deleteItem_ShouldDeleteItem() throws Exception {
        mockMvc.perform(delete("/items/{id}", item.getId()).contentType(MediaType.APPLICATION_JSON).content(item.getId().toString())).andExpect(status().isOk());

        mockMvc.perform(get("/items/{itemId}", item.getId()).header("X-Sharer-User-Id", owner.getId())).andExpect(status().isNotFound());
    }

    @Test
    void createItem_WithoutUserId_ShouldReturnBadRequest() throws Exception {
        String itemJson = """
                {
                    "name": "New Item",
                    "description": "New Description",
                    "available": true
                }
                """;

        mockMvc.perform(post("/items").contentType(MediaType.APPLICATION_JSON).content(itemJson)).andExpect(status().isBadRequest());
    }

}
