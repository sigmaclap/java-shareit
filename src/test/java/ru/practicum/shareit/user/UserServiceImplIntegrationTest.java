package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserServiceImplIntegrationTest {

    private final UserService service;
    private final EntityManager entityManager;
    private final UserRepository userRepository;

    @Test
    void findUserById() {
        User user = User.builder()
                .name("name")
                .email("email@gmail.com")
                .build();
        service.createUser(user);
        service.findUserById(user.getId());

        TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u WHERE u.id = :id", User.class);
        User actualUser = query.setParameter("id", user.getId()).getSingleResult();

        assertThat(actualUser.getId(), notNullValue());
        assertThat(actualUser.getName(), equalTo(user.getName()));
        assertThat(actualUser.getEmail(), equalTo(user.getEmail()));
    }
}