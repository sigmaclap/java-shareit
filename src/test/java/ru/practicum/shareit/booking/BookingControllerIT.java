package ru.practicum.shareit.booking;

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
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.entity.Booking;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.statusEnum.StatusBooking;
import ru.practicum.shareit.booking.statusEnum.StatusState;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@SpringJUnitWebConfig(BookingController.class)
class BookingControllerIT {
    private static final String REQUEST_HEADER_SHARER_USER_ID = "X-Sharer-User-Id";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private BookingMapper mapper;
    @MockBean
    private BookingService service;

    private BookingDtoResponse bookingDtoResponse;
    private Booking booking;
    private BookingDto bookingDto;
    private Long userId;

    @BeforeEach
    void setUp(WebApplicationContext wac) {
        userId = 1L;
        mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .build();
        bookingDtoResponse = BookingDtoResponse.builder()
                .status(StatusBooking.WAITING)
                .build();
        booking = new Booking();
        booking.setId(1L);
        bookingDto = BookingDto.builder()
                .start(LocalDateTime.now().plusMinutes(1))
                .end(LocalDateTime.now().plusDays(1))
                .build();
    }

    @SneakyThrows
    @Test
    void createBookingRequest_whenValidDataDtoRequest_thenCreateBooking() {
        when(mapper.toBooking(bookingDto, userId)).thenReturn(booking);
        when(service.createBookingRequest(booking, userId)).thenReturn(booking);
        when(mapper.toBookingDtoResponse(booking)).thenReturn(bookingDtoResponse);

        String result = mockMvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(bookingDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER_SHARER_USER_ID, userId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(bookingDtoResponse), result);
        verify(service).createBookingRequest(booking, userId);
    }

    @SneakyThrows
    @Test
    void createBookingRequest_whenNotValidDataDtoRequest_thenReturnedThrows() {
        BookingDto notValidDto = new BookingDto();
        notValidDto.setStart(LocalDateTime.now().minusDays(10));
        notValidDto.setEnd(LocalDateTime.now().minusDays(1));

        Object result = Objects.requireNonNull(mockMvc.perform(post("/bookings")
                                .content(objectMapper.writeValueAsString(notValidDto))
                                .characterEncoding(StandardCharsets.UTF_8)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(REQUEST_HEADER_SHARER_USER_ID, userId))
                        .andDo(print())
                        .andExpect(status().is(400))
                        .andReturn()
                        .getResolvedException())
                .getClass();

        assertEquals(MethodArgumentNotValidException.class, result);
        verify(service, never()).createBookingRequest(booking, userId);
    }

    @SneakyThrows
    @Test
    void updateBookingStatusByOwner() {
        boolean approved = true;
        long bookingId = booking.getId();
        bookingDtoResponse.setStatus(StatusBooking.APPROVED);
        when(service.updateBookingStatusByOwner(bookingId, userId, approved))
                .thenReturn(booking);
        when(mapper.toUpdateBookingDtoResponse(booking)).thenReturn(bookingDtoResponse);

        String result = mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER_SHARER_USER_ID, userId)
                        .param("approved", Boolean.TRUE.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(bookingDtoResponse), result);
        verify(service).updateBookingStatusByOwner(bookingId, userId, approved);
    }

    @SneakyThrows
    @Test
    void getBookingDetails() {
        long bookingId = booking.getId();
        when(service.getBookingDetails(bookingId, userId)).thenReturn(booking);
        when(mapper.toBookingDtoResponse(booking)).thenReturn(bookingDtoResponse);

        String result = mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER_SHARER_USER_ID, userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(bookingDtoResponse), result);
        verify(service).getBookingDetails(bookingId, userId);
    }

    @SneakyThrows
    @Test
    void getAllBookingsByAuthor() {
        StatusState state = StatusState.REJECTED;
        List<Booking> bookings = List.of(booking);
        List<BookingDtoResponse> expectedBookings = List.of(bookingDtoResponse);

        when(service.getAllBookingsByAuthor(state, userId, 0, 20))
                .thenReturn(bookings);
        when(mapper.toBookingDtoResponse(bookings.get(0))).thenReturn(bookingDtoResponse);

        String result = mockMvc.perform(get("/bookings")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER_SHARER_USER_ID, userId)
                        .param("from", "0")
                        .param("size", "20")
                        .param("state", state.toString()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(expectedBookings), result);
        verify(service).getAllBookingsByAuthor(state, userId, 0, 20);
    }

    @SneakyThrows
    @Test
    void getAllBookingByOwner() {
        StatusState state = StatusState.REJECTED;
        List<Booking> bookings = List.of(booking);
        List<BookingDtoResponse> expectedBookings = List.of(bookingDtoResponse);

        when(service.getAllBookingByOwner(state, userId, 0, 20))
                .thenReturn(bookings);
        when(mapper.toBookingDtoResponse(bookings.get(0))).thenReturn(bookingDtoResponse);

        String result = mockMvc.perform(get("/bookings/owner")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(REQUEST_HEADER_SHARER_USER_ID, userId)
                        .param("from", "0")
                        .param("size", "20")
                        .param("state", state.toString()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(expectedBookings), result);
        verify(service).getAllBookingByOwner(state, userId, 0, 20);
    }
}