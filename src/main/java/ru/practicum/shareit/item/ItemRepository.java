package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("SELECT i FROM Item i WHERE " + "LOWER(i.name) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " + "LOWER(i.description) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<Item> searchInNameOrDescription(@Param("searchText") String searchText);

    @EntityGraph(attributePaths = {"owner"})
    List<Item> findAllByOwner_Id(Long id);

    @EntityGraph(attributePaths = {"owner"})
    Optional<Item> findWithOwnerById(Long id);


}
