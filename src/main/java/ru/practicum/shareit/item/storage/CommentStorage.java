package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentStorage extends JpaRepository<Comment, Long> {
    @Query("select c " +
            "from Comment as c " +
            "join fetch c.user " +
            "join fetch c.item " +
            "where c.item.id = :id")
    List<Comment> findAllByItemIdWithUserAndItem(@Param("id") long id);
}
