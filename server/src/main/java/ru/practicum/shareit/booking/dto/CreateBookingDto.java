package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
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
    @NotNull(message = "item id is mandatory")
    private Long itemId;
    @NotNull(message = "start time is mandatory")
    @FutureOrPresent
    private LocalDateTime start;
    @NotNull(message = "end time is mandatory")
    @Future
    private LocalDateTime end;

}
