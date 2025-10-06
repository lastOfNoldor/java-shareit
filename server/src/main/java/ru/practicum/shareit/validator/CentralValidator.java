package ru.practicum.shareit.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UpdateUserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CentralValidator {
    private final UserRepository userRepository;

    public void updatedUserEmailIsTaken(User existingUser, UpdateUserDto updateUserDto) {
        String newEmail = updateUserDto.getEmail();
        if (newEmail != null && !existingUser.getEmail().equals(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new ConflictException("Email уже занят другим пользователем");
        }
    }

    public void updatedItemAccess(Item existingItem, Long userId) {
        if (!Objects.equals(existingItem.getOwner().getId(), userId)) {
            throw new NotFoundException("Отказано в доступе.Пользователь не является владельцем вещи.");
        }
    }

    public Long userIdFormatValidation(String userIdStr) {
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Неверный формат Id пользователя");
        }
    }

}
