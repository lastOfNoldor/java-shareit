package ru.practicum.shareit.booking.dto;

import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortBookingDto {
    private LocalDateTime start;
    private LocalDateTime end;
}
