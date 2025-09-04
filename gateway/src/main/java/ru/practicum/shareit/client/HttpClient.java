package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Service
public class HttpClient {
    private final RestTemplate restTemplate;

    @Value("${shareit.api.auth.userheader}")
    private String userIdHeader;

    public HttpClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    public ResponseEntity<Object> get(String endPoint, Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (userId != null) headers.set(userIdHeader, String.valueOf(userId));
        HttpEntity<String> request = new HttpEntity<>("", headers);
        return restTemplate.exchange(endPoint, HttpMethod.GET, request, Object.class);
    }

    public ResponseEntity<Object> post(String endPoint, Long userId, Object object) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (userId != null) headers.set(userIdHeader, String.valueOf(userId));
        HttpEntity<Object> request = new HttpEntity<>(object, headers);
        return restTemplate.exchange(endPoint, HttpMethod.POST, request, Object.class);
    }
    public ResponseEntity<Object> patch(String endPoint, Long userId, Object object) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (userId != null) headers.set(userIdHeader, String.valueOf(userId));
        HttpEntity<Object> request = new HttpEntity<>(object, headers);
        return restTemplate.exchange(endPoint, HttpMethod.PATCH, request, Object.class);
    }

    public ResponseEntity<Object> delete(String endPoint, Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (userId != null) headers.set(userIdHeader, String.valueOf(userId));
        HttpEntity<String> request = new HttpEntity<>("", headers);
        return restTemplate.exchange(endPoint, HttpMethod.DELETE, request, Object.class);
    }
}
