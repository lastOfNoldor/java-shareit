package ru.practicum.shareit.request.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortItemDtoForRequest {
    private Long id;
    private String name;
    private Long ownerId;
}
