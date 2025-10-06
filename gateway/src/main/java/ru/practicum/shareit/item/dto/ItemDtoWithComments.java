package ru.practicum.shareit.item.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDtoWithComments {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long ownerId;
    private List<CommentDto> comments;
}
