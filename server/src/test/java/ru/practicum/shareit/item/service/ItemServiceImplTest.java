package ru.practicum.shareit.item.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentStorage;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ComponentScan(basePackages = "ru.practicum.shareit")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImplTest {
    private final ItemServiceImpl itemService;
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final CommentStorage commentStorage;
    private final BookingStorage bookingStorage;
    private final ItemRequestStorage itemRequestStorage;
    private final EntityManager em;

    private User owner;
    private User booker;
    private User anotherUser;
    private Item item1;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        userStorage.deleteAll();
        itemStorage.deleteAll();
        commentStorage.deleteAll();
        bookingStorage.deleteAll();
        itemRequestStorage.deleteAll();

        owner = userStorage.save(new User(null, "Owner", "owner@email.com"));
        booker = userStorage.save(new User(null, "Booker", "booker@email.com"));
        anotherUser = userStorage.save(new User(null, "Another", "another@email.com"));

        request = itemRequestStorage.save(new ItemRequest(null, "Need item", LocalDateTime.now(), booker));

        item1 = itemStorage.save(new Item(null, "Item1", "Description1", true, owner, null));
        itemStorage.save(new Item(null, "Item2", "Description2", false, owner, request));
    }

    @Test
    void getById_ShouldReturnItem() {
        ItemDto result = itemService.getById(item1.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(item1.getId());
        assertThat(result.getName()).isEqualTo("Item1");
        assertThat(result.getDescription()).isEqualTo("Description1");
        assertThat(result.getAvailable()).isTrue();
    }

    @Test
    void getById_WithComments_ShouldReturnItemWithComments() {
        commentStorage.save(Comment.builder()
                .text("Great item!")
                .item(item1)
                .user(booker)
                .created(LocalDateTime.now())
                .build());

        ItemDto result = itemService.getById(item1.getId());

        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getComments().getFirst().getText()).isEqualTo("Great item!");
    }

    @Test
    void getById_WithNonExistingItem_ShouldThrowException() {
        assertThatThrownBy(() -> itemService.getById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найдена");
    }

    @Test
    void create_ShouldCreateItem() {
        ItemDto newItem = ItemDto.builder()
                .name("New Item")
                .description("New Description")
                .available(true)
                .build();

        ItemDto result = itemService.create(owner.getId(), newItem);

        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.id = :id", Item.class);
        Item item = query.setParameter("id", result.getId())
                .getSingleResult();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("New Item");
        assertThat(result.getDescription()).isEqualTo("New Description");
        assertThat(result.getAvailable()).isTrue();

        assertThat(item.getId()).isEqualTo(result.getId());
        assertThat(item.getName()).isEqualTo("New Item");
        assertThat(item.getDescription()).isEqualTo("New Description");
        assertThat(item.getAvailable()).isTrue();
    }

    @Test
    void create_WithRequest_ShouldCreateItemWithRequest() {
        ItemDto newItem = ItemDto.builder()
                .name("New Item")
                .description("New Description")
                .available(true)
                .requestId(request.getId())
                .build();

        ItemDto result = itemService.create(owner.getId(), newItem);
        assertThat(result).isNotNull();

        TypedQuery<Long> query = em.createQuery("Select i.request.id from Item i join i.request where i.id = :id", Long.class);
        Long requestId = query.setParameter("id", result.getId())
                .getSingleResult();
        assertThat(requestId).isEqualTo(request.getId());
    }

    @Test
    void create_WithEmptyName_ShouldThrowException() {
        ItemDto newItem = ItemDto.builder()
                .name(" ")
                .description("Description")
                .available(true)
                .build();

        assertThatThrownBy(() -> itemService.create(owner.getId(), newItem))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessageContaining("не должно быть пустым");
    }

    @Test
    void create_WithEmptyDescription_ShouldThrowException() {
        ItemDto newItem = ItemDto.builder()
                .name("Name")
                .description(" ")
                .available(true)
                .build();

        assertThatThrownBy(() -> itemService.create(owner.getId(), newItem))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessageContaining("не должно быть пустым");
    }

    @Test
    void create_WithNullAvailable_ShouldThrowException() {
        ItemDto newItem = ItemDto.builder()
                .name("Name")
                .description("Description")
                .available(null)
                .build();

        assertThatThrownBy(() -> itemService.create(owner.getId(), newItem))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessageContaining("не может быть пустым");
    }

    @Test
    void create_WithNonExistingRequest_ShouldThrowException() {
        ItemDto newItem = ItemDto.builder()
                .name("Name")
                .description("Description")
                .available(true)
                .requestId(999L)
                .build();

        assertThatThrownBy(() -> itemService.create(owner.getId(), newItem))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void update_ShouldUpdateItem() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Name")
                .description("Updated Description")
                .available(false)
                .build();

        ItemDto result = itemService.update(item1.getId(), owner.getId(), updateDto);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        assertThat(result.getAvailable()).isFalse();
    }

    @Test
    void update_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Name")
                .build();

        ItemDto result = itemService.update(item1.getId(), owner.getId(), updateDto);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getDescription()).isEqualTo("Description1"); // unchanged
        assertThat(result.getAvailable()).isTrue(); // unchanged
    }

    @Test
    void update_WithNonOwner_ShouldThrowException() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Name")
                .build();

        assertThatThrownBy(() -> itemService.update(item1.getId(), anotherUser.getId(), updateDto))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessageContaining("не является владельцем");
    }

    @Test
    void update_WithNonExistingItem_ShouldThrowException() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Name")
                .build();

        assertThatThrownBy(() -> itemService.update(999L, owner.getId(), updateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найдена");
    }

    @Test
    void getItemsByUserId_ShouldReturnUserItems() {
        List<ItemDto> result = itemService.getItemsByUserId(owner.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ItemDto::getName)
                .containsExactlyInAnyOrder("Item1", "Item2");
    }

    @Test
    void getItemsByUserId_WithBookings_ShouldReturnItemsWithBookings() {
        bookingStorage.save(Booking.builder()
                .item(item1)
                .user(booker)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .status(BookingState.APPROVED)
                .build());

        List<ItemDto> result = itemService.getItemsByUserId(owner.getId());

        assertThat(result).hasSize(2);
        ItemDto item1Dto = result.stream()
                .filter(item -> item.getId().equals(item1.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(item1Dto.getLastBooking()).isNotNull();
    }

    @Test
    void getItemsByUserId_WithComments_ShouldReturnItemsWithComments() {
        commentStorage.save(Comment.builder()
                .text("Comment")
                .item(item1)
                .user(booker)
                .created(LocalDateTime.now())
                .build());

        List<ItemDto> result = itemService.getItemsByUserId(owner.getId());

        assertThat(result).hasSize(2);
        ItemDto item1Dto = result.stream()
                .filter(item -> item.getId().equals(item1.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(item1Dto.getComments()).hasSize(1);
    }

    @Test
    void getItemsByUserId_WithNonExistingUser_ShouldThrowException() {
        assertThatThrownBy(() -> itemService.getItemsByUserId(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void searchItems_ShouldReturnMatchingItems() {
        List<ItemDto> result = itemService.searchItems("item1");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Item1");
    }

    @Test
    void searchItems_ByDescription_ShouldReturnMatchingItems() {
        List<ItemDto> result = itemService.searchItems("description1");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDescription()).isEqualTo("Description1");
    }

    @Test
    void searchItems_WithEmptyQuery_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.searchItems(" ");

        assertThat(result).isEmpty();
    }

    @Test
    void searchItems_OnlyAvailable_ShouldReturnOnlyAvailableItems() {
        List<ItemDto> result = itemService.searchItems("item");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Item1");
    }

    @Test
    void searchItems_WithNoMatches_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.searchItems("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void createComment_ShouldCreateComment() {
        bookingStorage.save(Booking.builder()
                .item(item1)
                .user(booker)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .status(BookingState.APPROVED)
                .build());

        CommentDto commentDto = CommentDto.builder()
                .text("Great item!")
                .build();

        CommentDto result = itemService.createComment(booker.getId(), item1.getId(), commentDto);

        TypedQuery<Comment> query = em.createQuery("Select c from Comment c join fetch c.user join fetch c.item where c.id = :id", Comment.class);
        Comment comment = query.setParameter("id", result.getId())
                .getSingleResult();

        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Great item!");
        assertThat(result.getAuthorName()).isEqualTo("Booker");
        assertThat(result.getItemId()).isEqualTo(item1.getId());

        assertThat(comment.getText()).isEqualTo("Great item!");
        assertThat(comment.getUser().getName()).isEqualTo("Booker");
        assertThat(comment.getItem().getId()).isEqualTo(item1.getId());
    }

    @Test
    void createComment_WithoutBooking_ShouldThrowException() {
        CommentDto commentDto = CommentDto.builder()
                .text("Great item!")
                .build();

        assertThatThrownBy(() -> itemService.createComment(booker.getId(), item1.getId(), commentDto))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessageContaining("не брал вещь");
    }

    @Test
    void createComment_WithFutureBooking_ShouldThrowException() {
        bookingStorage.save(Booking.builder()
                .item(item1)
                .user(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingState.APPROVED)
                .build());

        CommentDto commentDto = CommentDto.builder()
                .text("Great item!")
                .build();

        assertThatThrownBy(() -> itemService.createComment(booker.getId(), item1.getId(), commentDto))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessageContaining("не брал вещь");
    }

    @Test
    void createComment_WithNonExistingItem_ShouldThrowException() {
        CommentDto commentDto = CommentDto.builder()
                .text("Great item!")
                .build();

        assertThatThrownBy(() -> itemService.createComment(booker.getId(), 999L, commentDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найдена");
    }

    @Test
    void deleteById_ShouldDeleteItem() {
        itemService.deleteById(item1.getId());

        assertThat(itemStorage.findById(item1.getId())).isEmpty();
    }

    @Test
    void deleteById_WithNonExistingItem_ShouldNotThrowException() {
        assertThatNoException().isThrownBy(() -> itemService.deleteById(999L));
    }

    @Test
    void createComment_WithWaitingBooking_ShouldThrowException() {
        bookingStorage.save(Booking.builder()
                .item(item1)
                .user(booker)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .status(BookingState.WAITING)
                .build());

        CommentDto commentDto = CommentDto.builder()
                .text("Great item!")
                .build();

        assertThatThrownBy(() -> itemService.createComment(booker.getId(), item1.getId(), commentDto))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessageContaining("не брал вещь");
    }

    @Test
    void createComment_WithRejectedBooking_ShouldThrowException() {
        bookingStorage.save(Booking.builder()
                .item(item1)
                .user(booker)
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .status(BookingState.REJECTED)
                .build());

        CommentDto commentDto = CommentDto.builder()
                .text("Great item!")
                .build();

        assertThatThrownBy(() -> itemService.createComment(booker.getId(), item1.getId(), commentDto))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessageContaining("не брал вещь");
    }
}