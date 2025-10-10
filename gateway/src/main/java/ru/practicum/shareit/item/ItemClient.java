package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.CreateItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {

    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(builder.uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX)).requestFactory(() -> new HttpComponentsClientHttpRequestFactory()).build());
    }

    public ResponseEntity<Object> getAllItems(Long userId) {
        return get("", userId);
    }


    public ResponseEntity<Object> getItemById(Long userId, Long itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> postItem(Long userId, CreateItemDto createItemDto) {
        return post("", userId, createItemDto);
    }

    public ResponseEntity<Object> patchItem(Long userId, Long itemId, UpdateItemDto updateItemDto) {
        return patch("/" + itemId, userId, updateItemDto);
    }

    public ResponseEntity<Object> searchItem(String path, String text) {
        Map<String, Object> parameters = Map.of("text", text);
        return get(path + "?text={text}", null, parameters);
    }

    public ResponseEntity<Object> createComment(String path, Long userId, Long itemId, @Valid CreateCommentDto createCommentDto) {
        return post("/" + itemId + path, userId, createCommentDto);
    }

    public ResponseEntity<Object> deleteItem(Long id) {
        return delete("/" + id);
    }
}
