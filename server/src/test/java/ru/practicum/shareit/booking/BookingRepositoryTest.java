package ru.practicum.shareit.booking;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.entity.Booking;
import ru.practicum.shareit.booking.statusEnum.StatusBooking;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingRepositoryTest {
    private static final LocalDateTime CURRENT_TIME = LocalDateTime.now();
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    private void addBookings_setUp() {
        User user = User.builder()
                .name("name")
                .email("email@gmail.com")
                .build();
        User userOwner = User.builder()
                .name("owner")
                .email("owner@gmail.com")
                .build();
        userRepository.save(userOwner);
        userRepository.save(user);
        Item item = Item.builder()
                .name("name")
                .description("description")
                .available(true)
                .owner(userOwner)
                .build();
        itemRepository.save(item);
        bookingRepository.save(Booking.builder()
                .booker(user)
                .endDate(CURRENT_TIME.plusDays(2))
                .item(item)
                .startDate(CURRENT_TIME.minusDays(1))
                .status(StatusBooking.WAITING)
                .build());
        bookingRepository.save(Booking.builder()
                .booker(user)
                .endDate(CURRENT_TIME.plusDays(2))
                .item(item)
                .startDate(CURRENT_TIME.plusDays(1))
                .status(StatusBooking.WAITING)
                .build());
    }

    @Test
    void findBookingsByBooker_IdOrderByIdAsc() {
        List<Booking> actualData = bookingRepository
                .findBookingsByBooker_IdOrderByIdAsc(2L);

        assertFalse(actualData.isEmpty());
        assertEquals(2, actualData.size());
        assertEquals(2L, actualData.get(0).getBooker().getId());
    }

    @Test
    void findFirstByItem_IdAndStartDateBeforeOrderByEndDateDesc() {
        Optional<Booking> actualBooking = bookingRepository
                .findFirstByItem_IdAndStartDateBeforeOrderByEndDateDesc(1L, CURRENT_TIME);

        assertTrue(actualBooking.isPresent());
    }

    @Test
    void findFirstByItem_IdAndStartDateAfterOrderByEndDateAsc() {
        Optional<Booking> actualBooking = bookingRepository
                .findFirstByItem_IdAndStartDateAfterOrderByEndDateAsc(1L, CURRENT_TIME);

        assertTrue(actualBooking.isPresent());
    }

    @AfterEach
    private void deleteBooking_teardown() {
        bookingRepository.deleteAll();
        userRepository.deleteAll();
        itemRepository.deleteAll();
    }
}