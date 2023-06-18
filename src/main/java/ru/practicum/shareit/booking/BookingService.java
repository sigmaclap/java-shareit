package ru.practicum.shareit.booking;


import ru.practicum.shareit.booking.entity.Booking;
import ru.practicum.shareit.booking.statusEnum.StatusState;

import java.util.List;

public interface BookingService {
    Booking createBookingRequest(Booking booking, Long userId);

    Booking updateBookingStatusByOwner(Long bookingId, Long userId, boolean approved);

    Booking getBookingDetails(Long bookingId, Long userId);

    List<Booking> getAllBookingsByAuthor(StatusState state, Long userId, Integer limit, Integer size);

    List<Booking> getAllBookingByOwner(StatusState state, Long userId, Integer limit, Integer size);
}
