package ru.practicum.shareit.request.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ComponentScan(basePackages = "ru.practicum.shareit")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceImplTest {

    private final ItemRequestServiceImpl itemRequestService;
    private final ItemRequestStorage itemRequestStorage;
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final EntityManager em;

    private User user1;
    private User user2;
    private User user3;
    private ItemRequest request1;
    private ItemRequest request2;
    private ItemRequest request3;

    @BeforeEach
    void setUp() {
        userStorage.deleteAll();
        itemRequestStorage.deleteAll();
        itemStorage.deleteAll();

        user1 = userStorage.save(new User(null, "User1", "user1@email.com"));
        user2 = userStorage.save(new User(null, "User2", "user2@email.com"));
        user3 = userStorage.save(new User(null, "User3", "user3@email.com"));

        request1 = itemRequestStorage.save(new ItemRequest(null, "Need item 1", LocalDateTime.now().minusDays(3), user1));
        request2 = itemRequestStorage.save(new ItemRequest(null, "Need item 2", LocalDateTime.now().minusDays(2), user2));
        request3 = itemRequestStorage.save(new ItemRequest(null, "Need item 3", LocalDateTime.now().minusDays(1), user1));

        itemStorage.save(new Item(null, "Item for request 1", "Description", true, user2, request1));
        itemStorage.save(new Item(null, "Item for request 2", "Description", true, user3, request1));
    }

    @Test
    void getAllByUserId_ShouldReturnUserRequestsSortedByCreatedDesc() {
        List<ItemRequestDto> result = itemRequestService.getAllByUserId(user1.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ItemRequestDto::getDescription)
                .containsExactly("Need item 3", "Need item 1");
    }

    @Test
    void getAllByUserId_WithItems_ShouldReturnRequestsWithItems() {
        List<ItemRequestDto> result = itemRequestService.getAllByUserId(user1.getId());

        ItemRequestDto request1Dto = result.stream()
                .filter(r -> r.getId().equals(request1.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(request1Dto.getItems()).hasSize(2);
        assertThat(request1Dto.getItems()).extracting("name")
                .containsExactlyInAnyOrder("Item for request 1", "Item for request 2");
    }

    @Test
    void getAllByUserId_WithNoRequests_ShouldReturnEmptyList() {
        List<ItemRequestDto> result = itemRequestService.getAllByUserId(user3.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void getAllByUserId_WithNoItems_ShouldReturnRequestsWithoutItems() {
        List<ItemRequestDto> result = itemRequestService.getAllByUserId(user1.getId());

        ItemRequestDto request3Dto = result.stream()
                .filter(r -> r.getId().equals(request3.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(request3Dto.getItems()).isEmpty();
    }

    @Test
    void getAllByUserId_WithNonExistingUser_ShouldThrowException() {
        assertThatThrownBy(() -> itemRequestService.getAllByUserId(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с id 999 не найден");
    }

    @Test
    void getAllOther_ShouldReturnOtherUsersRequests() {
        List<ItemRequestDto> result = itemRequestService.getAllOther(user1.getId());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDescription()).isEqualTo("Need item 2");
    }

    @Test
    void getAllOther_ShouldReturnSortedByCreatedDesc() {
        itemRequestStorage.save(ItemRequest.builder()
                .description("Newer request")
                .created(LocalDateTime.now())
                .user(user2)
                .build());

        List<ItemRequestDto> result = itemRequestService.getAllOther(user1.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ItemRequestDto::getDescription)
                .containsExactly("Newer request", "Need item 2");
    }

    @Test
    void getAllOther_WithNoOtherRequests_ShouldReturnEmptyList() {
        itemRequestStorage.deleteAll();
        itemRequestStorage.save(ItemRequest.builder()
                .description("Only user1 request")
                .created(LocalDateTime.now())
                .user(user1)
                .build());

        List<ItemRequestDto> result = itemRequestService.getAllOther(user1.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void getAllOther_WithNonExistingUser_ShouldReturnAllRequests() {
        List<ItemRequestDto> result = itemRequestService.getAllOther(999L);

        assertThat(result).hasSize(3);
    }

    @Test
    void getById_WithItems_ShouldReturnRequestWithItems() {
        ItemRequestDto result = itemRequestService.getById(request1.getId());

        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems()).extracting("name")
                .containsExactlyInAnyOrder("Item for request 1", "Item for request 2");
    }

    @Test
    void getById_WithNoItems_ShouldReturnRequestWithoutItems() {
        ItemRequestDto result = itemRequestService.getById(request2.getId());

        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void getById_WithNonExistingRequest_ShouldThrowException() {
        assertThatThrownBy(() -> itemRequestService.getById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Запрос с id 999 не найден");
    }

    @Test
    void create_ShouldCreateRequest() {
        ItemRequestDto newRequest = ItemRequestDto.builder()
                .description("New request description")
                .build();

        ItemRequestDto result = itemRequestService.create(user3.getId(), newRequest);

        TypedQuery<ItemRequest> query = em.createQuery("Select ir from ItemRequest ir where ir.id = :id", ItemRequest.class);
        ItemRequest itemRequest = query.setParameter("id", result.getId())
                .getSingleResult();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("New request description");
        assertThat(result.getCreated()).isNotNull();

        assertThat(itemRequest.getDescription()).isEqualTo("New request description");
        assertThat(itemRequest.getId()).isEqualTo(result.getId());
    }

    @Test
    void create_ShouldSetCurrentDateTime() {
        LocalDateTime beforeCreate = LocalDateTime.now().minusSeconds(1);
        ItemRequestDto newRequest = ItemRequestDto.builder()
                .description("New request")
                .build();

        ItemRequestDto result = itemRequestService.create(user3.getId(), newRequest);

        assertThat(result.getCreated()).isAfter(beforeCreate);
        assertThat(result.getCreated()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    void create_WithNonExistingUser_ShouldThrowException() {
        ItemRequestDto newRequest = ItemRequestDto.builder()
                .description("New request")
                .build();

        assertThatThrownBy(() -> itemRequestService.create(999L, newRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void getAllByUserId_AfterDeletingAllRequests_ShouldReturnEmptyList() {
        itemRequestStorage.deleteAll();

        List<ItemRequestDto> result = itemRequestService.getAllByUserId(user1.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void getAllOther_AfterDeletingAllRequests_ShouldReturnEmptyList() {
        itemRequestStorage.deleteAll();

        List<ItemRequestDto> result = itemRequestService.getAllOther(user1.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void getById_WithDeletedRequest_ShouldThrowException() {
        ItemRequestDto newRequest = ItemRequestDto.builder()
                .description("To be deleted")
                .build();
        ItemRequestDto created = itemRequestService.create(user1.getId(), newRequest);

        itemRequestStorage.deleteById(created.getId());

        assertThatThrownBy(() -> itemRequestService.getById(created.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    void getAllByUserId_WithDeletedUser_ShouldThrowException() {
        User tempUser = userStorage.save(new User(null, "Temp", "temp@email.com"));
        itemRequestStorage.save(ItemRequest.builder()
                .description("Temp request")
                .created(LocalDateTime.now())
                .user(tempUser)
                .build());

        userStorage.deleteById(tempUser.getId());

        assertThatThrownBy(() -> itemRequestService.getAllByUserId(tempUser.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }
}