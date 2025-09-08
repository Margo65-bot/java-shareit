package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.RespondingItem;

import java.util.List;
import java.util.Optional;

public interface ItemStorage extends JpaRepository<Item, Long> {
    @Query("select i " +
            "from Item as i " +
            "where (" +
            "lower(i.name) like lower(concat('%', ?1, '%')) or " +
            "lower(i.description) like lower(concat('%', ?1, '%'))" +
            ") and i.available")
    List<Item> findByQuery(String query);

    List<Item> findAllByUserId(long id);

    @Query("select i " +
            "from Item as i " +
            "JOIN FETCH i.user " +
            "where i.id = :id")
    Optional<Item> findByIdWithUser(@Param("id") long id);

    @Query("select new ru.practicum.shareit.request.dto.RespondingItem(i.id, i.name, i.user.id, i.request.id) " +
            "from Item as i " +
            "join i.user " +
            "where i.request.id in :ids")
    List<RespondingItem> findAllByRequestIdIn(@Param("ids") List<Long> requestIds);

    @Query("select new ru.practicum.shareit.request.dto.RespondingItem(i.id, i.name, i.user.id, i.request.id) " +
            "from Item as i " +
            "join i.user " +
            "where i.request.id = :id")
    List<RespondingItem> findAllByRequestId(@Param("id") long id);
}
