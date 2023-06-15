package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.request.entity.ItemRequest;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@SpringJUnitWebConfig(ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemRequestService itemRequestService;
    @MockBean
    private ItemRequestMapper itemRequestMapper;

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .build();
    }

    @SneakyThrows
    @Test
    void createItemRequest_whenDataRequestValid_thenReturnedCreateItemRequest() {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("not null")
                .build();
        ItemResponseDto itemResponseDto = ItemResponseDto.builder()
                .description("not null")
                .build();
        long userId = 1L;
        ItemRequest itemRequest = new ItemRequest();
        when(itemRequestMapper.toItemRequest(itemRequestDto, userId))
                .thenReturn(itemRequest);
        when(itemRequestService.createItemRequest(itemRequest))
                .thenReturn(itemRequest);
        when(itemRequestMapper.itemRequestDto(itemRequest))
                .thenReturn(itemResponseDto);

        String result = mockMvc.perform(post("/requests")
                        .content(objectMapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemResponseDto), result);
        verify(itemRequestService).createItemRequest(itemRequest);
    }

    @SneakyThrows
    @Test
    void createItemRequest_whenDataRequestNotValid_thenReturnedBadRequest() {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description(null)
                .build();
        long userId = 1L;
        ItemRequest itemRequest = new ItemRequest();

        mockMvc.perform(post("/requests")
                        .content(objectMapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));

        verify(itemRequestService, never()).createItemRequest(itemRequest);
    }

    @SneakyThrows
    @Test
    void getAllItemRequestOwner_whenValidData_thenReturnedListItemRequests() {
        Long userId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .description("description")
                .build();
        List<ItemRequest> itemRequests = List.of(itemRequest);
        ItemResponseDto itemResponseDto = ItemResponseDto.builder()
                .description("not null")
                .build();
        List<ItemResponseDto> listDtoResponse = List.of(itemResponseDto);
        when(itemRequestService.getAllItemRequestOwner(userId))
                .thenReturn(itemRequests);
        when(itemRequestMapper.itemRequestDto(itemRequests.get(0)))
                .thenReturn(itemResponseDto);

        String result = mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(listDtoResponse), result);
        verify(itemRequestService).getAllItemRequestOwner(userId);
    }

    @SneakyThrows
    @Test
    void findRequestItemById_whenValidData_thenReturnedRequestItemById() {
        Long userId = 1L;
        Long requestId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .description("description")
                .build();
        ItemResponseDto itemResponseDto = ItemResponseDto.builder()
                .description("not null")
                .build();
        when(itemRequestService.findRequestItemById(userId, requestId))
                .thenReturn(itemRequest);
        when(itemRequestMapper.itemRequestDto(itemRequest))
                .thenReturn(itemResponseDto);

        String result = mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(itemResponseDto), result);
        verify(itemRequestService).findRequestItemById(userId, requestId);
    }

    @SneakyThrows
    @Test
    void findAllUsersRequests_whenValidData_thenReturnedListItemRequest() {
        Long userId = 1L;
        ItemRequest itemRequest = ItemRequest.builder()
                .description("description")
                .build();
        List<ItemRequest> itemRequests = List.of(itemRequest);
        ItemResponseDto itemResponseDto = ItemResponseDto.builder()
                .description("not null")
                .build();
        List<ItemResponseDto> listDtoResponse = List.of(itemResponseDto);
        when(itemRequestService.findAllUsersRequests(userId, 0, 20))
                .thenReturn(itemRequests);
        when(itemRequestMapper.itemRequestDto(itemRequests.get(0)))
                .thenReturn(itemResponseDto);

        String result = mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(listDtoResponse), result);
        verify(itemRequestService).findAllUsersRequests(userId, 0, 20);
    }

    @SneakyThrows
    @Test
    void findAllUsersRequests_whenNotValidFrom_thenReturnedThrows() {
        Long userId = 1L;

        assertThrows(NestedServletException.class, () -> mockMvc.perform(get("/requests/all")
                .header("X-Sharer-User-Id", userId)
                .param("from", "-1")
                .param("size", "20")));
        verify(itemRequestService, never()).findAllUsersRequests(userId, 0, 20);
    }

    @SneakyThrows
    @Test
    void findAllUsersRequests_whenNotValidSize_thenReturnedThrows() {
        Long userId = 1L;

        assertThrows(NestedServletException.class, () -> mockMvc.perform(get("/requests/all")
                .header("X-Sharer-User-Id", userId)
                .param("from", "0")
                .param("size", "0")));
        verify(itemRequestService, never()).findAllUsersRequests(userId, 0, 20);
    }

    @SneakyThrows
    @Test
    void findAllUsersRequests_whenNotValidSizeGreaterThan50_thenReturnedThrows() {
        Long userId = 1L;

        assertThrows(NestedServletException.class, () -> mockMvc.perform(get("/requests/all")
                .header("X-Sharer-User-Id", userId)
                .param("from", "0")
                .param("size", "51")));
        verify(itemRequestService, never()).findAllUsersRequests(userId, 0, 20);
    }
}