package ru.practicum.shareit.user;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

@Repository
@Qualifier("userRepository")
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final HashMap<Long, User> tempRepository = new HashMap<>();
    Long counter = 0L;

    @Override
    public Collection<User> getAllUsers() {
        return tempRepository.values();
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return tempRepository.values().stream().filter(user -> user.getId().equals(id)).findFirst();
    }

    @Override
    public Optional<User> createUser(User user) {
        if (tempRepository.values().stream().anyMatch(emailUser -> emailUser.getEmail().equals(user.getEmail()))) {
            return Optional.empty();
        }
        user.setId(++counter);
        tempRepository.put(user.getId(), user);
        return Optional.of(user);
    }

    @Override
    public User updateUser(User user) {
        tempRepository.put(user.getId(), user);
        return user;
    }

    public boolean isEmailTakenByOtherUser(String email, Long userId) {
        return tempRepository.values().stream().filter(user -> !user.getId().equals(userId)).anyMatch(user -> user.getEmail().equals(email));
    }

    @Override
    public boolean deleteUser(Long id) {
        tempRepository.remove(id);
        return true;

    }
}
