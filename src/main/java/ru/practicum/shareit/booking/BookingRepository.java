package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b WHERE b.id = :id AND b.item.owner.id = :itemOwnerId")
    Optional<Booking> findByIdAndItemOwnerId(@Param("id") Long id, @Param("itemOwnerId") Long itemOwnerId);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findAllByBooker_IdAndEndDateBefore(Long id, LocalDateTime endDate, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findAllByBooker_IdAndStartDateBeforeAndEndDateAfter(Long id, LocalDateTime startDate, LocalDateTime endDate, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findAllByBooker_IdAndStartDateAfter(Long id, LocalDateTime startDate, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findAllByBooker_IdAndStatus(Long id, BookingStatus status, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findAllByBooker_Id(Long id, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findAllByItem_Owner_Id(Long id, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findAllByItem_Owner_IdAndEndDateBefore(Long id, LocalDateTime endDate, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findAllByItem_Owner_IdAndStartDateBeforeAndEndDateAfter(Long id, LocalDateTime startDate, LocalDateTime endDate, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findAllByItem_Owner_IdAndStartDateAfter(Long id, LocalDateTime startDate, Sort sort);

    @EntityGraph(attributePaths = {"item", "booker"})
    List<Booking> findAllByItem_Owner_IdAndStatus(Long id, BookingStatus status, Sort sort);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.item i LEFT JOIN FETCH b.booker LEFT JOIN FETCH i.owner WHERE b.id = :bookingId")
    Optional<Booking> findWithBookerAndOwnerById(@Param("bookingId") Long bookingId);

    void deleteByBooker_Id(Long id);

    void deleteByItem_Id(Long id);

    Optional<Booking> findByItem_Id(Long id);

    @EntityGraph(attributePaths = {"booker"})
    Optional<Booking> findByBooker_IdAndItem_IdAndEndDateBefore(Long id, Long id1, LocalDateTime endDate);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId " + "AND b.endDate < :now AND b.status = ru.practicum.shareit.booking.BookingStatus.APPROVED " + "ORDER BY b.endDate DESC LIMIT 1")
    Optional<Booking> findLastBooking(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId " + "AND b.startDate > :now AND b.status = ru.practicum.shareit.booking.BookingStatus.APPROVED " + "ORDER BY b.startDate ASC LIMIT 1")
    Optional<Booking> findNextBooking(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

}

