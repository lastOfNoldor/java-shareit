package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(attributePaths = {"author"})
    List<Comment> findAllByItem_Id(Long id);

    @Query("SELECT c FROM Comment c JOIN FETCH c.item WHERE c.item.id IN :itemIds")
    List<Comment> findAllByItemIdsIn(@Param("itemIds") List<Long> itemIds);
}
