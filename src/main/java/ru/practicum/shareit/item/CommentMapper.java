package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.time.LocalDateTime;

public class CommentMapper {
    public static Comment createDtoToComment(CreateCommentDto createCommentDto) {
        return Comment.builder().text(createCommentDto.getText()).created(LocalDateTime.now()).build();
    }

    public static CommentDto commentToDto(Comment saved, String authorName) {

        return CommentDto.builder().id(saved.getId()).text(saved.getText()).authorName(authorName).created(saved.getCreated()).build();
    }
}
