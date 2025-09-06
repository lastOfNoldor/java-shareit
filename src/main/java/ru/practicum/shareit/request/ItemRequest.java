package ru.practicum.shareit.request;

import lombok.EqualsAndHashCode;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
public class ItemRequest {
    @EqualsAndHashCode.Include
    private Long id;
    private String description;
    private User requester;
    private LocalDateTime created;
}
