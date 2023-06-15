package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JsonTest
class BookingDtoTest {
    @Autowired
    private JacksonTester<BookingDto> json;
    @Autowired
    private ObjectMapper objectMapper;

    @SneakyThrows
    @Test
    void testBookingDto() {
        BookingDto bookingDto = BookingDto.builder()
                .itemId(10L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(1))
                .build();

        JsonContent<BookingDto> result = json.write(bookingDto);
        String actual = json.write(bookingDto)
                .getJson();

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(10);
        assertEquals(actual, objectMapper.writeValueAsString(bookingDto));
    }
}