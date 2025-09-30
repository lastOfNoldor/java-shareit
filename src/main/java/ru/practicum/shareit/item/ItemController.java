package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<Collection<ItemDto>> findAllItems(@RequestHeader(value = "X-Sharer-User-Id") Long userId) {
        return ResponseEntity.ok(itemService.findAllUsersItems(userId));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDtoWithMultipleBookings> findItemById(@RequestHeader(value = "X-Sharer-User-Id") Long userId, @PathVariable Long itemId) {
        return ResponseEntity.ok(itemService.findItemById(userId, itemId));
    }

    @PostMapping
    public ResponseEntity<ItemDto> createItem(@RequestHeader(value = "X-Sharer-User-Id", required = false) String userId, @RequestBody @Valid CreateItemDto createItemDto) {
        return ResponseEntity.ok(itemService.createItem(userId, createItemDto));
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<UpdateItemDto> updateItem(@RequestHeader(value = "X-Sharer-User-Id") Long userId, @PathVariable Long itemId, @RequestBody UpdateItemDto updateItemDto) {
        return ResponseEntity.ok(itemService.updateItem(userId, itemId, updateItemDto));

    }

    @GetMapping("/search")
    public ResponseEntity<Collection<UpdateItemDto>> searchItem(@RequestParam String text) {
        return ResponseEntity.ok(itemService.searchItem(text));
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> createComment(@RequestHeader(value = "X-Sharer-User-Id") Long userId, @PathVariable Long itemId, @RequestBody CreateCommentDto createCommentDto) {
        return ResponseEntity.ok(itemService.createComment(userId, itemId, createCommentDto));
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@RequestBody Long id) {
        itemService.deleteItem(id);

    }
}
