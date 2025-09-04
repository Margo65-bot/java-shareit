package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.dto.BookingStateQueryParam;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    private final Long userId = 1L;
    private final Long bookingId = 1L;
    private final LocalDateTime start = LocalDateTime.now().plusDays(1);
    private final LocalDateTime end = LocalDateTime.now().plusDays(2);

    @Test
    void create_ValidRequest_ShouldReturnBooking() throws Exception {
        BookingCreateDto createDto = BookingCreateDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(bookingId)
                .start(start)
                .end(end)
                .status(BookingState.WAITING)
                .booker(UserDto.builder().id(userId).build())
                .item(ItemDto.builder().id(1L).build())
                .build();

        when(bookingService.create(eq(userId), any(BookingCreateDto.class)))
                .thenReturn(responseDto);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void updateState_ValidApproval_ShouldReturnUpdatedBooking() throws Exception {
        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(bookingId)
                .status(BookingState.APPROVED)
                .build();

        when(bookingService.updateStateByOwner(eq(userId), eq(bookingId), eq(true)))
                .thenReturn(responseDto);

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void updateState_ValidRejection_ShouldReturnUpdatedBooking() throws Exception {
        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(bookingId)
                .status(BookingState.REJECTED)
                .build();

        when(bookingService.updateStateByOwner(eq(userId), eq(bookingId), eq(false)))
                .thenReturn(responseDto);

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId))
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void getById_ValidRequest_ShouldReturnBooking() throws Exception {
        BookingResponseDto responseDto = BookingResponseDto.builder()
                .id(bookingId)
                .status(BookingState.APPROVED)
                .build();

        when(bookingService.getByIdForOwnerOrBooker(eq(userId), eq(bookingId)))
                .thenReturn(responseDto);

        mvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId));
    }

    @Test
    void getAllForBooker_WithAllState_ShouldReturnBookings() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(bookingId)
                .status(BookingState.APPROVED)
                .build();

        when(bookingService.getAllForBooker(eq(userId), eq(BookingStateQueryParam.ALL)))
                .thenReturn(List.of(booking));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId));
    }

    @Test
    void getAllForBooker_WithCurrentState_ShouldReturnBookings() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(bookingId)
                .status(BookingState.APPROVED)
                .build();

        when(bookingService.getAllForBooker(eq(userId), eq(BookingStateQueryParam.CURRENT)))
                .thenReturn(List.of(booking));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "CURRENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId));
    }

    @Test
    void getAllForBooker_WithPastState_ShouldReturnBookings() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(bookingId)
                .status(BookingState.APPROVED)
                .build();

        when(bookingService.getAllForBooker(eq(userId), eq(BookingStateQueryParam.PAST)))
                .thenReturn(List.of(booking));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "PAST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId));
    }

    @Test
    void getAllForBooker_WithFutureState_ShouldReturnBookings() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(bookingId)
                .status(BookingState.APPROVED)
                .build();

        when(bookingService.getAllForBooker(eq(userId), eq(BookingStateQueryParam.FUTURE)))
                .thenReturn(List.of(booking));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "FUTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId));
    }

    @Test
    void getAllForBooker_WithWaitingState_ShouldReturnBookings() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(bookingId)
                .status(BookingState.WAITING)
                .build();

        when(bookingService.getAllForBooker(eq(userId), eq(BookingStateQueryParam.WAITING)))
                .thenReturn(List.of(booking));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("WAITING"));
    }

    @Test
    void getAllForBooker_WithRejectedState_ShouldReturnBookings() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(bookingId)
                .status(BookingState.REJECTED)
                .build();

        when(bookingService.getAllForBooker(eq(userId), eq(BookingStateQueryParam.REJECTED)))
                .thenReturn(List.of(booking));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "REJECTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("REJECTED"));
    }

    @Test
    void getAllForBooker_WithoutState_ShouldUseAllState() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(bookingId)
                .status(BookingState.APPROVED)
                .build();

        when(bookingService.getAllForBooker(eq(userId), eq(BookingStateQueryParam.ALL)))
                .thenReturn(List.of(booking));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId));
    }

    @Test
    void getAllForOwner_WithAllState_ShouldReturnBookings() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(bookingId)
                .status(BookingState.APPROVED)
                .build();

        when(bookingService.getAllForOwner(eq(userId), eq(BookingStateQueryParam.ALL)))
                .thenReturn(List.of(booking));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId));
    }

    @Test
    void getAllForOwner_WithCurrentState_ShouldReturnBookings() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(bookingId)
                .status(BookingState.APPROVED)
                .build();

        when(bookingService.getAllForOwner(eq(userId), eq(BookingStateQueryParam.CURRENT)))
                .thenReturn(List.of(booking));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "CURRENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId));
    }

    @Test
    void getAllForOwner_WithPastState_ShouldReturnBookings() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(bookingId)
                .status(BookingState.APPROVED)
                .build();

        when(bookingService.getAllForOwner(eq(userId), eq(BookingStateQueryParam.PAST)))
                .thenReturn(List.of(booking));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "PAST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId));
    }

    @Test
    void getAllForOwner_WithFutureState_ShouldReturnBookings() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(bookingId)
                .status(BookingState.APPROVED)
                .build();

        when(bookingService.getAllForOwner(eq(userId), eq(BookingStateQueryParam.FUTURE)))
                .thenReturn(List.of(booking));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "FUTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId));
    }

    @Test
    void getAllForOwner_WithWaitingState_ShouldReturnBookings() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(bookingId)
                .status(BookingState.WAITING)
                .build();

        when(bookingService.getAllForOwner(eq(userId), eq(BookingStateQueryParam.WAITING)))
                .thenReturn(List.of(booking));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("WAITING"));
    }

    @Test
    void getAllForOwner_WithRejectedState_ShouldReturnBookings() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(bookingId)
                .status(BookingState.REJECTED)
                .build();

        when(bookingService.getAllForOwner(eq(userId), eq(BookingStateQueryParam.REJECTED)))
                .thenReturn(List.of(booking));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "REJECTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("REJECTED"));
    }

    @Test
    void getAllForOwner_WithoutState_ShouldUseAllState() throws Exception {
        BookingResponseDto booking = BookingResponseDto.builder()
                .id(bookingId)
                .status(BookingState.APPROVED)
                .build();

        when(bookingService.getAllForOwner(eq(userId), eq(BookingStateQueryParam.ALL)))
                .thenReturn(List.of(booking));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId));
    }
}