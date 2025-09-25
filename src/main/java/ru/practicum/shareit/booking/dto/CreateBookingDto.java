package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.model.Item;

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
    @NotNull(message = "item is mandatory")
    private Item item;

}
