package ru.practicum.shareit.booking.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.dto.BookingOwnerDto;
import ru.practicum.shareit.booking.entity.Booking;
import ru.practicum.shareit.booking.statusEnum.StatusBooking;
import ru.practicum.shareit.exceptions.ItemNotFoundException;
import ru.practicum.shareit.exceptions.UserNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.mapper.UserMapper;

@Component
@RequiredArgsConstructor
public class BookingMapper {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    public Booking toBooking(BookingDto bookingDto, Long userId) {
        return Booking.builder()
                .startDate(bookingDto.getStart())
                .endDate(bookingDto.getEnd())
                .item(itemRepository.findById(bookingDto.getItemId())
                        .orElseThrow(() -> new ItemNotFoundException("Item not found for create booking")))
                .booker(userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException("User not found for create booking")))
                .status(StatusBooking.WAITING)
                .build();
    }

    public BookingDtoResponse toBookingDtoResponse(Booking booking) {
        return BookingDtoResponse.builder()
                .id(booking.getId())
                .item(itemMapper.toItemDto(booking.getItem()))
                .start(booking.getStartDate())
                .end(booking.getEndDate())
                .status(booking.getStatus())
                .booker(userMapper.toBookerDto(booking.getBooker()))
                .build();
    }

    public BookingDtoResponse toUpdateBookingDtoResponse(Booking booking) {
        return BookingDtoResponse.builder()
                .id(booking.getId())
                .item(itemMapper.toItemDto(booking.getItem()))
                .start(booking.getStartDate())
                .end(booking.getEndDate())
                .status(booking.getStatus())
                .booker(userMapper.toBookerDto(booking.getBooker()))
                .build();
    }

    public BookingOwnerDto toBookingOwnerDto(Booking booking) {
        return BookingOwnerDto.builder()
                .id(booking.getId())
                .start(booking.getStartDate())
                .end(booking.getEndDate())
                .bookerId(booking.getBooker().getId())
                .build();
    }
}
