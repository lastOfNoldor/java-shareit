package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {
    Collection<User> getAllUsers();

    Optional<User> getUserById(Long id);

    Optional<User> createUser(User user);

    User updateUser(User user);

    boolean deleteUser(Long id);

    boolean isEmailTakenByOtherUser(String email, Long userId);

}
