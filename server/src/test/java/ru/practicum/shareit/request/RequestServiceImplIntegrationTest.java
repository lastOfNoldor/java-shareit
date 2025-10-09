package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.CreateRequestDto;
import ru.practicum.shareit.request.dto.RequestDto;
import ru.practicum.shareit.request.dto.ShortItemDtoForRequest;
import ru.practicum.shareit.request.dto.ShortRequestResponse;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RequestServiceImplIntegrationTest {

    @Autowired
    private RequestService requestService;

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
    void createRequest_WithNonExistentUser_ShouldThrowNotFoundException() {
        CreateRequestDto createRequestDto = new CreateRequestDto();
        createRequestDto.setDescription("Need a hammer");

        assertThatThrownBy(() -> requestService.createRequest(999L, createRequestDto)).isInstanceOf(NotFoundException.class).hasMessageContaining("Пользователь не найден");
    }

    @Test
    void getUsersRequests_WithNoRequests_ShouldReturnEmptyList() {
        User userWithoutRequests = userRepository.save(User.builder().name("No Requests User").email("norequests@email.com").build());

        List<RequestDto> result = requestService.getUsersRequests(userWithoutRequests.getId());

        assertThat(result, empty());
    }

    @Test
    void getUsersRequests_WithMultipleRequests_ShouldReturnSortedByCreatedDesc() {
        request.setCreated(LocalDateTime.now().minusDays(3));
        requestRepository.save(request);

        ItemRequest olderRequest = requestRepository.save(ItemRequest.builder().description("Older request").requester(user).created(LocalDateTime.now().minusDays(2)).build());

        ItemRequest newerRequest = requestRepository.save(ItemRequest.builder().description("Newer request").requester(user).created(LocalDateTime.now().minusDays(1)).build());

        List<RequestDto> result = requestService.getUsersRequests(user.getId());

        assertThat(result, hasSize(3));
        assertThat(result.get(0).getDescription(), is("Newer request"));  // самый новый
        assertThat(result.get(1).getDescription(), is("Older request"));  // средний
        assertThat(result.get(2).getDescription(), is("Need a drill"));   // самый старый
    }

    @Test
    void getAllRequests_ShouldReturnOnlyOtherUsersRequestsSorted() {
        ItemRequest olderOtherRequest = requestRepository.save(ItemRequest.builder().description("Older other request").requester(otherUser).created(LocalDateTime.now().minusDays(2)).build());

        ItemRequest newerOtherRequest = requestRepository.save(ItemRequest.builder().description("Newer other request").requester(otherUser).created(LocalDateTime.now().minusDays(1)).build());

        List<ShortRequestResponse> result = requestService.getAllRequests(user.getId());

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getDescription(), is("Newer other request"));
        assertThat(result.get(1).getDescription(), is("Older other request"));
    }

    @Test
    void getAllRequests_WithMultipleUsers_ShouldExcludeCurrentUserRequests() {
        User thirdUser = userRepository.save(User.builder().name("Third User").email("third@email.com").build());

        requestRepository.save(ItemRequest.builder().description("Other user request").requester(otherUser).created(LocalDateTime.now()).build());

        requestRepository.save(ItemRequest.builder().description("Third user request").requester(thirdUser).created(LocalDateTime.now()).build());

        List<ShortRequestResponse> result = requestService.getAllRequests(user.getId());

        assertThat(result, hasSize(2));
        assertThat(result.stream().map(ShortRequestResponse::getDescription).collect(Collectors.toList()), containsInAnyOrder("Other user request", "Third user request"));
    }

    @Test
    void getRequestById_WithItemsFromDifferentOwners_ShouldReturnCorrectOwnerIds() {
        User thirdUser = userRepository.save(User.builder().name("Third User").email("third@email.com").build());

        Item item1 = itemRepository.save(Item.builder().name("Drill").description("Powerful drill").available(true).owner(otherUser).request(request).build());

        Item item2 = itemRepository.save(Item.builder().name("Extension Cord").description("Long cord").available(true).owner(thirdUser).request(request).build());

        RequestDto result = requestService.getRequestById(request.getId());

        assertThat(result.getItems(), hasSize(2));
        assertThat(result.getItems().stream().map(ShortItemDtoForRequest::getOwnerId).collect(Collectors.toList()), containsInAnyOrder(otherUser.getId(), thirdUser.getId()));
    }

    @Test
    void createRequest_ShouldSetCorrectRequesterAndTimestamps() {
        CreateRequestDto createRequestDto = new CreateRequestDto();
        createRequestDto.setDescription("Test description");

        ShortRequestResponse result = requestService.createRequest(user.getId(), createRequestDto);

        assertThat(result, allOf(hasProperty("description", is("Test description")), hasProperty("id", notNullValue()), hasProperty("created", notNullValue())));

        ItemRequest savedRequest = requestRepository.findById(result.getId()).orElseThrow();
        assertThat(savedRequest.getRequester().getId(), is(user.getId()));
        assertThat(savedRequest.getDescription(), is("Test description"));
    }
}
