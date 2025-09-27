package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validator.CentralValidator;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CentralValidator centralValidator;
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;


    @Override
    public Collection<UserDto> findAllUsers() {
        log.info("Попытка получения списка всех пользователей.");
        return userRepository.findAll().stream().map(UserMapper::userToDto).collect(Collectors.toList());
    }

    @Override
    public UserDto findUserById(Long id) {
        log.info("Попытка получения пользователя по ID: {}", id);
        if (id == null) {
            throw new ValidationException("отсутствует Id пользователя");
        }
        Optional<User> userById = userRepository.findById(id);
        return UserMapper.userToDto(userById.orElseThrow(() -> new NotFoundException("User с Id" + id + "не найден")));
    }

    @Override
    public UserDto createUser(CreateUserDto createUserDto) {
        log.info("Попытка создания нового пользователя: email={}, name={}", createUserDto.getEmail(), createUserDto.getName());
        User createdUser = UserMapper.dtoToNewUser(createUserDto);
        if (userRepository.findByEmail(createdUser.getEmail()).isPresent())
            throw new ConflictException("Такой email уже существует");
        User resultUser = userRepository.save(createdUser);
        log.info("Попытка создания нового пользователя:  id={}", resultUser.getId());
        return UserMapper.userToDto(resultUser);
    }

    @Override
    public UserDto updateUser(Long id, UpdateUserDto updateUserDto) {
        log.info("Попытка обновления пользователя с ID: {}", id);
        if (id == null) {
            throw new ValidationException("отсутствует Id пользователя");
        }
        User existingUser = userRepository.findById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        centralValidator.updatedUserEmailIsTaken(existingUser, updateUserDto);
        User updatedUser = UserMapper.dtoToUpdatedUser(existingUser, updateUserDto);
        User resultUser = userRepository.save(updatedUser);
        log.info("Успешное обновление пользователя с ID: {}", id);
        return UserMapper.userToDto(resultUser);
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        if (id == null) {
            throw new ValidationException("отсутствует Id пользователя");
        }
        bookingRepository.deleteByBooker_Id(id);
        List<Item> userItems = itemRepository.findAllByOwner_Id(id);
        for (Item item : userItems) {
            bookingRepository.deleteByItem_Id(item.getId());
            itemRepository.delete(item);
        }
        userRepository.deleteById(id);
        log.info("Пользователь с ID {} удален", id);
    }
}
