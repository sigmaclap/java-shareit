package ru.practicum.shareit.user.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserPatchDto;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserPatchMapper {

    private final UserRepository repository;

    public User toUser(UserPatchDto userPatchDto, Long userId) {
        userPatchDto.setId(userId);
        Map<Long, User> userList = repository.getUserMap();
        if (userList.containsKey(userPatchDto.getId()) && userPatchDto.getName() == null) {
            userPatchDto.setName(userList.get(userPatchDto.getId()).getName());
        } else if (userList.containsKey(userPatchDto.getId()) && userPatchDto.getEmail() == null) {
            userPatchDto.setEmail(userList.get(userPatchDto.getId()).getEmail());
        }
        return User.builder()
                .id(userPatchDto.getId())
                .name(userPatchDto.getName())
                .email(userPatchDto.getEmail())
                .build();
    }
}
