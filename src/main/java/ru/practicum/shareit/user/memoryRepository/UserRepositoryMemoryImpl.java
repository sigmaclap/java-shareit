package ru.practicum.shareit.user.memoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.InvalidDataException;
import ru.practicum.shareit.exceptions.UserAlreadyExistException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.entity.User;

import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryMemoryImpl implements UserRepositoryMemory {
    private static final String ERROR_EMAIL_ALREADY_EXIST = "User email already exists";

    private final Map<Long, User> userMap = new HashMap<>();
    private long generatedID = 0;

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(userMap.values());
    }

    @Override
    public Map<Long, User> getUserMap() {
        return userMap;
    }

    @Override
    public User createUser(User user) {
        if (userMap.containsKey(user.getId())) {
            log.error("User already exists");
            throw new UserAlreadyExistException("User already exists");
        } else if (user.toString().isEmpty()) {
            log.error("Empty value User");
            throw new InvalidDataException("Empty value User");
        } else if (isExistsEmail(user)) {
            log.error(ERROR_EMAIL_ALREADY_EXIST);
            throw new UserAlreadyExistException(ERROR_EMAIL_ALREADY_EXIST);
        } else {
            user.setId(++generatedID);
            userMap.put(user.getId(), user);
            log.info("User successfully created {}", user.getName());
        }
        return user;
    }

    private boolean isExistsEmail(User user) {
        if (userMap.isEmpty()) return false;
        return userMap.values().stream()
                .filter(user1 -> !Objects.equals(user1.getId(), user.getId()))
                .map(User::getEmail)
                .anyMatch(user1 -> user1.equals(user.getEmail()));
    }

    @Override
    public User updateUser(User user, Long userId) {
        if (!userMap.containsKey(userId)) {
            log.error("User ID not found");
            throw new UserNotFoundException("User ID not found");
        }
        if (isExistsEmail(user)) {
            log.error(ERROR_EMAIL_ALREADY_EXIST);
            throw new UserAlreadyExistException(ERROR_EMAIL_ALREADY_EXIST);
        }
        userMap.remove(userId);
        userMap.put(userId, user);
        log.info("User successfully created {}", user.getName());
        return user;
    }

    @Override
    public User findUserById(Long userId) {
        return userMap.values().stream()
                .filter(p -> p.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(String.format("User № %d not found", userId)));
    }

    @Override
    public void deleteUserById(Long userId) {
        if (!userMap.containsKey(userId)) {
            log.error("User not found");
            throw new UserNotFoundException("User not found");
        }
        userMap.remove(userId);
    }
}
