package ru.practicum.shareit.request.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ShortItemDtoForRequest> items;


}
