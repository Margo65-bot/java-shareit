package ru.practicum.shareit.request.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestStorage extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findByUserIdOrderByCreatedDesc(Long userId);

    @Query("select ir from ItemRequest as ir WHERE ir.user.id != :id ORDER BY ir.created DESC")
    List<ItemRequest> findAllByUserIdNotOrderByCreatedDesc(@Param("id") Long userId);
}
