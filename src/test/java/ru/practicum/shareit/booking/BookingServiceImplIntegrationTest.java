package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.entity.Booking;
import ru.practicum.shareit.booking.statusEnum.StatusBooking;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.entity.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.entity.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class BookingServiceImplIntegrationTest {
    private final BookingService service;
    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;

    private User user;
    private User userOwner;
    private ItemRequest itemRequest;
    private Item item;

    @BeforeEach
    private void setUp() {
        user = User.builder()
                .name("name")
                .email("email@gmail.com")
                .build();
        user = userRepository.save(user);
        userOwner = User.builder()
                .name("name")
                .email("e12mail@gmail.com")
                .build();
        userOwner = userRepository.save(userOwner);
        itemRequest = ItemRequest.builder()
                .description("description")
                .requester(user)
                .build();
        itemRequest = itemRequestRepository.save(itemRequest);
        item = Item.builder()
                .name("name")
                .description("description")
                .available(true)
                .owner(userOwner)
                .itemRequest(itemRequest)
                .build();
        item = itemRepository.save(item);

    }

    @Test
    void createBookingRequest() {
        Booking booking = Booking.builder()
                .startDate(LocalDateTime.now().plusMinutes(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user)
                .status(StatusBooking.APPROVED)
                .build();

        service.createBookingRequest(booking, user.getId());

        TypedQuery<Booking> query = entityManager
                .createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking actualBooking = query.setParameter("id", booking.getId()).getSingleResult();

        assertThat(actualBooking.getId(), notNullValue());
        assertThat(actualBooking.getItem(), equalTo(booking.getItem()));
        assertThat(actualBooking.getBooker(), equalTo(booking.getBooker()));
        assertThat(actualBooking.getStatus(), equalTo(booking.getStatus()));
    }
}