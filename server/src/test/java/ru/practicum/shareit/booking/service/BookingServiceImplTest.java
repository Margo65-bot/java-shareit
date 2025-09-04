package ru.practicum.shareit.booking.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingStateQueryParam;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ComponentScan(basePackages = "ru.practicum.shareit")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceImplTest {
    private final BookingServiceImpl bookingService;

    private final BookingStorage bookingStorage;

    private final ItemStorage itemStorage;

    private final UserStorage userStorage;

    private final EntityManager em;

    private User owner;
    private User booker1;
    private User booker2;
    private User stranger;
    private Item item1;
    private Item item2;
    private BookingCreateDto createDto1;
    private BookingCreateDto createDto2;

    @BeforeEach
    public void setUp() {
        userStorage.deleteAll();
        itemStorage.deleteAll();
        bookingStorage.deleteAll();

        owner = userStorage.save(new User(null, "Owner", "owner@email.com"));
        booker1 = userStorage.save(new User(null, "Booker1", "booker1@email.com"));
        booker2 = userStorage.save(new User(null, "Booker2", "booker2@email.com"));
        stranger = userStorage.save(new User(null, "stranger@email.com", "Stranger"));
        item1 = itemStorage.save(new Item(null, "Item", "Description", true, owner, null));
        item2 = itemStorage.save(new Item(null, "Item", "Description", false, owner, null));
        createDto1 = BookingCreateDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(item1.getId())
                .build();
        createDto2 = BookingCreateDto.builder()
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(4))
                .itemId(item1.getId())
                .build();
    }

    @Test
    void create_ShouldCreateBooking() {
        BookingResponseDto result = bookingService.create(booker1.getId(), createDto1);

        TypedQuery<Booking> query = em.createQuery("Select b from Booking b join fetch b.user join fetch b.item where b.id = :id", Booking.class);
        Booking booking = query.setParameter("id", result.getId())
                .getSingleResult();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingState.WAITING);
        assertThat(result.getItem().getId()).isEqualTo(item1.getId());
        assertThat(result.getBooker().getId()).isEqualTo(booker1.getId());

        assertThat(booking.getId()).isEqualTo(result.getId());
        assertThat(booking.getStatus()).isEqualTo(result.getStatus());
        assertThat(booking.getItem().getId()).isEqualTo(result.getItem().getId());
        assertThat(booking.getUser().getId()).isEqualTo(result.getBooker().getId());
    }

    @Test
    void create_WithUnavailableItem_ShouldThrowException() {
        createDto1.setItemId(item2.getId());
        assertThatThrownBy(() -> bookingService.create(booker1.getId(), createDto1))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessageContaining("не доступна для аренды");
    }

    @Test
    void getByIdForOwnerOrBooker_ShouldReturnBooking() {
        BookingResponseDto created = bookingService.create(booker1.getId(), createDto1);
        BookingResponseDto resultByBooker = bookingService.getByIdForOwnerOrBooker(booker1.getId(), created.getId());
        BookingResponseDto resultByOwner = bookingService.getByIdForOwnerOrBooker(owner.getId(), created.getId());

        assertThat(resultByBooker.getId()).isEqualTo(created.getId());
        assertThat(resultByOwner.getId()).isEqualTo(created.getId());
    }

    @Test
    void getByIdForOwnerOrBooker_WithUnauthorizedUser_ShouldThrowException() {
        BookingResponseDto created = bookingService.create(booker1.getId(), createDto1);

        assertThatThrownBy(() -> bookingService.getByIdForOwnerOrBooker(stranger.getId(), created.getId()))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessageContaining("не является владельцем или арендатором");
    }

    @Test
    void getAllForBooker_ShouldReturnBookings() {
        bookingService.create(booker1.getId(), createDto1);
        bookingService.create(booker1.getId(), createDto2);

        List<BookingResponseDto> result = bookingService.getAllForBooker(booker1.getId(), BookingStateQueryParam.ALL);

        assertThat(result).hasSize(2);

        for (BookingResponseDto booking : result) {
            assertThat(booking.getBooker().getId()).isEqualTo(booker1.getId());
        }
    }

    @Test
    void getAllForBooker_WithCurrentState_ShouldReturnCurrentBookings() {
        BookingCreateDto currentDto = BookingCreateDto.builder()
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(1))
                .itemId(item1.getId())
                .build();

        bookingService.create(booker1.getId(), currentDto);

        List<BookingResponseDto> result = bookingService.getAllForBooker(booker1.getId(), BookingStateQueryParam.CURRENT);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getBooker().getId()).isEqualTo(booker1.getId());
    }

    @Test
    void getAllForBooker_WithPastState_ShouldReturnPastBookings() {
        BookingCreateDto pastDto = BookingCreateDto.builder()
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .itemId(item1.getId())
                .build();

        bookingService.create(booker1.getId(), pastDto);

        List<BookingResponseDto> result = bookingService.getAllForBooker(booker1.getId(), BookingStateQueryParam.PAST);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getBooker().getId()).isEqualTo(booker1.getId());
    }

    @Test
    void getAllForBooker_WithFutureState_ShouldReturnFutureBookings() {
        bookingService.create(booker1.getId(), createDto1);

        List<BookingResponseDto> result = bookingService.getAllForBooker(booker1.getId(), BookingStateQueryParam.FUTURE);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getBooker().getId()).isEqualTo(booker1.getId());
    }

    @Test
    void getAllForBooker_WithWaitingState_ShouldReturnWaitingBookings() {
        bookingService.create(booker1.getId(), createDto1);

        List<BookingResponseDto> result = bookingService.getAllForBooker(booker1.getId(), BookingStateQueryParam.WAITING);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStatus()).isEqualTo(BookingState.WAITING);
    }

    @Test
    void getAllForBooker_WithRejectedState_ShouldReturnRejectedBookings() {
        BookingResponseDto created = bookingService.create(booker1.getId(), createDto1);
        bookingService.updateStateByOwner(owner.getId(), created.getId(), false);

        List<BookingResponseDto> result = bookingService.getAllForBooker(booker1.getId(), BookingStateQueryParam.REJECTED);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getStatus()).isEqualTo(BookingState.REJECTED);
    }

    @Test
    void getAllForBooker_WithNonExistingUser_ShouldThrowException() {
        assertThatThrownBy(() -> bookingService.getAllForBooker(999L, BookingStateQueryParam.ALL))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void getAllForBooker_WithNoBookings_ShouldReturnEmptyList() {
        List<BookingResponseDto> result = bookingService.getAllForBooker(booker1.getId(), BookingStateQueryParam.ALL);

        assertThat(result).isEmpty();
    }

    @Test
    void getAllForOwner_ShouldReturnBookings() {
        bookingService.create(booker1.getId(), createDto1);
        bookingService.create(booker2.getId(), createDto2);

        List<BookingResponseDto> result = bookingService.getAllForOwner(owner.getId(), BookingStateQueryParam.ALL);
        assertThat(result.size()).isEqualTo(2);

        TypedQuery<Long> query = em.createQuery("Select distinct(i.user.id) from Item i join i.user where i.id in :ids", Long.class);
        List<Long> ownerIds = query.setParameter("ids", List.of(result.getFirst().getItem().getId(), result.get(1).getItem().getId()))
                .getResultList();
        assertThat(ownerIds).hasSize(1);

        assertThat(ownerIds.getFirst()).isEqualTo(owner.getId());
    }

    @Test
    void getAllForOwner_WithCurrentState_ShouldReturnCurrentBookings() {
        BookingCreateDto currentDto = BookingCreateDto.builder()
                .start(LocalDateTime.now().minusHours(1))
                .end(LocalDateTime.now().plusHours(1))
                .itemId(item1.getId())
                .build();

        bookingService.create(booker1.getId(), currentDto);

        List<BookingResponseDto> result = bookingService.getAllForOwner(owner.getId(), BookingStateQueryParam.CURRENT);
        assertThat(result.size()).isEqualTo(1);

        TypedQuery<Long> query = em.createQuery("Select i.user.id from Item i join i.user where i.id = :id", Long.class);
        Long ownerId = query.setParameter("id", result.getFirst().getItem().getId())
                .getSingleResult();

        assertThat(ownerId).isEqualTo(owner.getId());
    }

    @Test
    void getAllForOwner_WithPastState_ShouldReturnPastBookings() {
        BookingCreateDto pastDto = BookingCreateDto.builder()
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .itemId(item1.getId())
                .build();

        bookingService.create(booker1.getId(), pastDto);

        List<BookingResponseDto> result = bookingService.getAllForOwner(owner.getId(), BookingStateQueryParam.PAST);
        assertThat(result.size()).isEqualTo(1);

        TypedQuery<Long> query = em.createQuery("Select i.user.id from Item i join i.user where i.id = :id", Long.class);
        Long ownerId = query.setParameter("id", result.getFirst().getItem().getId())
                .getSingleResult();

        assertThat(ownerId).isEqualTo(owner.getId());
    }

    @Test
    void getAllForOwner_WithFutureState_ShouldReturnFutureBookings() {
        bookingService.create(booker1.getId(), createDto1);

        List<BookingResponseDto> result = bookingService.getAllForOwner(owner.getId(), BookingStateQueryParam.FUTURE);
        assertThat(result.size()).isEqualTo(1);

        TypedQuery<Long> query = em.createQuery("Select i.user.id from Item i join i.user where i.id = :id", Long.class);
        Long ownerId = query.setParameter("id", result.getFirst().getItem().getId())
                .getSingleResult();

        assertThat(ownerId).isEqualTo(owner.getId());
    }

    @Test
    void getAllForOwner_WithWaitingState_ShouldReturnWaitingBookings() {
        bookingService.create(booker1.getId(), createDto1);

        List<BookingResponseDto> result = bookingService.getAllForOwner(owner.getId(), BookingStateQueryParam.WAITING);
        assertThat(result.size()).isEqualTo(1);

        assertThat(result.getFirst().getStatus()).isEqualTo(BookingState.WAITING);
    }

    @Test
    void getAllForOwner_WithRejectedState_ShouldReturnRejectedBookings() {
        BookingResponseDto created = bookingService.create(booker1.getId(), createDto1);
        bookingService.updateStateByOwner(owner.getId(), created.getId(), false);

        List<BookingResponseDto> result = bookingService.getAllForOwner(owner.getId(), BookingStateQueryParam.REJECTED);
        assertThat(result.size()).isEqualTo(1);

        assertThat(result.getFirst().getStatus()).isEqualTo(BookingState.REJECTED);
    }

    @Test
    void getAllForOwner_WithNonExistingUser_ShouldThrowException() {
        assertThatThrownBy(() -> bookingService.getAllForOwner(999L, BookingStateQueryParam.ALL))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void getAllForOwner_WithNoBookings_ShouldReturnEmptyList() {
        List<BookingResponseDto> result = bookingService.getAllForOwner(owner.getId(), BookingStateQueryParam.ALL);

        assertThat(result).isEmpty();
    }

    @Test
    void updateStateByOwner_ShouldApproveBooking() {
        BookingResponseDto created = bookingService.create(booker1.getId(), createDto1);

        BookingResponseDto result = bookingService.updateStateByOwner(owner.getId(), created.getId(), true);

        assertThat(result.getStatus()).isEqualTo(BookingState.APPROVED);
    }

    @Test
    void updateStateByOwner_ShouldRejectBooking() {
        BookingResponseDto created = bookingService.create(booker1.getId(), createDto1);

        BookingResponseDto result = bookingService.updateStateByOwner(owner.getId(), created.getId(), false);

        assertThat(result.getStatus()).isEqualTo(BookingState.REJECTED);
    }

    @Test
    void updateStateByOwner_WithAlreadyApprovedBooking_ShouldThrowException() {
        BookingResponseDto created = bookingService.create(booker1.getId(), createDto1);
        bookingService.updateStateByOwner(owner.getId(), created.getId(), true);

        assertThatThrownBy(() -> bookingService.updateStateByOwner(owner.getId(), created.getId(), false))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessageContaining("если статус равен WAITING");
    }

    @Test
    void updateStateByOwner_WithNonOwner_ShouldThrowException() {
        BookingResponseDto created = bookingService.create(booker1.getId(), createDto1);

        assertThatThrownBy(() -> bookingService.updateStateByOwner(stranger.getId(), created.getId(), true))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessageContaining("не является владельцем");
    }

    @Test
    void updateStateByOwner_WithNonExistingBooking_ShouldThrowException() {
        assertThatThrownBy(() -> bookingService.updateStateByOwner(owner.getId(), 999L, true))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найдена");
    }

    @Test
    void create_WithNonExistingItem_ShouldThrowException() {
        createDto1.setItemId(999L);
        assertThatThrownBy(() -> bookingService.create(booker1.getId(), createDto1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найдена");
    }

    @Test
    void create_WithNonExistingUser_ShouldThrowException() {
        assertThatThrownBy(() -> bookingService.create(999L, createDto1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void getByIdForOwnerOrBooker_WithNonExistingBooking_ShouldThrowException() {
        assertThatThrownBy(() -> bookingService.getByIdForOwnerOrBooker(stranger.getId(), 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найдена");
    }

    @Test
    void getByIdForOwnerOrBooker_WithApprovedBooking_ShouldReturnBooking() {
        BookingResponseDto created = bookingService.create(booker1.getId(), createDto1);
        bookingService.updateStateByOwner(owner.getId(), created.getId(), true);

        BookingResponseDto result = bookingService.getByIdForOwnerOrBooker(booker1.getId(), created.getId());

        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getStatus()).isEqualTo(BookingState.APPROVED);
    }

    @Test
    void create_WithOwnerBookingOwnItem_ShouldThrowException() {
        assertThatThrownBy(() -> bookingService.create(owner.getId(), createDto1))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessageContaining("не доступна");
    }

    @Test
    void create_WithExactSameTime_ShouldThrowException() {
        bookingService.create(booker1.getId(), createDto1);

        BookingCreateDto sameTimeDto = BookingCreateDto.builder()
                .start(createDto1.getStart())
                .end(createDto1.getEnd())
                .itemId(item1.getId())
                .build();

        assertThatThrownBy(() -> bookingService.create(booker2.getId(), sameTimeDto))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessageContaining("не должно пересекаться");
    }

    @Test
    void create_WithStartAtEndOfExisting_ShouldThrowException() {
        bookingService.create(booker1.getId(), createDto1);

        BookingCreateDto conflictDto = BookingCreateDto.builder()
                .start(createDto1.getEnd())
                .end(createDto1.getEnd().plusDays(1))
                .itemId(item1.getId())
                .build();

        assertThatThrownBy(() -> bookingService.create(booker2.getId(), conflictDto))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessageContaining("не должно пересекаться");
    }

    @Test
    void create_WithStartInsideExisting_ShouldThrowException() {
        bookingService.create(booker1.getId(), createDto1);

        BookingCreateDto conflictDto = BookingCreateDto.builder()
                .start(createDto1.getStart().plusHours(12)) // Внутри промежутка
                .end(createDto1.getEnd().plusDays(1))
                .itemId(item1.getId())
                .build();

        assertThatThrownBy(() -> bookingService.create(booker2.getId(), conflictDto))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessageContaining("не должно пересекаться");
    }

    @Test
    void create_WithEndInsideExisting_ShouldThrowException() {
        bookingService.create(booker1.getId(), createDto1);

        BookingCreateDto conflictDto = BookingCreateDto.builder()
                .start(createDto1.getStart().minusDays(1))
                .end(createDto1.getStart().plusHours(12)) // Заканчивается внутри
                .itemId(item1.getId())
                .build();

        assertThatThrownBy(() -> bookingService.create(booker2.getId(), conflictDto))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessageContaining("не должно пересекаться");
    }

    @Test
    void create_WithExistingInsideNew_ShouldThrowException() {
        bookingService.create(booker1.getId(), createDto1);

        BookingCreateDto conflictDto = BookingCreateDto.builder()
                .start(createDto1.getStart().minusDays(1))
                .end(createDto1.getEnd().plusDays(1)) // Полностью содержит первое
                .itemId(item1.getId())
                .build();

        assertThatThrownBy(() -> bookingService.create(booker2.getId(), conflictDto))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessageContaining("не должно пересекаться");
    }

    @Test
    void create_WithNoOverlap_ShouldSuccess() {
        bookingService.create(booker1.getId(), createDto1);

        BookingResponseDto result = bookingService.create(booker2.getId(), createDto2);
        assertThat(result).isNotNull();
    }

    @Test
    void create_WithRejectedStatus_ShouldNotCauseConflict() {
        BookingResponseDto rejected = bookingService.create(booker1.getId(), createDto1);
        bookingService.updateStateByOwner(owner.getId(), rejected.getId(), false);

        BookingCreateDto newBooking = BookingCreateDto.builder()
                .start(createDto1.getStart())
                .end(createDto1.getEnd())
                .itemId(item1.getId())
                .build();

        BookingResponseDto result = bookingService.create(booker2.getId(), newBooking);
        assertThat(result).isNotNull();
    }
}
