package ru.practicum.shareit.user;

import ru.practicum.shareit.user.entity.User;

import java.util.List;

public interface UserService {
    List<User> getUsers();

    User createUser(User user);

    User updateUser(User user, Long userId);

    User findUserById(Long userId);

    void deleteUserById(Long userId);
}
