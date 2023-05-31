package ru.practicum.shareit.user.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserPatchDto;
import ru.practicum.shareit.user.entity.User;

import java.util.Optional;


@Component
@RequiredArgsConstructor
public class UserPatchMapper {

    private final UserRepository repository;

    public User toUser(UserPatchDto userPatchDto, Long userId) {
        userPatchDto.setId(userId);
        Optional<User> userOptional = repository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (userPatchDto.getName() == null) {
                userPatchDto.setName(user.getName());
            }
            if (userPatchDto.getEmail() == null) {
                userPatchDto.setEmail(user.getEmail());
            }
        } else {
            throw new UserNotFoundException("User not exists");
        }
        return User.builder()
                .id(userPatchDto.getId())
                .name(userPatchDto.getName())
                .email(userPatchDto.getEmail())
                .build();
    }
}
