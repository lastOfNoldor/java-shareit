package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.CreateRequestDto;

@Service
public class RequestClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public RequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX)).requestFactory(() -> new HttpComponentsClientHttpRequestFactory()).build());
    }

    public ResponseEntity<Object> createRequest(long userId, CreateRequestDto description) {
        return post("", userId, description);
    }

    public ResponseEntity<Object> getUsersRequests(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getAllRequests(String path, long userId) {
        return get(path, userId);
    }

    public ResponseEntity<Object> getRequestById(long reqId) {
        return get("/" + reqId);
    }
}