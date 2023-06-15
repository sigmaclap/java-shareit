package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.entity.Booking;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.statusEnum.StatusBooking;
import ru.practicum.shareit.exceptions.InvalidDataException;
import ru.practicum.shareit.exceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.entity.Comment;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.entity.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    private static final LocalDateTime CURRENT_DATE_TIME = LocalDateTime.now();
    private static final int PAGE = 0;
    private static final int SIZE = 20;
    @Mock
    private ItemRepository repository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    ItemServiceImpl itemService;


    @Test
    void getAllItems_whenValidData_thenReturnedCorrectList() {
        long userId = 0L;
        Page<Item> itemsUserOwner = new PageImpl<>(List.of(new Item()));
        List<CommentDto> comments = new ArrayList<>();
        ItemDtoWithBooking itemDto = new ItemDtoWithBooking();
        itemDto.setId(1L);
        Item item = itemsUserOwner.getContent().get(0);
        item.setId(1L);
        when(repository.findAllByOwner_IdOrderByIdAsc(userId, PageRequest.of(PAGE, SIZE)))
                .thenReturn(itemsUserOwner);
        when(itemMapper.toItemDtoBooking(item, comments))
                .thenReturn(itemDto);
//        when(bookingRepository.findFirstByItem_IdAndStartDateBeforeOrderByEndDateDesc(item.getId(), currentDateTime))
//                .thenReturn(Optional.of(new Booking()));
//        when(bookingRepository.findFirstByItem_IdAndStartDateAfterOrderByEndDateAsc(item.getId(), currentDateTime))
//                .thenReturn(Optional.of(new Booking()));

        List<ItemDtoWithBooking> actualList = itemService.getAllItems(userId, PAGE, SIZE);

        assertEquals(1, actualList.size());
        InOrder inOrder = Mockito.inOrder(repository, itemMapper, bookingRepository, commentRepository);
        inOrder.verify(repository)
                .findAllByOwner_IdOrderByIdAsc(userId, PageRequest.of(PAGE, SIZE));
        inOrder.verify(commentRepository).findCommentsByUser_Id(userId);
        inOrder.verify(itemMapper)
                .toItemDtoBooking(item, comments);
//        inOrder.verify(bookingRepository)
//                .findFirstByItem_IdAndStartDateBeforeOrderByEndDateDesc(item.getId(), currentDateTime);
//        inOrder.verify(bookingRepository)
//                .findFirstByItem_IdAndStartDateAfterOrderByEndDateAsc(item.getId(), currentDateTime);
    }

    @Test
    void getItemById_whenItemFound_thenReturnedItemDtoWithBooking() {
        long userId = 1L;
        Item expectedItem = new Item();
        List<CommentDto> comments = new ArrayList<>();
        expectedItem.setId(1L);
        expectedItem.setOwner(User.builder()
                .id(1L)
                .build());
        ItemDtoWithBooking ex = new ItemDtoWithBooking();
        ex.setComments(comments);
        when(repository.findById(any())).thenReturn(Optional.of(expectedItem));
        when(itemMapper.toItemDtoBooking(expectedItem, comments)).thenReturn(ex);


        ItemDtoWithBooking actualItem = itemService.getItemById(ex.getId(), userId);

        assertEquals(ex, actualItem);
    }

    @Test
    void getItemById_whenUserNotOwner_thenReturnedItemsWithoutBookingsInfo() {
        long userId = 1L;
        Item expectedItem = new Item();
        List<CommentDto> comments = new ArrayList<>();
        expectedItem.setId(1L);
        expectedItem.setOwner(User.builder()
                .id(2L)
                .build());
        ItemDtoWithBooking ex = new ItemDtoWithBooking();
        ex.setComments(comments);
        when(repository.findById(any())).thenReturn(Optional.of(expectedItem));
        when(itemMapper.toItemDtoBooking(expectedItem, comments)).thenReturn(ex);


        ItemDtoWithBooking actualItem = itemService.getItemById(ex.getId(), userId);

        assertEquals(ex, actualItem);
        assertNull(actualItem.getLastBooking());
        assertNull(actualItem.getNextBooking());
    }

    @Test
    void createItem_whenUserExistsAndItemExists_thenSavedItem() {
        long userId = 1L;
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        User user = User.builder().id(userId).build();
        Item expectedItem = new Item();
        expectedItem.setItemRequest(request);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(itemRequestRepository.getReferenceById(expectedItem.getItemRequest().getId()))
                .thenReturn(request);
        when(repository.save(expectedItem)).thenReturn(expectedItem);

        Item actualItem = itemService.createItem(userId, expectedItem);

        verify(repository, times(1)).save(expectedItem);
        assertEquals(expectedItem, actualItem);
    }

    @Test
    void createItem_whenUserExistsAndItemNotExists_thenSavedItem() {
        long userId = 1L;
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        User user = User.builder().id(userId).build();
        Item expectedItem = new Item();
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(repository.save(expectedItem)).thenReturn(expectedItem);

        Item actualItem = itemService.createItem(userId, expectedItem);

        verify(repository, times(1)).save(expectedItem);
        assertEquals(expectedItem, actualItem);
        assertNull(actualItem.getItemRequest());
    }

    @Test
    void createItem_whenUserNotExists_thenReturnedThrows() {
        long userId = 1L;
        Item expectedItem = new Item();
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> itemService.createItem(userId, expectedItem));
        verify(repository, never()).save(expectedItem);
    }

    @Test
    void updateItem_whenAllValidProperties_thenUpdateItem() {
        long userId = 1L;
        Item expectedItem = Item.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(true)
                .owner(User.builder()
                        .id(1L)
                        .build())
                .build();
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        User user = User.builder().id(userId).build();
        expectedItem.setItemRequest(request);
        when(repository.findById(expectedItem.getId())).thenReturn(Optional.of(expectedItem));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(repository.save(expectedItem)).thenReturn(expectedItem);

        Item actualItem = itemService.updateItem(userId, expectedItem, 1L);

        verify(repository, times(1)).save(expectedItem);
        assertEquals(expectedItem, actualItem);
        verify(repository, times(2)).findById(expectedItem.getId());
    }

    @Test
    void updateItem_whenItemIdNotFound_thenReturnedThrown() {
        long userId = 1L;
        long anyItemId = 100L;
        Item expectedItem = Item.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(true)
                .owner(User.builder()
                        .id(1L)
                        .build())
                .build();
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        expectedItem.setItemRequest(request);
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.updateItem(userId, expectedItem, anyItemId));
        verify(repository, never()).save(expectedItem);
    }

    @Test
    void updateItem_whenOwnerNotFound_thenReturnedThrown() {
        long userId = 1L;
        long anyItemId = 1L;
        Item expectedItem = Item.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(true)
                .owner(User.builder()
                        .id(1L)
                        .build())
                .build();
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        expectedItem.setItemRequest(request);
        when(repository.findById(expectedItem.getId())).thenReturn(Optional.of(expectedItem));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> itemService.updateItem(userId, expectedItem, anyItemId));
        verify(repository, never()).save(expectedItem);
    }

    @Test
    void updateItem_whenDataNewItemIsEmpty_thenUpdateItem() {
        long userId = 1L;
        long anyItemId = 1L;
        Item expectedItem = Item.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(true)
                .owner(User.builder()
                        .id(1L)
                        .build())
                .build();
        Item newItem = Item.builder()
                .id(1L)
                .build();
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        User user = User.builder().id(userId).build();
        expectedItem.setItemRequest(request);
        when(repository.findById(expectedItem.getId())).thenReturn(Optional.of(expectedItem));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        Item actualItem = itemService.updateItem(userId, newItem, anyItemId);

        assertEquals("name", actualItem.getName());
        assertEquals("description", actualItem.getDescription());
        assertEquals(true, actualItem.getAvailable());
        verify(repository).save(newItem);
    }

    @Test
    void searchItemForText_whenTextNotEmpty_thenSearchItemForText() {
        Item expectedItem = Item.builder()
                .name("Find name")
                .build();
        when(repository.searchItemForText("name", PageRequest.of(PAGE, SIZE)))
                .thenReturn(new PageImpl<>(List.of(expectedItem)));

        List<Item> actualItems = itemService.searchItemForText("name", PAGE, SIZE);

        assertEquals("Find name", actualItems.get(0).getName());
    }

    @Test
    void searchItemForText_whenTextEmpty_thenSearchItemForText() {
        List<Item> actualItems = itemService.searchItemForText("", PAGE, SIZE);

        assertEquals(0, actualItems.size());
        verify(repository, never()).searchItemForText("", PageRequest.of(PAGE, SIZE));
    }

    @Test
    void createComment_whenItemForCommentFind_thenCreatedComment() {
        Comment expectedComment = new Comment();
        User userRented = User.builder()
                .id(1L)
                .build();
        Item itemRented = Item.builder()
                .id(1L)
                .available(true)
                .name("name")
                .description("description")
                .owner(new User())
                .itemRequest(new ItemRequest())
                .build();
        Booking booking = Booking.builder()
                .id(1L)
                .booker(userRented)
                .status(StatusBooking.APPROVED)
                .endDate(LocalDateTime.now().minusDays(1))
                .item(itemRented)
                .build();
        Booking bookingSecond = Booking.builder()
                .id(2L)
                .status(StatusBooking.REJECTED)
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
        List<Booking> bookings = List.of(booking, bookingSecond);
        when(bookingRepository.findBookingsByBooker_IdOrderByIdAsc(anyLong()))
                .thenReturn(bookings);
        when(userRepository.getReferenceById(anyLong())).thenReturn(userRented);
        when(repository.getReferenceById(anyLong())).thenReturn(itemRented);
        when(commentRepository.save(expectedComment)).thenReturn(expectedComment);

        Comment actualComment = itemService.createComment(userRented.getId(), expectedComment, itemRented.getId());

        assertEquals(expectedComment, actualComment);
        InOrder inOrder = Mockito.inOrder(bookingRepository, userRepository, repository, commentRepository);
        inOrder.verify(bookingRepository).findBookingsByBooker_IdOrderByIdAsc(anyLong());
        inOrder.verify(userRepository).getReferenceById(anyLong());
        inOrder.verify(repository).getReferenceById(anyLong());
        inOrder.verify(commentRepository).save(expectedComment);
    }

    @Test
    void createComment_whenItemForCommentNotFind_thenReturnedThrown() {
        Comment expectedComment = new Comment();
        User userRented = User.builder()
                .id(1L)
                .build();
        Item itemRented = Item.builder()
                .id(1L)
                .available(true)
                .name("name")
                .description("description")
                .owner(new User())
                .itemRequest(new ItemRequest())
                .build();
        Booking booking = Booking.builder()
                .id(1L)
                .booker(userRented)
                .status(StatusBooking.WAITING)
                .endDate(LocalDateTime.now().minusDays(1))
                .item(itemRented)
                .build();
        Booking bookingSecond = Booking.builder()
                .id(2L)
                .status(StatusBooking.REJECTED)
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
        List<Booking> bookings = List.of(booking, bookingSecond);
        when(bookingRepository.findBookingsByBooker_IdOrderByIdAsc(anyLong()))
                .thenReturn(bookings);

        Throwable ex = assertThrows(InvalidDataException.class,
                () -> itemService.createComment(userRented.getId(), expectedComment, itemRented.getId()));
        assertEquals("Could not find item for this renter", ex.getMessage());
        verify(commentRepository, never()).save(expectedComment);
    }
}