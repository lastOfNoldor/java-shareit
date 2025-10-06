package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRequestDto {
    @NotBlank(message = "Description cannot be blank")
    @Size(max = 1000, message = "Description too long")
    private String description;

}
