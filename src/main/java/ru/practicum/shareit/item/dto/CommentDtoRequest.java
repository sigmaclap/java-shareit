package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.user.entity.User;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class CommentDtoRequest {
    private Long id;
    @NotBlank
    private String text;
    private Item item;
    private User user;
    private final LocalDateTime createdDate = LocalDateTime.now();
}
