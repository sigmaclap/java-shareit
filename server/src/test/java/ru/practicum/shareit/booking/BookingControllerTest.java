package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.entity.Booking;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.statusEnum.StatusBooking;
import ru.practicum.shareit.booking.statusEnum.StatusState;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@SpringJUnitWebConfig(BookingController.class)
class BookingControllerTest {
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
    void setUp() {
        userId = 1L;
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
    void createBookingRequest_whenValidDataDtoRequest_thenCreateBookingStatusCode200() {
        when(mapper.toBooking(bookingDto, userId)).thenReturn(booking);
        when(service.createBookingRequest(booking, userId)).thenReturn(booking);
        when(mapper.toBookingDtoResponse(booking)).thenReturn(bookingDtoResponse);

        String result = mockMvcPerformValidData(performPost(bookingDto));

        checkEqualsResult(bookingDtoResponse, result);
        verify(service).createBookingRequest(booking, userId);
    }


    @SneakyThrows
    @Test
    void updateBookingStatusByOwner_whenOkData_thenReturnedUpdateStatusCode200() {
        boolean approved = true;
        long bookingId = booking.getId();
        bookingDtoResponse.setStatus(StatusBooking.APPROVED);
        when(service.updateBookingStatusByOwner(bookingId, userId, approved))
                .thenReturn(booking);
        when(mapper.toUpdateBookingDtoResponse(booking)).thenReturn(bookingDtoResponse);

        String result = mockMvcPerformValidData(performPatch(bookingId, approved));

        checkEqualsResult(bookingDtoResponse, result);
        verify(service).updateBookingStatusByOwner(bookingId, userId, approved);
    }

    @SneakyThrows
    @Test
    void shouldReturnOkWhenGetAllBookingDetailsStatusCode200() {
        long bookingId = booking.getId();
        when(service.getBookingDetails(bookingId, userId)).thenReturn(booking);
        when(mapper.toBookingDtoResponse(booking)).thenReturn(bookingDtoResponse);

        String result = mockMvcPerformValidData(performGet("/bookings/{bookingId}", bookingId));


        checkEqualsResult(bookingDtoResponse, result);
        verify(service).getBookingDetails(bookingId, userId);
    }

    @SneakyThrows
    @Test
    void shouldReturnOkWhenGetAllBookingByAuthorStatusCode200() {
        StatusState state = StatusState.REJECTED;
        List<Booking> bookings = List.of(booking);
        List<BookingDtoResponse> expectedBookings = List.of(bookingDtoResponse);

        when(service.getAllBookingsByAuthor(state, userId, 0, 20))
                .thenReturn(bookings);
        when(mapper.toBookingDtoResponse(bookings.get(0))).thenReturn(bookingDtoResponse);

        String result = mockMvcPerformValidData(performGetWithParams("/bookings", state));

        checkEqualsResult(expectedBookings, result);
        verify(service).getAllBookingsByAuthor(state, userId, 0, 20);
    }

    @SneakyThrows
    @Test
    void shouldReturnOkWhenGetAllBookingByOwnerStatusCode200() {
        StatusState state = StatusState.REJECTED;
        List<Booking> bookings = List.of(booking);
        List<BookingDtoResponse> expectedBookings = List.of(bookingDtoResponse);

        when(service.getAllBookingByOwner(state, userId, 0, 20))
                .thenReturn(bookings);
        when(mapper.toBookingDtoResponse(bookings.get(0))).thenReturn(bookingDtoResponse);

        String result = mockMvcPerformValidData(performGetWithParams("/bookings/owner", state));

        checkEqualsResult(expectedBookings, result);
        verify(service).getAllBookingByOwner(state, userId, 0, 20);
    }

    private String mockMvcPerformValidData(ResultActions perform) throws Exception {
        return perform
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private ResultActions performGetWithParams(String url, StatusState state) throws Exception {
        return mockMvc.perform(get(url)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(REQUEST_HEADER_SHARER_USER_ID, userId)
                .param("from", "0")
                .param("size", "20")
                .param("state", state.toString()));
    }

    private ResultActions performPatch(long bookingId, Boolean approved) throws Exception {
        return mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(REQUEST_HEADER_SHARER_USER_ID, userId)
                .param("approved", approved.toString()));
    }

    private ResultActions performPost(Object content) throws Exception {
        return mockMvc.perform(post("/bookings")
                .content(objectMapper.writeValueAsString(content))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(REQUEST_HEADER_SHARER_USER_ID, userId));
    }

    private ResultActions performGet(String url, Long bookingId) throws Exception {
        return mockMvc.perform(get(url, bookingId)
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(REQUEST_HEADER_SHARER_USER_ID, userId)
        );
    }

    private void checkEqualsResult(Object expected, String result) throws JsonProcessingException {
        assertEquals(objectMapper.writeValueAsString(expected), result);
    }
}