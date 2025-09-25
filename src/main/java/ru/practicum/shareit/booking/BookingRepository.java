package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b WHERE b.id = :id AND b.item.owner.id = :itemOwnerId")
    Optional<Booking> findByIdAndItemOwnerId(@Param("id") Long id, @Param("itemOwnerId") Long itemOwnerId);

    List<Booking> findAllByBooker_IdAndEndBefore(Long id, LocalDateTime end, Sort sort);

    List<Booking> findAllByBooker_IdAndStartBeforeAndEndAfter(Long id, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findAllByBooker_IdAndStartAfter(Long id, LocalDateTime start, Sort sort);

    List<Booking> findAllByBooker_IdAndStatus(Long id, BookingStatus status, Sort sort);

    List<Booking> findAllByBooker_Id(Long id, Sort sort);

    List<Booking> findAllByItem_Owner_Id(Long id, Sort sort);

    List<Booking> findAllByItem_Owner_IdAndEndBefore(Long id, LocalDateTime end, Sort sort);

    List<Booking> findAllByItem_Owner_IdAndStartBeforeAndEndAfter(Long id, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findAllByItem_Owner_IdAndStartAfter(Long id, LocalDateTime start, Sort sort);

    List<Booking> findAllByItem_Owner_IdAndStatus(Long id, BookingStatus status, Sort sort);
}
