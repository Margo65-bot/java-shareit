package ru.practicum.shareit.user.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UserEmailConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ComponentScan(basePackages = "ru.practicum.shareit")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImplTest {

    private final UserServiceImpl userService;
    private final UserStorage userStorage;
    private final EntityManager em;

    private Long existingUserId;

    @BeforeEach
    void setUp() {
        userStorage.deleteAll();

        User existingUser = userStorage.save(new User(null, "Existing User", "existing@email.com"));
        userStorage.save(new User(null, "Another User", "another@email.com"));

        existingUserId = existingUser.getId();
    }

    @Test
    void getAll_WhenUsersExist_ShouldReturnAllUsers() {
        List<UserDto> result = userService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserDto::getEmail)
                .containsExactlyInAnyOrder("existing@email.com", "another@email.com");
    }

    @Test
    void getAll_WhenNoUsers_ShouldReturnEmptyList() {
        userStorage.deleteAll();

        List<UserDto> result = userService.getAll();

        assertThat(result).isEmpty();
    }

    @Test
    void getById_WithExistingId_ShouldReturnUser() {
        UserDto result = userService.getById(existingUserId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingUserId);
        assertThat(result.getName()).isEqualTo("Existing User");
        assertThat(result.getEmail()).isEqualTo("existing@email.com");
    }

    @Test
    void getById_WithNonExistingId_ShouldThrowNotFoundException() {
        assertThatThrownBy(() -> userService.getById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь с id " + 999L + " не найден");
    }

    @Test
    void deleteById_WithExistingId_ShouldDeleteUser() {
        userService.deleteById(existingUserId);

        assertThat(userStorage.existsById(existingUserId)).isFalse();
        assertThat(userStorage.count()).isEqualTo(1);
    }

    @Test
    void deleteById_WithNonExistingId_ShouldNotThrowException() {
        assertThatNoException().isThrownBy(() -> userService.deleteById(999L));
    }

    @Test
    void create_WithValidData_ShouldCreateAndReturnUser() {
        UserDto newUserDto = UserDto.builder()
                .name("New User")
                .email("new@email.com")
                .build();

        UserDto result = userService.create(newUserDto);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.id = :id", User.class);
        User user = query.setParameter("id", result.getId())
                .getSingleResult();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("New User");
        assertThat(result.getEmail()).isEqualTo("new@email.com");

        assertThat(user.getId()).isEqualTo(result.getId());
        assertThat(user.getName()).isEqualTo("New User");
        assertThat(user.getEmail()).isEqualTo("new@email.com");
    }

    @Test
    void create_WithNullEmail_ShouldThrowConditionsNotMetException() {
        UserDto userDto = UserDto.builder()
                .name("Test User")
                .email(null)
                .build();

        assertThatThrownBy(() -> userService.create(userDto))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessage("Электронная почта не может быть пустой");
        assertThat(userStorage.count()).isEqualTo(2);
    }

    @Test
    void create_WithBlankEmail_ShouldThrowConditionsNotMetException() {
        UserDto userDto = UserDto.builder()
                .name("Test User")
                .email("   ")
                .build();

        assertThatThrownBy(() -> userService.create(userDto))
                .isInstanceOf(ConditionsNotMetException.class)
                .hasMessage("Электронная почта не может быть пустой");
        assertThat(userStorage.count()).isEqualTo(2);
    }

    @Test
    void create_WithDuplicateEmail_ShouldThrowUserEmailConflictException() {
        UserDto duplicateUserDto = UserDto.builder()
                .name("Duplicate User")
                .email("existing@email.com")
                .build();

        assertThatThrownBy(() -> userService.create(duplicateUserDto))
                .isInstanceOf(UserEmailConflictException.class)
                .hasMessage("Пользователь с электронной почтой existing@email.com уже существует");
        assertThat(userStorage.count()).isEqualTo(2);
    }

    @Test
    void create_WithDuplicateEmailDifferentCase_ShouldThrowUserEmailConflictException() {
        UserDto duplicateUserDto = UserDto.builder()
                .name("Duplicate User")
                .email("EXISTING@email.com")
                .build();

        assertThatThrownBy(() -> userService.create(duplicateUserDto))
                .isInstanceOf(UserEmailConflictException.class)
                .hasMessageContaining("уже существует");
        assertThat(userStorage.count()).isEqualTo(2);
    }

    @Test
    void update_WithValidData_ShouldUpdateAndReturnUser() {
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email("updated@email.com")
                .build();

        UserDto result = userService.update(existingUserId, updateDto);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.id = :id", User.class);
        User user = query.setParameter("id", result.getId())
                .getSingleResult();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingUserId);
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("updated@email.com");

        assertThat(user.getName()).isEqualTo("Updated Name");
        assertThat(user.getEmail()).isEqualTo("updated@email.com");
    }

    @Test
    void update_WithOnlyName_ShouldUpdateNameAndKeepOriginalEmail() {
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .build();

        UserDto result = userService.update(existingUserId, updateDto);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("existing@email.com");
    }

    @Test
    void update_WithOnlyEmail_ShouldUpdateEmailAndKeepOriginalName() {
        UserDto updateDto = UserDto.builder()
                .email("updated@email.com")
                .build();

        UserDto result = userService.update(existingUserId, updateDto);

        assertThat(result.getName()).isEqualTo("Existing User");
        assertThat(result.getEmail()).isEqualTo("updated@email.com");
    }

    @Test
    void update_WithNullName_ShouldUseOriginalName() {
        UserDto updateDto = UserDto.builder()
                .name(null)
                .email("updated@email.com")
                .build();

        UserDto result = userService.update(existingUserId, updateDto);

        assertThat(result.getName()).isEqualTo("Existing User");
        assertThat(result.getEmail()).isEqualTo("updated@email.com");
    }

    @Test
    void update_WithBlankName_ShouldUseOriginalName() {
        UserDto updateDto = UserDto.builder()
                .name("   ")
                .email("updated@email.com")
                .build();

        UserDto result = userService.update(existingUserId, updateDto);

        assertThat(result.getName()).isEqualTo("Existing User");
        assertThat(result.getEmail()).isEqualTo("updated@email.com");
    }

    @Test
    void update_WithNullEmail_ShouldUseOriginalEmail() {
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email(null)
                .build();

        UserDto result = userService.update(existingUserId, updateDto);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("existing@email.com");
    }

    @Test
    void update_WithBlankEmail_ShouldUseOriginalEmail() {
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email("   ")
                .build();

        UserDto result = userService.update(existingUserId, updateDto);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("existing@email.com");
    }

    @Test
    void update_WithDuplicateEmail_ShouldThrowUserEmailConflictException() {
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email("another@email.com") // Email of another user
                .build();

        assertThatThrownBy(() -> userService.update(existingUserId, updateDto))
                .isInstanceOf(UserEmailConflictException.class)
                .hasMessage("Пользователь с электронной почтой another@email.com уже существует");
    }

    @Test
    void update_WithNonExistingId_ShouldThrowNotFoundException() {
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email("updated@email.com")
                .build();

        assertThatThrownBy(() -> userService.update(999L, updateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь с id " + 999L + " не найден");
    }

    @Test
    void update_WithSameEmail_ShouldUpdateSuccessfully() {
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email("existing@email.com")
                .build();

        UserDto result = userService.update(existingUserId, updateDto);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("existing@email.com");
    }

    @Test
    void create_MultipleUsers_ShouldIncrementIdsCorrectly() {
        // Given
        UserDto user1 = UserDto.builder().name("User1").email("user1@email.com").build();
        UserDto user2 = UserDto.builder().name("User2").email("user2@email.com").build();

        // When
        UserDto result1 = userService.create(user1);
        UserDto result2 = userService.create(user2);

        // Then
        assertThat(result1.getId()).isNotNull();
        assertThat(result2.getId()).isNotNull();
        assertThat(result2.getId()).isGreaterThan(result1.getId());
        assertThat(userStorage.count()).isEqualTo(4);
    }
}