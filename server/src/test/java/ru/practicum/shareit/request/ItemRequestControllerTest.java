package ru.practicum.shareit.request;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.test.web.servlet.ResultActions;
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
    private static final String REQUEST_HEADER_SHARER_USER_ID = "X-Sharer-User-Id";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemRequestService itemRequestService;
    @MockBean
    private ItemRequestMapper itemRequestMapper;
    private long userId;
    private ItemRequest itemRequest;
    private ItemResponseDto itemResponseDto;
    private ItemRequestDto itemRequestDto;

    @BeforeEach
    void setUp() {
        userId = 1L;
        itemRequest = ItemRequest.builder()
                .description("description")
                .build();
        itemResponseDto = ItemResponseDto.builder()
                .description("not null")
                .build();
        itemRequestDto = ItemRequestDto.builder()
                .description("not null")
                .build();
    }

    @SneakyThrows
    @Test
    void createItemRequest_whenDataRequestValid_thenReturnedCreateItemRequestStatusCode200() {
        ItemRequest itemRequest = new ItemRequest();
        when(itemRequestMapper.toItemRequest(itemRequestDto, userId))
                .thenReturn(itemRequest);
        when(itemRequestService.createItemRequest(itemRequest))
                .thenReturn(itemRequest);
        when(itemRequestMapper.itemRequestDto(itemRequest))
                .thenReturn(itemResponseDto);

        String result = performPost(itemRequestDto)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        checkEqualsResult(itemResponseDto, result);
        verify(itemRequestService).createItemRequest(itemRequest);
    }

//    @SneakyThrows
//    @Test
//    void createItemRequest_whenDataRequestNotValid_thenReturnedBadRequestStatusCode400() {
//        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
//                .description(null)
//                .build();
//
//        performPost(itemRequestDto)
//                .andExpect(status().is(400));
//
//        verify(itemRequestService, never()).createItemRequest(itemRequest);
//    }

    @SneakyThrows
    @Test
    void getAllItemRequestOwner_whenValidData_thenReturnedListItemRequestsStatusCode200() {
        List<ItemRequest> itemRequests = List.of(itemRequest);
        List<ItemResponseDto> listDtoResponse = List.of(itemResponseDto);
        when(itemRequestService.getAllItemRequestOwner(userId))
                .thenReturn(itemRequests);
        when(itemRequestMapper.itemRequestDto(itemRequests.get(0)))
                .thenReturn(itemResponseDto);

        String result = mockMvc.perform(get("/requests")
                        .header(REQUEST_HEADER_SHARER_USER_ID, userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        checkEqualsResult(listDtoResponse, result);
        verify(itemRequestService).getAllItemRequestOwner(userId);
    }

    @SneakyThrows
    @Test
    void findRequestItemById_whenValidData_thenReturnedRequestItemByIdStatusCode200() {
        Long requestId = 1L;
        when(itemRequestService.findRequestItemById(userId, requestId))
                .thenReturn(itemRequest);
        when(itemRequestMapper.itemRequestDto(itemRequest))
                .thenReturn(itemResponseDto);

        String result = mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(REQUEST_HEADER_SHARER_USER_ID, userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        checkEqualsResult(itemResponseDto, result);
        verify(itemRequestService).findRequestItemById(userId, requestId);
    }

    @SneakyThrows
    @Test
    void findAllUsersRequests_whenValidData_thenReturnedListItemRequestStatusCode200() {
        List<ItemRequest> itemRequests = List.of(itemRequest);
        List<ItemResponseDto> listDtoResponse = List.of(itemResponseDto);
        when(itemRequestService.findAllUsersRequests(userId, 0, 20))
                .thenReturn(itemRequests);
        when(itemRequestMapper.itemRequestDto(itemRequests.get(0)))
                .thenReturn(itemResponseDto);

        String result = mockMvc.perform(get("/requests/all")
                        .header(REQUEST_HEADER_SHARER_USER_ID, userId)
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        checkEqualsResult(listDtoResponse, result);
        verify(itemRequestService).findAllUsersRequests(userId, 0, 20);
    }

//    @SneakyThrows
//    @Test
//    void findAllUsersRequests_whenNotValidFrom_thenReturnedThrowsErrorNestedServletException() {
//        assertThrows(NestedServletException.class, () -> mockMvc.perform(get("/requests/all")
//                .header(REQUEST_HEADER_SHARER_USER_ID, userId)
//                .param("from", "-1")
//                .param("size", "20")));
//        verify(itemRequestService, never()).findAllUsersRequests(userId, 0, 20);
//    }
//
//    @SneakyThrows
//    @Test
//    void findAllUsersRequests_whenNotValidSize_thenReturnedThrowsErrorNestedServletException() {
//        assertThrows(NestedServletException.class, () -> mockMvc.perform(get("/requests/all")
//                .header(REQUEST_HEADER_SHARER_USER_ID, userId)
//                .param("from", "0")
//                .param("size", "0")));
//        verify(itemRequestService, never()).findAllUsersRequests(userId, 0, 20);
//    }

//    @SneakyThrows
//    @Test
//    void findAllUsersRequests_whenNotValidSizeGreaterThan50_thenReturnedThrowsErrorNestedServletException() {
//        assertThrows(NestedServletException.class, () -> mockMvc.perform(get("/requests/all")
//                .header(REQUEST_HEADER_SHARER_USER_ID, userId)
//                .param("from", "0")
//                .param("size", "51")));
//        verify(itemRequestService, never()).findAllUsersRequests(userId, 0, 20);
//    }

    private ResultActions performPost(Object expected) throws Exception {
        return mockMvc.perform(post("/requests")
                .content(objectMapper.writeValueAsString(expected))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .header(REQUEST_HEADER_SHARER_USER_ID, userId)
                .accept(MediaType.APPLICATION_JSON));
    }

    private void checkEqualsResult(Object expected, String result) throws JsonProcessingException {
        assertEquals(objectMapper.writeValueAsString(expected), result);
    }
}