package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exceptions.ItemRequestNotFoundException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.request.entity.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.entity.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;
    private long userId;
    private long requestId;
    private User expectedUser;
    private ItemRequest expectedRequest;

    @BeforeEach
    void setUp() {
        userId = 0L;
        requestId = 0L;
        expectedRequest = new ItemRequest();
        expectedUser = new User();
    }

    @Test
    void createItemRequest_whenValidData_thenReturnedItemRequest() {
        when(itemRequestRepository.save(expectedRequest)).thenReturn(expectedRequest);

        ItemRequest actualRequest = itemRequestService.createItemRequest(expectedRequest);

        assertEquals(expectedRequest, actualRequest);
        verify(itemRequestRepository).save(expectedRequest);
    }

    @Test
    void getAllItemRequestOwner_whenUserExist_thenReturnedExpectedList() {
        List<ItemRequest> expectedList = List.of(new ItemRequest());
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));
        when(itemRequestRepository.findAllByRequester_IdOrderByCreatedDesc(userId))
                .thenReturn(expectedList);

        List<ItemRequest> actualList = itemRequestService.getAllItemRequestOwner(userId);

        assertEquals(1, actualList.size());
        assertEquals(expectedList, actualList);
    }

    @Test
    void getAllItemRequestOwner_whenUserNotExist_thenNotReturnedExpectedList() {
        doThrow(UserNotFoundException.class).when(userRepository).findById(userId);

        assertThrows(UserNotFoundException.class, () -> itemRequestService.getAllItemRequestOwner(userId));
        verify(itemRequestRepository, never()).findAllByRequester_IdOrderByCreatedDesc(userId);
    }

    @Test
    void getAllItemRequestOwner_whenOwnerListEmpty_thenReturnedEmptyList() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));
        when(itemRequestRepository.findAllByRequester_IdOrderByCreatedDesc(userId))
                .thenReturn(Collections.emptyList());

        List<ItemRequest> actualList = itemRequestService.getAllItemRequestOwner(userId);

        assertEquals(0, actualList.size());
        assertEquals(new ArrayList<>(), actualList);
    }

    @Test
    void findRequestItemById_whenUserExistAndRequestExist_thenReturnedExpectedRequest() {
        ItemRequest expectedRequest = new ItemRequest();
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(expectedRequest));

        ItemRequest actualRequest = itemRequestService.findRequestItemById(userId, requestId);

        assertEquals(expectedRequest, actualRequest);
        verify(itemRepository).findAllByItemRequest_Id(expectedRequest.getId());
    }

    @Test
    void findRequestItemById_whenUserNotExist_thenReturnedThrown() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> itemRequestService.findRequestItemById(userId, requestId));
        verify(itemRepository, never()).findAllByItemRequest_Id(expectedRequest.getId());
    }

    @Test
    void findRequestItemById_whenRequestNotExist_thenReturnedThrown() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());


        assertThrows(ItemRequestNotFoundException.class, () -> itemRequestService.findRequestItemById(userId, requestId));
        verify(itemRepository, never()).findAllByItemRequest_Id(expectedRequest.getId());
    }

    @Test
    void findAllUsersRequests_whenUserExist_thenReturnedExpectedListRequests() {
        ItemRequest request = new ItemRequest();
        request.setId(requestId);
        List<ItemRequest> expectedRequests = List.of(request);
        List<Item> items = List.of(new Item());
        Page<ItemRequest> expectedResult = new PageImpl<>(expectedRequests);
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));
        when(itemRequestRepository.findAllByRequester_IdNotOrderByCreatedDesc(userId, PageRequest.of(0, 20)))
                .thenReturn(expectedResult);
        when(itemRepository.findAllByItemRequest_Id(requestId)).thenReturn(items);

        List<ItemRequest> actualRequests = itemRequestService.findAllUsersRequests(userId, 0, 20);

        assertEquals(1, actualRequests.size());
        assertEquals(expectedResult.getContent(), actualRequests);
        assertNotNull(expectedResult);
        assertEquals(items, actualRequests.get(0).getItems());
    }

    @Test
    void findAllUsersRequests_whenUserNotExist_thenReturnedThrown() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> itemRequestService.findAllUsersRequests(userId, 0, 20));
        verify(itemRepository, never()).findAllByItemRequest_Id(expectedRequest.getId());
    }

    @Test
    void findAllUsersRequests_whenItemRequestsIsEmpty_thenReturnedThrown() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));
        when(itemRequestRepository.findAllByRequester_IdNotOrderByCreatedDesc(userId, PageRequest.of(0, 20)))
                .thenReturn(Page.empty());

        List<ItemRequest> actualList = itemRequestService.findAllUsersRequests(userId, 0, 20);

        assertEquals(0, actualList.size());
        assertEquals(new ArrayList<>(), actualList);
        verify(itemRepository, never()).findAllByItemRequest_Id(requestId);
    }
}