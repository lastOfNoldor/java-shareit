package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.CreateItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;


    @GetMapping
    public ResponseEntity<Object> findAllItems(@RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return itemClient.getAllItems(userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findItemById(@RequestHeader(value = "X-Sharer-User-Id") Long userId, @PathVariable Long itemId) {
        return itemClient.getItemById(userId, itemId);
    }

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId, @RequestBody @Valid CreateItemDto createItemDto) {
        return itemClient.postItem(userId, createItemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(value = "X-Sharer-User-Id") Long userId, @PathVariable Long itemId, @RequestBody @Valid UpdateItemDto updateItemDto) {
        return itemClient.patchItem(userId, itemId, updateItemDto);

    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItem(@RequestParam String text) {
        return itemClient.searchItem("/search", text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader(value = "X-Sharer-User-Id") Long userId, @PathVariable Long itemId, @RequestBody @Valid CreateCommentDto createCommentDto) {
        return itemClient.createComment("/comment", userId, itemId, createCommentDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteItem(@RequestBody Long id) {
        return itemClient.deleteItem(id);
    }
}
