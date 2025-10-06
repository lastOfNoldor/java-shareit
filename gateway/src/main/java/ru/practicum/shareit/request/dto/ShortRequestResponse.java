package ru.practicum.shareit.request.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ShortRequestResponse {
    private Long id;
    private String description;
    private long requesterId;
    private LocalDateTime created;

}
