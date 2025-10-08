package ru.practicum.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingGateController;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

// Правильные импорты Mockito
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

// Правильные импорты MockMvc
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@WebMvcTest(BookingGateController.class)
class BookingGateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void bookItem_ShouldReturnBooking() throws Exception {
        // Given
        String mockResponse = """
            {
                "id": 1,
                "start": "2024-01-01T10:00:00",
                "end": "2024-01-02T10:00:00",
                "status": "WAITING"
            }
            """;

        when(bookingClient.bookItem(eq(1L), any(BookItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        // When & Then
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "itemId": 1,
                        "start": "2024-01-01T10:00:00",
                        "end": "2024-01-02T10:00:00"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }
}
