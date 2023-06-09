package ru.practicum.shareit.item.mapper;

import lombok.Generated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.entity.Comment;

@Component
@RequiredArgsConstructor
@Slf4j
@Generated
public class CommentMapper {
    public CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getUser().getName())
                .created(comment.getCreatedDate())
                .build();
    }

    public Comment toComment(CommentDtoRequest commentDtoRequest) {
        return Comment.builder()
                .text(commentDtoRequest.getText())
                .build();
    }
}
