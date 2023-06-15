package ru.practicum.shareit.booking;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.entity.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBooker_IdOrderByStartDateDesc(Long userId);

    Page<Booking> findAllByBooker_IdOrderByStartDateDesc(Long userId, Pageable pageable);

    @Query(value = "SELECT * FROM bookings b \n" +
            "WHERE b.booker_id = ?1\n" +
            "ORDER BY b.start_date DESC \n" +
            "OFFSET ?2 ROWS", nativeQuery = true)
    Page<Booking> findAllByBooker_IdOrderByStartDateDesc(Long userId, Integer limit, Pageable pageable);

    List<Booking> findAllByItem_Owner_IdOrderByStartDateDesc(Long userId);

    Page<Booking> findAllByItem_Owner_IdOrderByStartDateDesc(Long userId, Pageable pageable);

    @Query(value = "SELECT * FROM bookings b \n" +
            "JOIN items i ON i.id = b.item_id \n" +
            "WHERE i.owner_id = ?1\n" +
            "ORDER BY b.start_date DESC \n" +
            "OFFSET ?2 ROWS", nativeQuery = true)
    Page<Booking> findAllByItem_Owner_IdOrderByStartDateDesc(Long userId, Integer page, Pageable pageable);

    @Query(value = "SELECT * FROM bookings b \n" +
            "WHERE b.booker_id = ?1\n" +
            "ORDER BY id ASC", nativeQuery = true)
    List<Booking> findBookingsByBooker_IdOrderByIdAsc(Long userId);

    @Query(value = "SELECT * FROM bookings b \n" +
            "WHERE b.item_id = ?1 AND b.start_date <= CAST (?2 AS timestamp)\n" +
            "ORDER BY end_date DESC limit 1", nativeQuery = true)
    Optional<Booking> findFirstByItem_IdAndStartDateBeforeOrderByEndDateDesc(Long itemId, LocalDateTime beforeDate);

    @Query(value = "SELECT * FROM bookings b \n" +
            "WHERE b.item_id = ?1 AND b.start_date >= CAST (?2 AS timestamp)\n" +
            "ORDER BY end_date ASC limit 1", nativeQuery = true)
    Optional<Booking> findFirstByItem_IdAndStartDateAfterOrderByEndDateAsc(Long itemId, LocalDateTime afterDate);
}
