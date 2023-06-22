package ru.practicum.gateway.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.gateway.validated.Marker;
import ru.practicum.gateway.user.dto.UserDto;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> getUsers () {
        log.info("Get users list");
        return userClient.getUsers();
    }

    @PostMapping
    @Validated(Marker.OnCreate.class)
    public ResponseEntity<Object> createUser(@Valid @RequestBody UserDto userDto) {
        log.info("Create user {}", userDto);
        return userClient.createUser(userDto);
    }

    @PatchMapping("/{id}")
    @Validated(Marker.OnUpdate.class)
    public ResponseEntity<Object> updateUser(@Valid @RequestBody UserDto userDto,
                                              @PathVariable("id") Long userId) {
        log.info("Update user {}", userDto);
        return userClient.updateUser(userDto, userId);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Object> deleteUserById(@PathVariable("id") Long userId) {
        log.info("Delete user {}", userId);
        return userClient.deleteUserById(userId);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Object> findUserById(@PathVariable("id") Long userId) {
        log.info("Get user {}", userId);
        return userClient.findUserById(userId);
    }
}
