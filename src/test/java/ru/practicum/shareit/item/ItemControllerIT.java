package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.entity.Comment;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@SpringJUnitWebConfig(ItemController.class)
class ItemControllerIT {
    private static final String REQUEST_HEADER_SHARER_USER_ID = "X-Sharer-User-Id";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemService service;
    @MockBean
    private ItemMapper mapper;
    @MockBean
    private CommentMapper commentMapper;

    private ItemDto itemDto;
    private Long itemId;
    private Item item;
    private Long userId;

    private ItemDtoWithBooking itemDtoWithBooking;

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .build();
        itemId = 1L;
        userId = 1L;
        itemDto = ItemDto.builder()
                .name("item")
                .description("item description")
                .available(true)
                .build();
        item = Item.builder()
                .build();
        itemDtoWithBooking = ItemDtoWithBooking.builder()
                .build();
    }

    @SneakyThrows
    @Test
    void getAllItems_whenValidParam_thenReturnedListDtoItems() {
        List<ItemDtoWithBooking> expectedList = List.of(itemDtoWithBooking);
        when(service.getAllItems(userId, 0, 20)).thenReturn(expectedList);

        String result = mockMvc.perform(get("/items")
                        .header(REQUEST_HEADER_SHARER_USER_ID, userId)
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(expectedList), result);
        verify(service).getAllItems(userId, 0, 20);
    }

    @SneakyThrows
    @Test
    void getAllItems_whenNotValidParam_thenReturnedThrows() {
        List<ItemDtoWithBooking> expectedList = List.of(itemDtoWithBooking);
        when(service.getAllItems(userId, 0, 20)).thenReturn(expectedList);

        assertThrows(NestedServletException.class, () -> mockMvc.perform(get("/items")
                .header(REQUEST_HEADER_SHARER_USER_ID, userId)
                .param("from", "-1")
                .param("size", "20")));
        verify(service, never()).getAllItems(userId, 0, 20);
    }

    @SneakyThrows
    @Test
    void getItemById_whenValidData_thenReturnedItem() {
        when(service.getItemById(itemId, userId)).thenReturn(itemDtoWithBooking);

        String result = mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(REQUEST_HEADER_SHARER_USER_ID, userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemDtoWithBooking), result);
        verify(service).getItemById(itemId, userId);
    }

    @SneakyThrows
    @Test
    void createItem() {
        when(mapper.toItem(itemDto, userId)).thenReturn(item);
        when(service.createItem(userId, item)).thenReturn(item);
        when(mapper.toItemDto(item)).thenReturn(itemDto);

        String result = mockMvc.perform(post("/items")
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER_SHARER_USER_ID, userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemDto), result);
        verify(service).createItem(userId, item);
    }

    @SneakyThrows
    @Test
    void createItem_whenInvalidDataItem_thenReturnedThrows() {
        ItemDto nullableItemDto = new ItemDto();

        assertThrows(NestedServletException.class, () -> mockMvc.perform(post("/items")
                .content(objectMapper.writeValueAsString(nullableItemDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(REQUEST_HEADER_SHARER_USER_ID, userId)));

        verify(service, never()).createItem(userId, item);
    }

    @SneakyThrows
    @Test
    void updateItem() {
        when(mapper.toItem(itemDto, userId)).thenReturn(item);
        when(service.updateItem(userId, item, itemId)).thenReturn(item);
        when(mapper.toItemDto(item)).thenReturn(itemDto);

        String result = mockMvc.perform(patch("/items/{itemId}", itemId)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER_SHARER_USER_ID, userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemDto), result);
        verify(service).updateItem(userId, item, itemId);
    }

    @SneakyThrows
    @Test
    void updateItem_whenDataNullInItemDto_thenReturnedUpdatedItem() {
        ItemDto nullableItemDto = new ItemDto();

        when(mapper.toItem(nullableItemDto, userId)).thenReturn(item);
        when(service.updateItem(userId, item, itemId)).thenReturn(item);
        when(mapper.toItemDto(item)).thenReturn(nullableItemDto);


        String result = mockMvc.perform(patch("/items/{itemId}", itemId)
                        .content(objectMapper.writeValueAsString(nullableItemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER_SHARER_USER_ID, userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(nullableItemDto), result);
        verify(service).updateItem(userId, item, itemId);
    }

    @SneakyThrows
    @Test
    void searchItemForText() {
        List<Item> items = List.of(item);
        itemDto.setName("First name");
        List<ItemDto> expectedList = List.of(itemDto);
        String text = "name";
        when(service.searchItemForText(text, 0, 20)).thenReturn(items);
        when(mapper.toItemDto(items.get(0))).thenReturn(itemDto);

        String result = mockMvc.perform(get("/items/search")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("from", "0")
                        .param("size", "20")
                        .param("text", text))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(expectedList), result);
        verify(service).searchItemForText(text, 0, 20);
    }

    @SneakyThrows
    @Test
    void createComment_whenValidData_thenCreateComment() {
        CommentDtoRequest requestDto = new CommentDtoRequest();
        requestDto.setText("Hello world!");
        Comment comment = new Comment();
        CommentDto expectedComment = new CommentDto();
        when(commentMapper.toComment(requestDto)).thenReturn(comment);
        when(service.createComment(userId, comment, itemId)).thenReturn(comment);
        when(commentMapper.toCommentDto(comment)).thenReturn(expectedComment);

        String result = mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER_SHARER_USER_ID, userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(expectedComment), result);
        verify(service).createComment(userId, comment, itemId);
    }
}