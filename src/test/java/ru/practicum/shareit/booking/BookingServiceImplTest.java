package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.entity.Booking;
import ru.practicum.shareit.booking.statusEnum.StatusBooking;
import ru.practicum.shareit.booking.statusEnum.StatusState;
import ru.practicum.shareit.exceptions.BookingNotFoundException;
import ru.practicum.shareit.exceptions.InvalidDataException;
import ru.practicum.shareit.exceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.entity.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.entity.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    private final LocalDateTime currentDateTime = LocalDateTime.now();
    @Mock
    private BookingRepository repository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Captor
    private ArgumentCaptor<Booking> bookingArgumentCaptor;

    Item item;
    User userOwner;
    User userBooker;


    @BeforeEach
    void addDataToBase() {
        userOwner = User.builder()
                .id(10L)
                .name("name")
                .email("email")
                .build();

        userBooker = User.builder()
                .id(1L)
                .name("name1")
                .email("email1")
                .build();

        item = Item.builder()
                .id(1L)
                .name("name")
                .owner(userOwner)
                .description("description")
                .build();
    }

    @Test
    void createBookingRequest_whenItemFound_thenReturnedBooking() {
        Long userId = 1L;
        Booking bookingToSave = Booking.builder()
                .id(1L)
                .item(item)
                .build();
        when(itemRepository.findById(bookingToSave.getItem().getId())).thenReturn(Optional.of(item));
        when(repository.save(bookingToSave)).thenReturn(bookingToSave);

        Booking actualBooking = bookingService.createBookingRequest(bookingToSave, userId);

        assertEquals(bookingToSave, actualBooking);
        verify(repository, times(1)).save(bookingToSave);
    }

    @Test
    void createBookingRequest_whenItemNotFound_thenReturnedThrown() {
        long userId = 1L;
        Booking bookingToSave = Booking.builder()
                .id(1L)
                .item(item)
                .build();
        when(itemRepository.findById(userId)).thenReturn(Optional.empty());

        Throwable exception = assertThrows(ItemNotFoundException.class,
                () -> bookingService.createBookingRequest(bookingToSave, userId));

        assertEquals("Item with id not found", exception.getMessage());
        verify(repository, never()).save(bookingToSave);
    }

    @Test
    void createBookingRequest_whenAvailableFalse_thenThrown() {
        Long userId = 1L;
        item.setAvailable(false);
        Booking bookingToSave = Booking.builder()
                .id(1L)
                .item(item)
                .build();
        when(itemRepository.findById(bookingToSave.getItem().getId())).thenReturn(Optional.of(item));

        Throwable exception = assertThrows(InvalidDataException.class,
                () -> bookingService.createBookingRequest(bookingToSave, userId));

        assertEquals("Item not available for booking", exception.getMessage());
        verify(repository, never()).save(bookingToSave);
    }

    @Test
    void createBookingRequest_whenOwnerEqualsRequesterUser_thenThrown() {
        Long userId = 10L;
        Booking bookingToSave = Booking.builder()
                .id(1L)
                .item(item)
                .build();
        when(itemRepository.findById(bookingToSave.getItem().getId())).thenReturn(Optional.of(item));

        Throwable exception = assertThrows(UserNotFoundException.class,
                () -> bookingService.createBookingRequest(bookingToSave, userId));

        assertEquals("Owner can't create request to create own thing", exception.getMessage());
        verify(repository, never()).save(bookingToSave);
    }

    @Test
    void updateBookingStatusByOwner_whenApprovedStatusTrue_thenUpdateBooking() {
        Long userId = 10L;
        boolean approvedStatus = true;
        Booking booking = Booking.builder()
                .id(1L)
                .item(item)
                .status(StatusBooking.WAITING)
                .build();
        when(repository.findById(booking.getId())).thenReturn(Optional.of(booking));

        Booking actualBooking = bookingService.updateBookingStatusByOwner(booking.getId(), userId, approvedStatus);

        verify(repository).save(bookingArgumentCaptor.capture());
        Booking savedBooking = bookingArgumentCaptor.getValue();

        assertEquals(StatusBooking.APPROVED, savedBooking.getStatus());
        assertEquals(1L, savedBooking.getId());
        verify(repository, times(1)).save(bookingArgumentCaptor.capture());
    }

    @Test
    void updateBookingStatusByOwner_whenApprovedStatusFalse_thenUpdateBooking() {
        Long userId = 10L;
        boolean approvedStatus = false;
        Booking booking = Booking.builder()
                .id(1L)
                .item(item)
                .status(StatusBooking.WAITING)
                .build();
        when(repository.findById(booking.getId())).thenReturn(Optional.of(booking));

        Booking actualBooking = bookingService.updateBookingStatusByOwner(booking.getId(), userId, approvedStatus);

        verify(repository).save(bookingArgumentCaptor.capture());
        Booking savedBooking = bookingArgumentCaptor.getValue();

        assertEquals(StatusBooking.REJECTED, savedBooking.getStatus());
        assertEquals(1L, savedBooking.getId());
        verify(repository, times(1)).save(bookingArgumentCaptor.capture());
    }

    @Test
    void updateBookingStatusByOwner_whenStatusAlreadyApproved_thenThrown() {
        Long userId = 10L;
        boolean approvedStatus = true;
        Booking booking = Booking.builder()
                .id(1L)
                .item(item)
                .status(StatusBooking.APPROVED)
                .build();
        when(repository.findById(booking.getId())).thenReturn(Optional.of(booking));

        Throwable exception = assertThrows(InvalidDataException.class,
                () -> bookingService.updateBookingStatusByOwner(booking.getId(), userId, approvedStatus));

        assertEquals("Status already: approved", exception.getMessage());
        verify(repository, never()).save(bookingArgumentCaptor.capture());
    }

    @Test
    void updateBookingStatusByOwner_whenStatusAlreadyRejected_thenThrown() {
        Long userId = 10L;
        boolean approvedStatus = false;
        Booking booking = Booking.builder()
                .id(1L)
                .item(item)
                .status(StatusBooking.REJECTED)
                .build();
        when(repository.findById(booking.getId())).thenReturn(Optional.of(booking));

        Throwable exception = assertThrows(InvalidDataException.class,
                () -> bookingService.updateBookingStatusByOwner(booking.getId(), userId, approvedStatus));

        assertEquals("Status already: rejected", exception.getMessage());
        verify(repository, never()).save(bookingArgumentCaptor.capture());
    }

    @Test
    void updateBookingStatusByOwner_whenNotOwnerTryingUpdateStatus_thenThrown() {
        Long userId = 1L;
        boolean approvedStatus = true;
        Booking booking = Booking.builder()
                .id(1L)
                .item(item)
                .status(StatusBooking.WAITING)
                .build();
        when(repository.findById(booking.getId())).thenReturn(Optional.of(booking));

        Throwable exception = assertThrows(UserNotFoundException.class,
                () -> bookingService.updateBookingStatusByOwner(booking.getId(), userId, approvedStatus));

        assertEquals("Only the owner of the item can confirm the booking.", exception.getMessage());
        verify(repository, never()).save(bookingArgumentCaptor.capture());
    }

    @Test
    void updateBookingStatusByOwner_whenOldBookingNotFound_thenThrown() {
        Long userId = 1L;
        boolean approvedStatus = true;
        Booking booking = Booking.builder()
                .id(1L)
                .item(item)
                .status(StatusBooking.WAITING)
                .build();
        when(repository.findById(booking.getId())).thenReturn(Optional.empty());

        Throwable exception = assertThrows(BookingNotFoundException.class,
                () -> bookingService.updateBookingStatusByOwner(booking.getId(), userId, approvedStatus));

        assertEquals("Booking not found", exception.getMessage());
        verify(repository, never()).save(bookingArgumentCaptor.capture());
    }

    @Test
    void getBookingDetails_whenAuthorCheckDetail_thenGetDetailsBooking() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .name("name")
                .email("email")
                .build();
        Booking booking = Booking.builder()
                .id(1L)
                .item(item)
                .status(StatusBooking.WAITING)
                .build();
        booking.setBooker(userBooker);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.findById(booking.getId())).thenReturn(Optional.of(booking));

        Booking actualBooking = bookingService.getBookingDetails(booking.getId(), userId);

        assertEquals(booking, actualBooking);
        verify(repository, times(1)).findById(booking.getId());
    }

    @Test
    void getBookingDetails_whenOwnerCheckDetail_thenGetDetailsBooking() {
        Long userId = 10L;
        User user = User.builder()
                .id(userId)
                .name("name")
                .email("email")
                .build();
        Booking booking = Booking.builder()
                .id(1L)
                .item(item)
                .status(StatusBooking.WAITING)
                .build();
        booking.setBooker(userBooker);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.findById(booking.getId())).thenReturn(Optional.of(booking));

        Booking actualBooking = bookingService.getBookingDetails(booking.getId(), userId);

        assertEquals(booking, actualBooking);
        verify(repository, times(1)).findById(booking.getId());
    }

    @Test
    void getBookingDetails_whenUserNotExists_thenThrown() {
        Long userId = 100L;
        Booking booking = Booking.builder()
                .id(1L)
                .item(item)
                .status(StatusBooking.WAITING)
                .build();
        booking.setBooker(userBooker);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Throwable exception = assertThrows(UserNotFoundException.class,
                () -> bookingService.getBookingDetails(booking.getId(), userId));

        assertEquals("User with id not found: 100", exception.getMessage());
        verify(repository, never()).findById(booking.getId());
    }

    @Test
    void getBookingDetails_whenUserNotAuthorOrOwner_thenThrown() {
        Long userId = 2L;
        User user = User.builder()
                .id(userId)
                .name("name")
                .email("email")
                .build();
        Booking booking = Booking.builder()
                .id(1L)
                .item(item)
                .status(StatusBooking.WAITING)
                .build();
        booking.setBooker(userBooker);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.findById(booking.getId())).thenReturn(Optional.of(booking));

        Throwable exception = assertThrows(UserNotFoundException.class,
                () -> bookingService.getBookingDetails(booking.getId(), userId));

        assertEquals("Only the author or owner can check booking details", exception.getMessage());
        verify(repository, times(1)).findById(booking.getId());
    }

    @Test
    void getAllBookingsByAuthor_whenStatusStateAllExistUser_thenGetListBookingWithSort() {
        Long userId = 10L;
        StatusState statusState = StatusState.ALL;
        User user = User.builder()
                .id(userId)
                .name("name")
                .email("email")
                .build();
        Booking secondBooking = Booking.builder()
                .id(2L)
                .item(item)
                .status(StatusBooking.WAITING)
                .build();
        secondBooking.setBooker(user);
        List<Booking> expectedListBooking = List.of(secondBooking);
        Page<Booking> page = new PageImpl<>(expectedListBooking);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.findAllByBooker_IdOrderByStartDateDesc(userId, PageRequest.of(0 / 20, 20)))
                .thenReturn(page);

        List<Booking> actualBookings = bookingService.getAllBookingsByAuthor(statusState, userId, 0, 20);

        assertEquals(1, actualBookings.size());
        assertEquals(page.getContent(), actualBookings);
    }

    @Test
    void getAllBookingsByAuthor_whenStatusStatePastExistUser_thenGetListBookingWithSort() {
        Long userId = 10L;
        StatusState statusState = StatusState.PAST;
        User user = User.builder()
                .id(userId)
                .name("name")
                .email("email")
                .build();
        Booking booking = Booking.builder()
                .id(2L)
                .item(item)
                .status(StatusBooking.APPROVED)
                .build();
        booking.setBooker(user);
        booking.setEndDate(currentDateTime.minusDays(1));
        List<Booking> expectedListBooking = List.of(booking);
        Page<Booking> page = new PageImpl<>(expectedListBooking);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.findAllByBooker_IdOrderByStartDateDesc(userId, PageRequest.of(0 / 20, 20)))
                .thenReturn(page);

        List<Booking> actualBookings = bookingService.getAllBookingsByAuthor(statusState, userId, 0, 20);

        assertEquals(1, actualBookings.size());
        assertEquals(page.getContent(), actualBookings);
        assertTrue(actualBookings.get(0).getEndDate().isBefore(currentDateTime));
    }

    @Test
    void getAllBookingsByAuthor_whenStatusStateFutureExistUser_thenGetListBookingWithSort() {
        Long userId = 10L;
        StatusState statusState = StatusState.FUTURE;
        User user = User.builder()
                .id(userId)
                .name("name")
                .email("email")
                .build();
        Booking booking = Booking.builder()
                .id(2L)
                .item(item)
                .status(StatusBooking.APPROVED)
                .build();
        booking.setBooker(user);
        booking.setStartDate(currentDateTime.plusDays(1));
        List<Booking> expectedListBooking = List.of(booking);
        Page<Booking> page = new PageImpl<>(expectedListBooking);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.findAllByBooker_IdOrderByStartDateDesc(userId, PageRequest.of(0 / 20, 20)))
                .thenReturn(page);

        List<Booking> actualBookings = bookingService.getAllBookingsByAuthor(statusState, userId, 0, 20);

        assertEquals(1, actualBookings.size());
        assertEquals(page.getContent(), actualBookings);
        assertTrue(actualBookings.get(0).getStartDate().isAfter(currentDateTime));
    }

    @Test
    void getAllBookingsByAuthor_whenStatusStateCurrentExistUser_thenGetListBookingWithSort() {
        Long userId = 10L;
        StatusState statusState = StatusState.CURRENT;
        User user = User.builder()
                .id(userId)
                .name("name")
                .email("email")
                .build();
        Booking booking = Booking.builder()
                .id(2L)
                .item(item)
                .status(StatusBooking.APPROVED)
                .build();
        booking.setBooker(user);
        booking.setStartDate(currentDateTime.minusDays(1));
        booking.setEndDate(currentDateTime.plusDays(1));
        Booking secondBooking = Booking.builder()
                .id(2L)
                .item(item)
                .status(StatusBooking.REJECTED)
                .build();
        secondBooking.setBooker(user);
        secondBooking.setStartDate(currentDateTime.minusDays(1));
        secondBooking.setEndDate(currentDateTime.plusDays(1));
        List<Booking> expectedListBooking = List.of(booking, secondBooking);
        Page<Booking> page = new PageImpl<>(expectedListBooking);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.findAllByBooker_IdOrderByStartDateDesc(userId, PageRequest.of(0 / 20, 20)))
                .thenReturn(page);

        List<Booking> actualBookings = bookingService.getAllBookingsByAuthor(statusState, userId, 0, 20);

        assertEquals(2, actualBookings.size());
        assertEquals(page.getContent(), actualBookings);
        assertTrue(actualBookings.get(0).getStartDate().isBefore(currentDateTime));
        assertTrue(actualBookings.get(0).getEndDate().isAfter(currentDateTime));
        assertEquals(StatusBooking.REJECTED, actualBookings.get(1).getStatus());
    }

    @Test
    void getAllBookingsByAuthor_whenStatusStateWaitingExistUser_thenGetListBookingWithSort() {
        Long userId = 10L;
        StatusState statusState = StatusState.WAITING;
        User user = User.builder()
                .id(userId)
                .name("name")
                .email("email")
                .build();
        Booking booking = Booking.builder()
                .id(2L)
                .item(item)
                .status(StatusBooking.WAITING)
                .build();
        booking.setBooker(user);
        booking.setStartDate(currentDateTime.minusDays(1));
        booking.setEndDate(currentDateTime.plusDays(1));
        List<Booking> expectedListBooking = List.of(booking);
        Page<Booking> page = new PageImpl<>(expectedListBooking);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.findAllByBooker_IdOrderByStartDateDesc(userId, PageRequest.of(0 / 20, 20)))
                .thenReturn(page);

        List<Booking> actualBookings = bookingService.getAllBookingsByAuthor(statusState, userId, 0, 20);

        assertEquals(1, actualBookings.size());
        assertEquals(page.getContent(), actualBookings);
        assertEquals(StatusBooking.WAITING, actualBookings.get(0).getStatus());
    }

    @Test
    void getAllBookingsByAuthor_whenStatusStateRejectedExistUser_thenGetListBookingWithSort() {
        Long userId = 10L;
        StatusState statusState = StatusState.REJECTED;
        User user = User.builder()
                .id(userId)
                .name("name")
                .email("email")
                .build();
        Booking booking = Booking.builder()
                .id(2L)
                .item(item)
                .status(StatusBooking.REJECTED)
                .build();
        booking.setBooker(user);
        booking.setStartDate(currentDateTime.minusDays(1));
        booking.setEndDate(currentDateTime.plusDays(1));
        List<Booking> expectedListBooking = List.of(booking);
        Page<Booking> page = new PageImpl<>(expectedListBooking);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.findAllByBooker_IdOrderByStartDateDesc(userId, PageRequest.of(0 / 20, 20)))
                .thenReturn(page);

        List<Booking> actualBookings = bookingService.getAllBookingsByAuthor(statusState, userId, 0, 20);

        assertEquals(1, actualBookings.size());
        assertEquals(page.getContent(), actualBookings);
        assertEquals(StatusBooking.REJECTED, actualBookings.get(0).getStatus());
    }


    @Test
    void getAllBookingsByAuthor_whenNotExistUser_thenThrown() {
        Long userId = 2L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Throwable exception = assertThrows(UserNotFoundException.class,
                () -> bookingService.getAllBookingsByAuthor(StatusState.ALL, userId, 0, 20));

        assertEquals("User with id not found: 2", exception.getMessage());
        verify(repository, never()).findAllByBooker_IdOrderByStartDateDesc(userId);
    }

    @Test
    void getAllBookingByOwner_whenNotExistUser_thenThrown() {
        long userId = 2L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Throwable exception = assertThrows(UserNotFoundException.class,
                () -> bookingService.getAllBookingByOwner(StatusState.ALL, userId, 0, 20));

        assertEquals("User with id not found: 2", exception.getMessage());
        verify(repository, never()).findAllByItem_Owner_IdOrderByStartDateDesc(userId);
    }

    @Test
    void getAllBookingByOwner_whenOwnerHasNoItem_thenThrown() {
        long userId = 2L;
        List<Booking> expectedListBooking = Collections.emptyList();
        Page<Booking> page = new PageImpl<>(expectedListBooking);
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(repository.findAllByItem_Owner_IdOrderByStartDateDesc(userId, PageRequest.of(0 / 20, 20)))
                .thenReturn(page);

        Throwable exception = assertThrows(ItemNotFoundException.class,
                () -> bookingService.getAllBookingByOwner(StatusState.ALL, userId, 0, 20));

        assertEquals("This user has no item", exception.getMessage());
    }

    @Test
    void getAllBookingByOwner_whenOwnerHasItem_thenReturnCorrectDataValueList() {
        Long userId = 10L;
        StatusState statusState = StatusState.ALL;
        User user = User.builder()
                .id(userId)
                .name("name")
                .email("email")
                .build();
        Booking secondBooking = Booking.builder()
                .id(2L)
                .item(item)
                .status(StatusBooking.WAITING)
                .build();
        secondBooking.setBooker(user);
        List<Booking> expectedListBooking = List.of(secondBooking);
        Page<Booking> page = new PageImpl<>(expectedListBooking);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.findAllByItem_Owner_IdOrderByStartDateDesc(userId, PageRequest.of(0 / 20, 20)))
                .thenReturn(page);

        List<Booking> actualBookings = bookingService.getAllBookingByOwner(statusState, userId, 0, 20);

        assertEquals(1, actualBookings.size());
        assertEquals(page.getContent(), actualBookings);
    }
}