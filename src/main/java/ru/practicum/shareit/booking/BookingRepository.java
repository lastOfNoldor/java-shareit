package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b WHERE b.id = :id AND b.item.owner.id = :itemOwnerId")
    Optional<Booking> findByIdAndItemOwnerId(@Param("id") Long id, @Param("itemOwnerId") Long itemOwnerId);

    List<Booking> findAllByBookerId(Object unknownAttr1);

    List<Booking> findAllByItemOwnerId(Object unknownAttr1);
}
