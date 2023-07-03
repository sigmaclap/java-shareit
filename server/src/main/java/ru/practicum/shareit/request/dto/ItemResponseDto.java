package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDtoForRequestList;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ItemResponseDto {
    private Long id;
    private String description;
    private final LocalDateTime created;
    private List<ItemDtoForRequestList> items;
}
