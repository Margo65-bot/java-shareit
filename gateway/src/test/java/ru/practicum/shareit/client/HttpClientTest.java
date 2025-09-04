package ru.practicum.shareit.client;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HttpClientTest {

    @Setter
    @Getter
    static class TestResponse {
        private String message;
        private Integer value;

        public TestResponse() {}

        public TestResponse(String message, Integer value) {
            this.message = message;
            this.value = value;
        }
    }

    @Setter
    @Getter
    static class TestRequest {
        private String data;
        private Integer number;

        public TestRequest() {}

        public TestRequest(String data, Integer number) {
            this.data = data;
            this.number = number;
        }
    }

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    private HttpClient httpClient;

    private final String endPoint = "/test";
    private final Long userId = 1L;
    private final TestRequest requestBody = new TestRequest("test data", 123);
    private final ResponseEntity<Object> mockResponse = ResponseEntity.ok().body(new TestResponse("success", 200));

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.uriTemplateHandler(any(DefaultUriBuilderFactory.class)))
                .thenReturn(restTemplateBuilder);

        when(restTemplateBuilder.requestFactory(any(Supplier.class)))
                .thenReturn(restTemplateBuilder);

        when(restTemplateBuilder.build())
                .thenReturn(restTemplate);

        httpClient = new HttpClient("http://localhost:9090", restTemplateBuilder);

        ReflectionTestUtils.setField(httpClient, "userIdHeader", "X-Sharer-User-Id");
    }

    @Test
    void get_WithUserId_ShouldSetHeadersAndCallExchange() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(mockResponse);

        ResponseEntity<Object> response = httpClient.get(endPoint, userId);

        assertNotNull(response);
        assertEquals(mockResponse, response);
        verify(restTemplate).exchange(
                eq(endPoint),
                eq(HttpMethod.GET),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.getContentType().equals(MediaType.APPLICATION_JSON) &&
                            headers.get("X-Sharer-User-Id") != null &&
                            headers.get("X-Sharer-User-Id").getFirst().equals(userId.toString());
                }),
                eq(Object.class)
        );
    }

    @Test
    void get_WithoutUserId_ShouldSetHeadersWithoutUserId() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(mockResponse);

        ResponseEntity<Object> response = httpClient.get(endPoint, null);

        assertNotNull(response);
        verify(restTemplate).exchange(
                eq(endPoint),
                eq(HttpMethod.GET),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.getContentType().equals(MediaType.APPLICATION_JSON) &&
                            (headers.get("X-Sharer-User-Id") == null || headers.get("X-Sharer-User-Id").isEmpty());
                }),
                eq(Object.class)
        );
    }

    @Test
    void post_WithUserIdAndBody_ShouldSetHeadersAndCallExchange() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(mockResponse);

        ResponseEntity<Object> response = httpClient.post(endPoint, userId, requestBody);

        assertNotNull(response);
        assertEquals(mockResponse, response);
        verify(restTemplate).exchange(
                eq(endPoint),
                eq(HttpMethod.POST),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.getContentType().equals(MediaType.APPLICATION_JSON) &&
                            headers.get("X-Sharer-User-Id") != null &&
                            headers.get("X-Sharer-User-Id").getFirst().equals(userId.toString()) &&
                            entity.getBody() == requestBody;
                }),
                eq(Object.class)
        );
    }

    @Test
    void post_WithoutUserId_ShouldSetHeadersWithoutUserId() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(mockResponse);

        ResponseEntity<Object> response = httpClient.post(endPoint, null, requestBody);

        assertNotNull(response);
        verify(restTemplate).exchange(
                eq(endPoint),
                eq(HttpMethod.POST),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.getContentType().equals(MediaType.APPLICATION_JSON) &&
                            (headers.get("X-Sharer-User-Id") == null || headers.get("X-Sharer-User-Id").isEmpty()) &&
                            entity.getBody() == requestBody;
                }),
                eq(Object.class)
        );
    }

    @Test
    void patch_WithUserIdAndBody_ShouldSetHeadersAndCallExchange() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(mockResponse);

        ResponseEntity<Object> response = httpClient.patch(endPoint, userId, requestBody);

        assertNotNull(response);
        assertEquals(mockResponse, response);
        verify(restTemplate).exchange(
                eq(endPoint),
                eq(HttpMethod.PATCH),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.getContentType().equals(MediaType.APPLICATION_JSON) &&
                            headers.get("X-Sharer-User-Id") != null &&
                            headers.get("X-Sharer-User-Id").getFirst().equals(userId.toString()) &&
                            entity.getBody() == requestBody;
                }),
                eq(Object.class)
        );
    }

    @Test
    void patch_WithoutUserId_ShouldSetHeadersWithoutUserId() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(mockResponse);

        ResponseEntity<Object> response = httpClient.patch(endPoint, null, requestBody);

        assertNotNull(response);
        verify(restTemplate).exchange(
                eq(endPoint),
                eq(HttpMethod.PATCH),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.getContentType().equals(MediaType.APPLICATION_JSON) &&
                            (headers.get("X-Sharer-User-Id") == null || headers.get("X-Sharer-User-Id").isEmpty()) &&
                            entity.getBody() == requestBody;
                }),
                eq(Object.class)
        );
    }

    @Test
    void delete_WithUserId_ShouldSetHeadersAndCallExchange() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(mockResponse);

        ResponseEntity<Object> response = httpClient.delete(endPoint, userId);

        assertNotNull(response);
        assertEquals(mockResponse, response);
        verify(restTemplate).exchange(
                eq(endPoint),
                eq(HttpMethod.DELETE),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.getContentType().equals(MediaType.APPLICATION_JSON) &&
                            headers.get("X-Sharer-User-Id") != null &&
                            headers.get("X-Sharer-User-Id").getFirst().equals(userId.toString());
                }),
                eq(Object.class)
        );
    }

    @Test
    void delete_WithoutUserId_ShouldSetHeadersWithoutUserId() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(mockResponse);

        ResponseEntity<Object> response = httpClient.delete(endPoint, null);

        assertNotNull(response);
        verify(restTemplate).exchange(
                eq(endPoint),
                eq(HttpMethod.DELETE),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.getContentType().equals(MediaType.APPLICATION_JSON) &&
                            (headers.get("X-Sharer-User-Id") == null || headers.get("X-Sharer-User-Id").isEmpty());
                }),
                eq(Object.class)
        );
    }

    @Test
    void get_WhenRestTemplateThrowsException_ShouldPropagateException() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(new RuntimeException("Network error"));

        assertThrows(RuntimeException.class, () -> httpClient.get(endPoint, userId));
    }

    @Test
    void post_WhenRestTemplateThrowsException_ShouldPropagateException() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(new RuntimeException("Network error"));

        assertThrows(RuntimeException.class, () -> httpClient.post(endPoint, userId, requestBody));
    }

    @Test
    void patch_WhenRestTemplateThrowsException_ShouldPropagateException() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(new RuntimeException("Network error"));

        assertThrows(RuntimeException.class, () -> httpClient.patch(endPoint, userId, requestBody));
    }

    @Test
    void delete_WhenRestTemplateThrowsException_ShouldPropagateException() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(new RuntimeException("Network error"));

        assertThrows(RuntimeException.class, () -> httpClient.delete(endPoint, userId));
    }

    @Test
    void methods_WithEmptyStringEndpoint_ShouldHandleCorrectly() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(mockResponse);

        ResponseEntity<Object> response = httpClient.get("", userId);

        assertNotNull(response);
        verify(restTemplate).exchange(eq(""), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class));
    }

    @Test
    void methods_WithNullRequestBody_ShouldHandleCorrectly() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(mockResponse);

        ResponseEntity<Object> response = httpClient.post(endPoint, userId, null);

        assertNotNull(response);
        verify(restTemplate).exchange(
                eq(endPoint),
                eq(HttpMethod.POST),
                argThat(entity -> entity.getBody() == null),
                eq(Object.class)
        );
    }

    @Test
    void constructor_ShouldInitializeCorrectly() {
        HttpClient client = new HttpClient("http://test:8080", restTemplateBuilder);
        ReflectionTestUtils.setField(client, "userIdHeader", "X-Sharer-User-Id");
        assertNotNull(client);
    }

    @Test
    void shouldWorkWithDifferentHeaderName() {
        ReflectionTestUtils.setField(httpClient, "userIdHeader", "X-Custom-User-Id");

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(mockResponse);

        ResponseEntity<Object> response = httpClient.get(endPoint, userId);

        assertNotNull(response);
        verify(restTemplate).exchange(
                eq(endPoint),
                eq(HttpMethod.GET),
                argThat(entity -> {
                    HttpHeaders headers = entity.getHeaders();
                    return headers.get("X-Custom-User-Id") != null &&
                            headers.get("X-Custom-User-Id").getFirst().equals(userId.toString());
                }),
                eq(Object.class)
        );
    }
}