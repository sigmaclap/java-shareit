package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    @Override
    public List<User> getUsers() {
        return repository.getUsers();
    }

    @Override
    public User createUser(User user) {
        return repository.createUser(user);
    }

    @Override
    public User updateUser(User user, Long userId) {
        return repository.updateUser(user, userId);
    }

    @Override
    public User findUserById(Long userId) {
        return repository.findUserById(userId);
    }

    @Override
    public void deleteUserById(Long userId) {
        repository.deleteUserById(userId);
    }
}
