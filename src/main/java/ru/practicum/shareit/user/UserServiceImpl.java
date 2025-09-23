package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validator.CentralValidator;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CentralValidator centralValidator;


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
        Optional<User> resultUser = Optional.of(userRepository.save(createdUser));
        log.info("Попытка создания нового пользователя:  id={}", resultUser.get().getId());
        return UserMapper.userToDto(resultUser.orElseThrow(() -> new ConflictException("Такой email уже существует")));
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

    @Override
    public void deleteUser(Long id) {
        if (id == null) {
            throw new ValidationException("отсутствует Id пользователя");
        }
//        userRepository.delete(userRepository.findById(id));
        log.info("Пользователь с ID {} удален", id);
    }


}
