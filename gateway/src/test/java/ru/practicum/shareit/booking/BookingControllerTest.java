package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingStateQueryParam;
import ru.practicum.shareit.client.HttpClient;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private HttpClient httpClient;

    @Value("${shareit.api.auth.userheader}")
    private String userIdHeader;

    private final ResponseEntity<Object> mockResponse = ResponseEntity.ok().body("{\"id\": 1}");

    @Test
    void getByIdForOwnerOrBooker_ShouldReturnOk() throws Exception {
        long userId = 1L;
        long bookingId = 1L;

        when(httpClient.get(eq("/bookings/1"), eq(userId)))
                .thenReturn(mockResponse);

        mvc.perform(get("/bookings/{id}", bookingId)
                        .header(userIdHeader, userId))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1}"));
    }

    @Test
    void getByIdForOwnerOrBooker_WithInvalidUserId_ShouldReturnBadRequest() throws Exception {
        long invalidUserId = 0L;
        long bookingId = 1L;

        mvc.perform(get("/bookings/{id}", bookingId)
                        .header(userIdHeader, invalidUserId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByIdForOwnerOrBooker_WithInvalidBookingId_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;
        long invalidBookingId = 0L;

        mvc.perform(get("/bookings/{id}", invalidBookingId)
                        .header(userIdHeader, userId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllForBooker_ShouldReturnOk() throws Exception {
        long userId = 1L;
        BookingStateQueryParam state = BookingStateQueryParam.ALL;

        when(httpClient.get(eq("/bookings?state=ALL"), eq(userId)))
                .thenReturn(mockResponse);

        mvc.perform(get("/bookings")
                        .header(userIdHeader, userId)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1}"));
    }

    @Test
    void getAllForBooker_WithDefaultState_ShouldReturnOk() throws Exception {
        long userId = 1L;
        when(httpClient.get(eq("/bookings?state=ALL"), eq(userId)))
                .thenReturn(mockResponse);

        mvc.perform(get("/bookings")
                        .header(userIdHeader, userId))
                .andExpect(status().isOk());
    }

    @Test
    void getAllForBooker_WithDifferentStates_ShouldReturnOk() throws Exception {
        long userId = 1L;
        BookingStateQueryParam[] states = BookingStateQueryParam.values();
        for (BookingStateQueryParam state : states) {
            when(httpClient.get(eq("/bookings?state=" + state), eq(userId)))
                    .thenReturn(mockResponse);

            mvc.perform(get("/bookings")
                            .header(userIdHeader, userId)
                            .param("state", state.toString()))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void getAllForBooker_WithInvalidUserId_ShouldReturnBadRequest() throws Exception {
        long invalidUserId = 0L;
        mvc.perform(get("/bookings")
                        .header(userIdHeader, invalidUserId)
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllForOwner_ShouldReturnOk() throws Exception {
        long userId = 1L;
        BookingStateQueryParam state = BookingStateQueryParam.CURRENT;

        when(httpClient.get(eq("/bookings/owner?state=CURRENT"), eq(userId)))
                .thenReturn(mockResponse);

        mvc.perform(get("/bookings/owner")
                        .header(userIdHeader, userId)
                        .param("state", "CURRENT"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1}"));
    }

    @Test
    void getAllForOwner_WithDefaultState_ShouldReturnOk() throws Exception {
        long userId = 1L;
        when(httpClient.get(eq("/bookings/owner?state=ALL"), eq(userId)))
                .thenReturn(mockResponse);

        mvc.perform(get("/bookings/owner")
                        .header(userIdHeader, userId))
                .andExpect(status().isOk());
    }

    @Test
    void getAllForOwner_WithInvalidUserId_ShouldReturnBadRequest() throws Exception {
        long invalidUserId = 0L;
        mvc.perform(get("/bookings/owner")
                        .header(userIdHeader, invalidUserId)
                        .param("state", "ALL"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_ShouldReturnOk() throws Exception {
        long userId = 1L;
        BookingCreateDto createDto = BookingCreateDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .build();

        when(httpClient.post(eq("/bookings"), eq(userId), any(BookingCreateDto.class)))
                .thenReturn(mockResponse);

        mvc.perform(post("/bookings")
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1}"));
    }

    @Test
    void create_WithInvalidUserId_ShouldReturnBadRequest() throws Exception {
        long invalidUserId = 0L;
        BookingCreateDto createDto = BookingCreateDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .build();

        mvc.perform(post("/bookings")
                        .header(userIdHeader, invalidUserId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithInvalidBooking_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;
        BookingCreateDto createDto = BookingCreateDto.builder()
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .itemId(1L)
                .build();

        mvc.perform(post("/bookings")
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithNullFields_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;
        BookingCreateDto createDto = BookingCreateDto.builder()
                .start(null)
                .end(LocalDateTime.now().plusDays(1))
                .itemId(1L)
                .build();

        mvc.perform(post("/bookings")
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WithPastDates_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;
        BookingCreateDto createDto = BookingCreateDto.builder()
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .itemId(1L)
                .build();

        mvc.perform(post("/bookings")
                        .header(userIdHeader, userId)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStateByOwner_WithApprovedTrue_ShouldReturnOk() throws Exception {
        long userId = 1L;
        long bookingId = 1L;
        String approved = "true";

        when(httpClient.patch(eq("/bookings/1?approved=true"), eq(userId), eq(null)))
                .thenReturn(mockResponse);

        mvc.perform(patch("/bookings/{id}", bookingId)
                        .header(userIdHeader, userId)
                        .param("approved", approved))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"id\": 1}"));
    }

    @Test
    void updateStateByOwner_WithApprovedFalse_ShouldReturnOk() throws Exception {
        long userId = 1L;
        long bookingId = 1L;
        String approved = "false";

        when(httpClient.patch(eq("/bookings/1?approved=false"), eq(userId), eq(null)))
                .thenReturn(mockResponse);

        mvc.perform(patch("/bookings/{id}", bookingId)
                        .header(userIdHeader, userId)
                        .param("approved", approved))
                .andExpect(status().isOk());
    }

    @Test
    void updateStateByOwner_WithCaseInsensitiveApproved_ShouldReturnOk() throws Exception {
        long userId = 1L;
        long bookingId = 1L;

        when(httpClient.patch(eq("/bookings/1?approved=TRUE"), eq(userId), eq(null)))
                .thenReturn(mockResponse);

        mvc.perform(patch("/bookings/{id}", bookingId)
                        .header(userIdHeader, userId)
                        .param("approved", "TRUE"))
                .andExpect(status().isOk());
    }

    @Test
    void updateStateByOwner_WithInvalidApproved_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;
        long bookingId = 1L;
        String invalidApproved = "invalid";

        mvc.perform(patch("/bookings/{id}", bookingId)
                        .header(userIdHeader, userId)
                        .param("approved", invalidApproved))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStateByOwner_WithInvalidUserId_ShouldReturnBadRequest() throws Exception {
        long invalidUserId = 0L;
        long bookingId = 1L;
        String approved = "true";

        mvc.perform(patch("/bookings/{id}", bookingId)
                        .header(userIdHeader, invalidUserId)
                        .param("approved", approved))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStateByOwner_WithInvalidBookingId_ShouldReturnBadRequest() throws Exception {
        long userId = 1L;
        long invalidBookingId = 0L;
        String approved = "true";

        mvc.perform(patch("/bookings/{id}", invalidBookingId)
                        .header(userIdHeader, userId)
                        .param("approved", approved))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenHttpClientReturnsError_ShouldPropagateError() throws Exception {
        long userId = 1L;
        long bookingId = 1L;

        when(httpClient.get(eq("/bookings/1"), eq(userId)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found"));

        mvc.perform(get("/bookings/{id}", bookingId)
                        .header(userIdHeader, userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_WithMissingHeader_ShouldReturnBadRequest() throws Exception {
        BookingCreateDto createDto = BookingCreateDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .build();

        mvc.perform(post("/bookings")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }
}