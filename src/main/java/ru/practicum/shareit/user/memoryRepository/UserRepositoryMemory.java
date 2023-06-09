package ru.practicum.shareit.user.memoryRepository;

import ru.practicum.shareit.user.entity.User;

import java.util.List;
import java.util.Map;

public interface UserRepositoryMemory {
    List<User> getUsers();

    Map<Long, User> getUserMap();

    User createUser(User user);

    User updateUser(User user, Long userId);

    User findUserById(Long userId);

    void deleteUserById(Long userId);
}
