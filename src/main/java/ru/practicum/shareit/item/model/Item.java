package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.user.model.User;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
@AllArgsConstructor
public class Item {
    @EqualsAndHashCode.Include
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private User owner;

}
