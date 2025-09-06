package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.CreateUserDto;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    Collection<UserDto> findAllUsers();

    UserDto findUserById(Long id);

    UserDto createUser(CreateUserDto createUserDto);

    UserDto updateUser(Long id, UpdateUserDto updateUserDto);

    void deleteUser(Long id);
}
