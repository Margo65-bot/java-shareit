package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
import java.util.Optional;

public interface BookingStorage extends JpaRepository<Booking, Long> {
    @Query("select b " +
            "from Booking as b " +
            "join fetch b.user " +
            "join fetch b.item as i " +
            "join fetch i.user " +
            "where b.id = :id")
    Optional<Booking> findByIdWithUserAndItemWithOwner(@Param("id") long id);

    @Query("select count(b) > 0 " +
            "from Booking as b " +
            "where b.item.id = :itemId " +
            "and b.user.id = :userId " +
            "and b.status = 'APPROVED' " +
            "and b.end < current_timestamp")
    boolean existsApprovedPastBooking(@Param("itemId") long itemId, @Param("userId") long userId);

    @Query("select b " +
            "from Booking as b " +
            "join fetch b.user " +
            "join fetch b.item " +
            "where b.user.id = :id " +
            "order by b.start desc")
    List<Booking> findAllByUserIdOrderByStartDesc(@Param("id") long id);

    @Query("select b " +
            "from Booking as b " +
            "join fetch b.user " +
            "join fetch b.item " +
            "where b.user.id = :id " +
            "and b.start <= current_timestamp " +
            "and b.end >= current_timestamp " +
            "order by b.start desc")
    List<Booking> findCurrentByUserIdOrderByStartDesc(@Param("id") long id);

    @Query("select b " +
            "from Booking as b " +
            "join fetch b.user " +
            "join fetch b.item " +
            "where b.user.id = :id " +
            "and b.end < current_timestamp " +
            "order by b.start desc")
    List<Booking> findPastByUserIdOrderByStartDesc(@Param("id") long id);

    @Query("select b " +
            "from Booking as b " +
            "join fetch b.user " +
            "join fetch b.item " +
            "where b.user.id = :id " +
            "and b.start > current_timestamp " +
            "order by b.start desc")
    List<Booking> findFutureByUserIdOrderByStartDesc(@Param("id") long id);

    @Query("select b " +
            "from Booking as b " +
            "join fetch b.user " +
            "join fetch b.item " +
            "where b.user.id = :id " +
            "and b.status = 'WAITING' " +
            "order by b.start desc")
    List<Booking> findWaitingByUserIdOrderByStartDesc(@Param("id") long id);

    @Query("select b " +
            "from Booking as b " +
            "join fetch b.user " +
            "join fetch b.item " +
            "where b.user.id = :id " +
            "and b.status = 'REJECTED' " +
            "order by b.start desc")
    List<Booking> findRejectedByUserIdOrderByStartDesc(@Param("id") long id);

    @Query("select b " +
            "from Booking as b " +
            "join fetch b.user " +
            "join fetch b.item as i " +
            "join fetch i.user " +
            "where i.user.id = :id " +
            "and b.start <= current_timestamp " +
            "and b.end >= current_timestamp " +
            "order by b.start desc")
    List<Booking> findAllByItemOwnerIdOrderByStartDesc(@Param("id") long id);

    @Query("select b " +
            "from Booking as b " +
            "join fetch b.user " +
            "join fetch b.item as i " +
            "join fetch i.user " +
            "where i.user.id = :id " +
            "order by b.start desc")
    List<Booking> findCurrentByItemOwnerIdOrderByStartDesc(@Param("id") long id);

    @Query("select b " +
            "from Booking as b " +
            "join fetch b.user " +
            "join fetch b.item as i " +
            "join fetch i.user " +
            "where i.user.id = :id " +
            "and b.end < current_timestamp " +
            "order by b.start desc")
    List<Booking> findPastByItemOwnerIdOrderByStartDesc(@Param("id") long id);

    @Query("select b " +
            "from Booking as b " +
            "join fetch b.user " +
            "join fetch b.item as i " +
            "join fetch i.user " +
            "where i.user.id = :id " +
            "and b.start > current_timestamp " +
            "order by b.start desc")
    List<Booking> findFutureByItemOwnerIdOrderByStartDesc(@Param("id") long id);

    @Query("select b " +
            "from Booking as b " +
            "join fetch b.user " +
            "join fetch b.item as i " +
            "join fetch i.user " +
            "where i.user.id = :id " +
            "and b.status = 'WAITING' " +
            "order by b.start desc")
    List<Booking> findWaitingByItemOwnerIdOrderByStartDesc(@Param("id") long id);

    @Query("select b " +
            "from Booking as b " +
            "join fetch b.user " +
            "join fetch b.item as i " +
            "join fetch i.user " +
            "where i.user.id = :id " +
            "and b.status > 'REJECTED' " +
            "order by b.start desc")
    List<Booking> findRejectedByItemOwnerIdOrderByStartDesc(@Param("id") long id);

    @Query("select new ru.practicum.shareit.item.dto.ItemDto$BookingInfo(" +
            "b.item.id, b.start, b.end) " +
            "from Booking b " +
            "join b.item " +
            "where b.item.id in :itemIds " +
            "and b.status = 'APPROVED' " +
            "and b.end < current_timestamp " +
            "order by b.end desc " +
            "limit 1")
    List<ItemDto.BookingInfo> findAllLastBookingByItemIds(@Param("itemIds") List<Long> itemIds);

    @Query("select new ru.practicum.shareit.item.dto.ItemDto$BookingInfo(" +
            "b.item.id, b.start, b.end) " +
            "from Booking b " +
            "join b.item " +
            "where b.item.id in :itemIds " +
            "and b.status = 'APPROVED' " +
            "and b.start > current_timestamp " +
            "order by b.start asc " +
            "limit 1")
    List<ItemDto.BookingInfo> findAllNextBookingByItemIds(@Param("itemIds") List<Long> itemIds);
}
