package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingDto {
    @NotNull(message = "start time is mandatory")
    private LocalDateTime start;
    @NotNull(message = "end time is mandatory")
    private LocalDateTime end;
    @NotNull(message = "itemId time is mandatory")
    private Long itemId;

}
