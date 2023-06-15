package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ItemServiceImplIntegrationTest {
    private final ItemService service;
    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;

    private User user;
    private User userOwner;
    private ItemRequest itemRequest;

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

    }

    @Test
    void testCreateItem() {
        Item item = Item.builder()
                .name("name")
                .description("description")
                .available(true)
                .owner(userOwner)
                .itemRequest(itemRequest)
                .build();

        service.createItem(userOwner.getId(), item);

        TypedQuery<Item> query = entityManager
                .createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item actualItem = query.setParameter("id", item.getId()).getSingleResult();

        assertThat(actualItem.getId(), notNullValue());
        assertThat(actualItem.getName(), equalTo(item.getName()));
        assertThat(actualItem.getDescription(), equalTo(item.getDescription()));
        assertThat(actualItem.getAvailable(), equalTo(item.getAvailable()));
        assertThat(actualItem.getOwner().getId(), equalTo(item.getOwner().getId()));
        assertThat(actualItem.getItemRequest().getId(), equalTo(item.getItemRequest().getId()));
    }
}