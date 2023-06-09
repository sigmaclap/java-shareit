package ru.practicum.shareit.user.mapper;

import lombok.Generated;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.BookerDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.entity.User;

@Component
@Generated
public class UserMapper {
    public UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public BookerDto toBookerDto(User user) {
        return BookerDto.builder()
                .id(user.getId())
                .build();
    }

    public User toUser(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }
}
