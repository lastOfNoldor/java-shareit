// CHECKSTYLE:OFF
package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.RequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private RequestRepository itemRequestRepository;

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
    void createItem_WithRequestId_ShouldCreateItemWithRequest() {
        ItemRequest request = itemRequestRepository.save(ItemRequest.builder().description("Need this item").requester(booker).created(LocalDateTime.now()).build());

        CreateItemDto createItemDto = new CreateItemDto();
        createItemDto.setName("Requested Item");
        createItemDto.setDescription("Created from request");
        createItemDto.setAvailable(true);
        createItemDto.setRequestId(request.getId());

        ItemDto result = itemService.createItem(owner.getId(), createItemDto);

        assertThat(result, allOf(hasProperty("name", is("Requested Item")), hasProperty("description", is("Created from request")), hasProperty("available", is(true))));

        Item savedItem = itemRepository.findById(result.getId()).orElseThrow();
        assertThat(savedItem, hasProperty("request", allOf(notNullValue(), hasProperty("id", is(request.getId())))));
    }

    @Test
    void findItemById_WhenNotOwnerButWithComments_ShouldReturnComments() {
        Booking pastBooking = bookingRepository.save(Booking.builder().startDate(LocalDateTime.now().minusDays(3)).endDate(LocalDateTime.now().minusDays(1)).item(item).booker(booker).status(BookingStatus.APPROVED).build());

        Comment comment = commentRepository.save(Comment.builder().text("Nice item!").item(item).author(booker).created(LocalDateTime.now()).build());

        ItemDtoWithMultipleBookings result = itemService.findItemById(booker.getId(), item.getId());

        assertThat(result.getComments(), hasSize(1));
        assertThat(result.getComments().getFirst(), hasProperty("text", is("Nice item!")));
        assertThat(result, allOf(hasProperty("lastBooking", nullValue()), hasProperty("nextBooking", nullValue())));
    }


    @Test
    void findItemById_WhenItemHasRequest_ShouldIncludeRequestId() {
        ItemRequest request = itemRequestRepository.save(ItemRequest.builder().description("Test request").requester(booker).created(LocalDateTime.now()).build());

        Item itemWithRequest = itemRepository.save(Item.builder().name("Item with Request").description("For request test").available(true).owner(owner).request(request).build());

        ItemDtoWithMultipleBookings result = itemService.findItemById(owner.getId(), itemWithRequest.getId());

        assertThat(result, hasProperty("requestId", is(request.getId())));
    }

    @Test
    void findAllUsersItems_WhenItemHasBookingAndComments_ShouldReturnWithDatesAndComments() {
        Booking booking = bookingRepository.save(Booking.builder().startDate(LocalDateTime.now().minusDays(2)).endDate(LocalDateTime.now().plusDays(1)).item(item).booker(booker).status(BookingStatus.APPROVED).build());

        Comment comment = commentRepository.save(Comment.builder().text("Test comment").item(item).author(booker).created(LocalDateTime.now()).build());

        Collection<ItemDto> result = itemService.findAllUsersItems(owner.getId());

        assertThat(result, hasSize(1));
        ItemDto itemDto = result.iterator().next();

        assertThat(itemDto, instanceOf(ItemDtoWithDatesAndComments.class));
        ItemDtoWithDatesAndComments dtoWithDates = (ItemDtoWithDatesAndComments) itemDto;

        assertThat(dtoWithDates.getStart(), notNullValue());
        assertThat(dtoWithDates.getEnd(), notNullValue());
        assertThat(dtoWithDates.getComments(), hasSize(1));
        assertThat(dtoWithDates.getComments().getFirst(), hasProperty("text", is("Test comment")));
    }

    @Test
    void findItemById_WhenIdIsNull_ShouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> itemService.findItemById(owner.getId(), null));
    }

    @Test
    void updateItem_WhenItemIdIsNull_ShouldThrowValidationException() {
        UpdateItemDto updateDto = new UpdateItemDto();
        updateDto.setName("Updated");

        assertThrows(ValidationException.class, () -> itemService.updateItem(owner.getId(), null, updateDto));
    }

    @Test
    void updateItem_WhenUserIdIsNull_ShouldThrowValidationException() {
        UpdateItemDto updateDto = new UpdateItemDto();
        updateDto.setName("Updated");

        assertThrows(ValidationException.class, () -> itemService.updateItem(null, item.getId(), updateDto));
    }

    @Test
    void deleteItem_WhenIdIsNull_ShouldThrowValidationException() {
        assertThrows(ValidationException.class, () -> itemService.deleteItem(null));
    }

    @Test
    void updateItem_WithAllFields_ShouldUpdateAllFields() {
        UpdateItemDto updateDto = new UpdateItemDto();
        updateDto.setName("Fully Updated");
        updateDto.setDescription("Fully Updated Description");
        updateDto.setAvailable(false); // Эта ветка сейчас не покрыта!

        UpdateItemDto result = itemService.updateItem(owner.getId(), item.getId(), updateDto);

        assertThat(result, allOf(hasProperty("name", is("Fully Updated")), hasProperty("description", is("Fully Updated Description")), hasProperty("available", is(false))));
    }

    @Test
    void itemToDtoWithDatesAndComments_ShouldMapCorrectly() {
        List<Comment> comments = List.of(Comment.builder().id(1L).text("Comment 1").author(booker).created(LocalDateTime.now()).build());

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        Long requestId = 999L;

        ItemDtoWithDatesAndComments result = ItemMapper.itemToDtoWithDatesAndComments(item, start, end, comments, requestId);

        assertThat(result, allOf(hasProperty("id", is(item.getId())), hasProperty("name", is(item.getName())), hasProperty("start", is(start)), hasProperty("end", is(end)), hasProperty("comments", hasSize(1)), hasProperty("requestId", is(requestId))));

    }

    @Test
    void searchItem_WhenItemHasRequest_ShouldIncludeRequestId() {
        ItemRequest request = itemRequestRepository.save(ItemRequest.builder().description("Test request").requester(booker).created(LocalDateTime.now()).build());

        Item itemWithRequest = itemRepository.save(Item.builder().name("Searchable Item").description("For search test").available(true).owner(owner).request(request).build());

        Collection<UpdateItemDto> result = itemService.searchItem("searchable");

        assertThat(result, hasSize(1));
        UpdateItemDto dto = result.iterator().next();
        assertThat(dto, hasProperty("requestId", is(request.getId())));
    }


}
// CHECKSTYLE:ON