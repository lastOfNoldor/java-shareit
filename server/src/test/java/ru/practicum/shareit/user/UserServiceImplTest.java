package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validator.CentralValidator;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CentralValidator centralValidator;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private UserServiceImpl userService;


    @Test
    void findUserById_WhenUserExists_ShouldReturnUserDto() {
        Long userId = 1L;
        User user = new User(userId, "test@email.com", "Test User");
        UserDto expectedDto = UserMapper.userToDto(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDto result = userService.findUserById(userId);

        assertThat(result, is(equalTo(expectedDto)));
        verify(userRepository).findById(userId);
    }


    @Test
    void findUserById_WhenUserNotExists_ShouldThrowNotFoundException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.findUserById(userId));

        assertThat(exception, is(notNullValue()));
        assertThat(exception, instanceOf(NotFoundException.class));
        assertThat(exception.getMessage(), is("User с Id" + userId + "не найден"));

        verify(userRepository).findById(userId);
    }

    @Test
    void findUserById_WhenIdIsNull_ShouldThrowValidationException() {
        ValidationException exception = assertThrows(ValidationException.class, () -> userService.findUserById(null));

        assertThat(exception.getMessage(), is("отсутствует Id пользователя"));
    }

}
