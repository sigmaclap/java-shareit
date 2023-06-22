package ru.practicum.shareit.item.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingOwnerDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoWithBookingTest {
    @Autowired
    private JacksonTester<ItemDtoWithBooking> json;

    @SneakyThrows
    @Test
    void testItemDtoWithBooking() {
        ItemDtoWithBooking itemDtoWithBooking = ItemDtoWithBooking.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(true)
                .lastBooking(BookingOwnerDto.builder()
                        .id(1L)
                        .bookerId(333L)
                        .build())
                .nextBooking(BookingOwnerDto.builder()
                        .id(2L)
                        .build())
                .comments(List.of(CommentDto.builder()
                        .id(1L)
                        .build()))
                .build();

        JsonContent<ItemDtoWithBooking> result = json.write(itemDtoWithBooking);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("name");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("description");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(1);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.bookerId").isEqualTo(333);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(2);
        assertThat(result).extractingJsonPathNumberValue("$.comments[0].id").isEqualTo(1);
    }

}