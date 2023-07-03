package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.request.entity.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.entity.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;


@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(
        properties = {"spring.datasource.driverClassName=org.h2.Driver",
                "spring.datasource.url=jdbc:h2:mem:test", "spring.datasource.username=test",
                "spring.datasource.password=test"},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ItemRequestServiceImplIntegrationTest {

    private final ItemRequestService service;
    private final UserRepository userRepository;
    private final EntityManager em;

    private User user;

    @BeforeEach
    private void setUp() {
        user = User.builder()
                .name("name")
                .email("email@gmail.com")
                .build();
        user = userRepository.save(user);
    }

    @Test
    void testCreateItemRequest() {
        ItemRequest itemRequest = ItemRequest.builder()
                .description("description")
                .requester(user)
                .build();

        service.createItemRequest(itemRequest);

        TypedQuery<ItemRequest> query = em
                .createQuery("SELECT it FROM ItemRequest it WHERE it.id = :id", ItemRequest.class);
        ItemRequest actualRequest = query.setParameter("id", itemRequest.getId()).getSingleResult();

        assertThat(actualRequest.getId(), notNullValue());
        assertThat(actualRequest.getDescription(), equalTo(itemRequest.getDescription()));
        assertThat(actualRequest.getRequester().getId(), notNullValue());
        assertThat(actualRequest.getRequester().getId(), equalTo(itemRequest.getRequester().getId()));
    }
}