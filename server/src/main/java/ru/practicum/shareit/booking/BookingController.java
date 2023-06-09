package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.entity.Booking;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.statusEnum.StatusState;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingMapper mapper;
    private final BookingService service;
    private static final String REQUEST_HEADER_SHARER_USER_ID = "X-Sharer-User-Id";


    @PostMapping
    public BookingDtoResponse createBookingRequest(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                                                   @RequestBody BookingDto bookingDto) {
        Booking booking = mapper.toBooking(bookingDto, userId);
        return mapper.toBookingDtoResponse(service.createBookingRequest(booking, userId));
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoResponse updateBookingStatusByOwner(@PathVariable Long bookingId,
                                                         @RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                                                         @RequestParam boolean approved) {
        return mapper.toUpdateBookingDtoResponse(service.updateBookingStatusByOwner(bookingId, userId, approved));
    }

    @GetMapping("/{bookingId}")
    public BookingDtoResponse getBookingDetails(@PathVariable Long bookingId,
                                                @RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId) {
        return mapper.toBookingDtoResponse(service.getBookingDetails(bookingId, userId));
    }

    @GetMapping
    public List<BookingDtoResponse>
    getAllBookingsByAuthor(@RequestParam(defaultValue = "ALL") StatusState state,
                           @RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                           @RequestParam(name = "from", defaultValue = "0") Integer limit,
                           @RequestParam(defaultValue = "20") Integer size) {
        return service.getAllBookingsByAuthor(state, userId, limit, size).stream()
                .map(mapper::toBookingDtoResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public List<BookingDtoResponse>
    getAllBookingByOwner(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
                         @RequestParam(defaultValue = "ALL") StatusState state,
                         @RequestParam(name = "from", defaultValue = "0") Integer limit,
                         @RequestParam(defaultValue = "20") Integer size) {
        return service.getAllBookingByOwner(state, userId, limit, size).stream()
                .map(mapper::toBookingDtoResponse)
                .collect(Collectors.toList());
    }
}