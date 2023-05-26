package ru.practicum.shareit.user;

import java.util.List;
import java.util.Map;

public interface UserRepository {
    List<User> getUsers();

    Map<Long, User> getUserMap();

    User createUser(User user);

    User updateUser(User user, Long userId);

    User findUserById(Long userId);

    void deleteUserById(Long userId);
}
