package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentStorage extends JpaRepository<Comment, Long> {
    List<Comment> findAllByItemId(long id);

    @Query("select c " +
            "from Comment as c " +
            "join fetch c.item as i " +
            "join i.user as u " +
            "where u.id = :id")
    List<Comment> findAllByItemOwnerId(@Param("id") long id);
}
