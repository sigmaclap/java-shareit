package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private static final String ERROR_MESSAGE_BOOKING_404 = "Booking not found";
    private static final String ERROR_MESSAGE_USER_WITH_ID_404 = "User with id not found: ";

    private final BookingRepository repository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public Booking createBookingRequest(Booking booking, Long userId) {
        Item item = itemRepository.findById(booking.getItem().getId())
                .orElseThrow(() -> new ItemNotFoundException("Item with id not found"));
        User userOwner = item.getOwner();
        if (Boolean.FALSE.equals(item.getAvailable())) {
            log.error("Item not available for booking");
            throw new InvalidDataException("Item not available for booking");
        }
        if (!userOwner.getId().equals(userId)) {
            return repository.save(booking);
        } else {
            log.error("Owner can't create request to create own thing ");
            throw new UserNotFoundException("Owner can't create request to create own thing");
        }
    }

    @Override
    public Booking updateBookingStatusByOwner(Long bookingId, Long userId, boolean approved) {
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(ERROR_MESSAGE_BOOKING_404));
        User userOwner = booking.getItem().getOwner();
        if (booking.getStatus().equals(StatusBooking.APPROVED) && approved) {
            log.info("Status already: approved");
            throw new InvalidDataException("Status already: approved");
        }
        if (booking.getStatus().equals(StatusBooking.REJECTED) && !approved) {
            log.info("Status already: rejected");
            throw new InvalidDataException("Status already: rejected");
        }
        if (userId.equals(userOwner.getId())) {
            if (approved) {
                booking.setStatus(StatusBooking.APPROVED);
            } else {
                booking.setStatus(StatusBooking.REJECTED);
            }
            return repository.save(booking);
        } else {
            log.error("Only the owner of the item can confirm the booking.");
            throw new UserNotFoundException("Only the owner of the item can confirm the booking.");
        }
    }

    @Override
    public Booking getBookingDetails(Long bookingId, Long userId) {
        validateExistsUser(userId);
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(ERROR_MESSAGE_BOOKING_404));
        User userOwner = booking.getItem().getOwner();
        User userAuthor = booking.getBooker();
        if (userId.equals(userAuthor.getId()) || userId.equals(userOwner.getId())) {
            return repository.findById(bookingId)
                    .orElseThrow(() -> new BookingNotFoundException(ERROR_MESSAGE_BOOKING_404));
        } else {
            log.error("Only the author or owner can check booking details");
            throw new UserNotFoundException("Only the author or owner can check booking details");
        }
    }

    @Override
    public List<Booking> getAllBookingsByAuthor(StatusState state, Long userId) {
        validateExistsUser(userId);
        return getAllBookingsWithStateParameter(state, repository.findAllByBooker_IdOrderByStartDateDesc(userId));
    }

    @Override
    public List<Booking> getAllBookingByOwner(StatusState state, Long userId) {
        validateExistsUser(userId);
        List<Booking> listBookingsByOwner = repository.findAllByItem_Owner_IdOrderByStartDateDesc(userId);
        if (listBookingsByOwner.isEmpty()) {
            log.error("This user has no item");
            throw new ItemNotFoundException("This user has no item");
        }
        return getAllBookingsWithStateParameter(state, listBookingsByOwner);
    }

    private List<Booking> getAllBookingsWithStateParameter(StatusState state, List<Booking> listBookingsByUser) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        switch (state) {
            case PAST:
                return getBookings(listBookingsByUser, booking -> booking.getEndDate().isBefore(currentDateTime) &&
                        booking.getStatus().equals(StatusBooking.APPROVED));
            case FUTURE:
                return getBookings(listBookingsByUser,
                        booking -> booking.getStartDate().isAfter(currentDateTime));
            case CURRENT:
                return getBookings(listBookingsByUser,
                        booking -> booking.getStartDate().isBefore(currentDateTime) &&
                                booking.getEndDate().isAfter(currentDateTime) &&
                                (booking.getStatus().equals(StatusBooking.APPROVED)
                                        || booking.getStatus().equals(StatusBooking.REJECTED)));
            case WAITING:
                return getBookings(listBookingsByUser, booking -> booking.getStatus().equals(StatusBooking.WAITING));
            case REJECTED:
                return getBookings(listBookingsByUser, booking -> booking.getStatus().equals(StatusBooking.REJECTED));
            default:
                return listBookingsByUser;
        }
    }

    private List<Booking> getBookings(List<Booking> listBookings,
                                      Predicate<Booking> predicate) {
        return listBookings.stream().filter(predicate).collect(Collectors.toList());
    }

    private void validateExistsUser(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            log.error(ERROR_MESSAGE_USER_WITH_ID_404 + userId);
            throw new UserNotFoundException(ERROR_MESSAGE_USER_WITH_ID_404 + userId);
        }
    }
}
