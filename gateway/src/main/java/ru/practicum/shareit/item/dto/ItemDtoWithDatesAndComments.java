package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDtoWithDatesAndComments extends ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private LocalDateTime start;
    private LocalDateTime end;
    private Long ownerId;
    private List<CommentDto> comments;

}
