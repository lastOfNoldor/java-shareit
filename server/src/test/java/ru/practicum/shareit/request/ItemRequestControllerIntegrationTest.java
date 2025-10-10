// CHECKSTYLE:OFF
package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ItemRequestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User user;
    private User otherUser;
    private ItemRequest request;

    @BeforeEach
    void setUp() {

        user = userRepository.save(User.builder().name("Test User").email("test@email.com").build());

        otherUser = userRepository.save(User.builder().name("Other User").email("other@email.com").build());

        request = requestRepository.save(ItemRequest.builder().description("Need a drill").requester(user).created(LocalDateTime.now()).build());
    }

    @Test
    void createRequest_WithValidData_ShouldReturnRequest() throws Exception {
        String requestJson = """
                {
                    "description": "Need a hammer"
                }
                """;

        mockMvc.perform(post("/requests").header("X-Sharer-User-Id", user.getId()).contentType(MediaType.APPLICATION_JSON).content(requestJson)).andExpect(status().isOk()).andExpect(jsonPath("$.description", is("Need a hammer"))).andExpect(jsonPath("$.id", notNullValue())).andExpect(jsonPath("$.created", notNullValue()));
    }

    @Test
    void createRequest_WithoutUserId_ShouldReturnBadRequest() throws Exception {
        String requestJson = """
                {
                    "description": "Need a hammer"
                }
                """;

        mockMvc.perform(post("/requests").contentType(MediaType.APPLICATION_JSON).content(requestJson)).andExpect(status().isBadRequest());
    }

    @Test
    void getUsersRequests_ShouldReturnUserRequests() throws Exception {
        ItemRequest secondRequest = requestRepository.save(ItemRequest.builder().description("Need a saw").requester(user).created(LocalDateTime.now()).build());

        mockMvc.perform(get("/requests").header("X-Sharer-User-Id", user.getId())).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getUsersRequests_WithoutUserId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests")).andExpect(status().isBadRequest());
    }

    @Test
    void getAllRequests_ShouldReturnOtherUsersRequests() throws Exception {
        ItemRequest otherUserRequest = requestRepository.save(ItemRequest.builder().description("Other user request").requester(otherUser).created(LocalDateTime.now()).build());

        mockMvc.perform(get("/requests/all").header("X-Sharer-User-Id", user.getId())).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].description", is("Other user request")));
    }

    @Test
    void getAllRequests_ShouldNotReturnOwnRequests() throws Exception {
        mockMvc.perform(get("/requests/all").header("X-Sharer-User-Id", user.getId())).andExpect(status().isOk()).andExpect(jsonPath("$", empty()));
    }

    @Test
    void getAllRequests_WithoutUserId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests/all")).andExpect(status().isBadRequest());
    }

    @Test
    void getRequestById_WithExistingRequest_ShouldReturnRequest() throws Exception {
        mockMvc.perform(get("/requests/{reqId}", request.getId())).andExpect(status().isOk()).andExpect(jsonPath("$.id", is(request.getId().intValue()))).andExpect(jsonPath("$.description", is("Need a drill")));
    }

    @Test
    void getRequestById_WithNonExistentRequest_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/requests/{reqId}", 999L)).andExpect(status().isNotFound());
    }

    @Test
    void getRequestById_WithRequestContainingItems_ShouldReturnRequestWithItems() throws Exception {
        Item item = itemRepository.save(Item.builder().name("Drill").description("Powerful drill").available(true).owner(otherUser).request(request).build());

        mockMvc.perform(get("/requests/{reqId}", request.getId())).andExpect(status().isOk()).andExpect(jsonPath("$.items", hasSize(1))).andExpect(jsonPath("$.items[0].name", is("Drill"))).andExpect(jsonPath("$.items[0].id", is(item.getId().intValue()))).andExpect(jsonPath("$.items[0].ownerId", is(otherUser.getId().intValue())));
    }

    @Test
    void getUsersRequests_ShouldReturnRequestsWithItems() throws Exception {
        Item item = itemRepository.save(Item.builder().name("Drill").description("Powerful drill").available(true).owner(otherUser).request(request).build());

        mockMvc.perform(get("/requests").header("X-Sharer-User-Id", user.getId())).andExpect(status().isOk()).andExpect(jsonPath("$[0].items", hasSize(1))).andExpect(jsonPath("$[0].items[0].name", is("Drill")));
    }
}
// CHECKSTYLE:ON