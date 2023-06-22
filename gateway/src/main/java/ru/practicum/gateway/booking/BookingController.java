package ru.practicum.gateway.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.gateway.booking.dto.BookingDto;
import ru.practicum.gateway.booking.dto.StatusState;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private static final String REQUEST_HEADER_SHARER_USER_ID = "X-Sharer-User-Id";
	private final BookingClient bookingClient;

	@GetMapping
	public ResponseEntity<Object>
	getAllBookingsByAuthor(@RequestParam(defaultValue = "ALL") StatusState state,
						   @RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
						   @RequestParam(defaultValue = "0") @Min(0) Integer from,
						   @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer size) {
		log.info("Get booking with state {}, userId={}, from={}, size={}", state, userId, from, size);
		return bookingClient.getAllBookingsByAuthor(userId, state, from, size);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object>
	getAllBookingByOwner(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
						 @RequestParam(defaultValue = "ALL") StatusState state,
						 @RequestParam(defaultValue = "0") @Min(0) Integer from,
						 @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer size) {
		log.info("Get booking with state {}, userId={}, from={}, size={}", state, userId, from, size);
		return bookingClient.getAllBookingByOwner(userId, state, from, size);
	}

	@PostMapping
	public ResponseEntity<Object> createBookingRequest(@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
													   @Valid @RequestBody BookingDto bookingDto) {
		log.info("Creating booking {}, userId={}", bookingDto, userId);
		return bookingClient.createBookingRequest(userId, bookingDto);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBookingDetails(@PathVariable Long bookingId,
													@RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId) {
		log.info("Get booking {}, userId={}", bookingId, userId);
		return bookingClient.getBookingDetails(userId, bookingId);
	}


	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object>
			updateBookingStatusByOwner(@PathVariable Long bookingId,
									   @RequestHeader(REQUEST_HEADER_SHARER_USER_ID) Long userId,
									   @RequestParam boolean approved) {
		log.info("Update booking {}, userId={}, approved={}", bookingId, userId, approved);
		return bookingClient.updateBookingStatusByOwner(bookingId, userId, approved);
	}

}
