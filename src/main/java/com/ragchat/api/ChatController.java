package com.ragchat.api;

import com.ragchat.api.dto.*;
import com.ragchat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Validated
@Tag(name = "Chat", description = "Chat session and message APIs")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/sessions")
    @Operation(summary = "Create a new chat session")
    public ResponseEntity<ChatSessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.createSession(request));
    }

    @GetMapping("/sessions")
    @Operation(summary = "List chat sessions for a user")
    public ResponseEntity<List<ChatSessionResponse>> listSessions(@RequestParam("userId") String userId) {
        return ResponseEntity.ok(chatService.listSessions(userId));
    }

    @PutMapping("/sessions/{sessionId}/rename")
    @Operation(summary = "Rename a chat session")
    public ResponseEntity<ChatSessionResponse> renameSession(@PathVariable("sessionId") UUID sessionId,
                                                             @Valid @RequestBody RenameSessionRequest request) {
        return ResponseEntity.ok(chatService.renameSession(sessionId, request.getTitle()));
    }

    @PutMapping("/sessions/{sessionId}/favorite")
    @Operation(summary = "Mark or unmark a chat session as favorite")
    public ResponseEntity<ChatSessionResponse> setFavorite(@PathVariable("sessionId") UUID sessionId,
                                                           @Valid @RequestBody ToggleFavoriteRequest request) {
        return ResponseEntity.ok(chatService.setFavorite(sessionId, request.isFavorite()));
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "Delete a chat session and its messages")
    public ResponseEntity<Void> deleteSession(@PathVariable("sessionId") UUID sessionId) {
        chatService.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping({"/sessions/{sessionId}/messages", "/sessions/messages"})
    @Operation(summary = "Add a message to a chat session")
    public ResponseEntity<ChatMessageResponse> addMessage(@PathVariable(value = "sessionId", required = false) UUID sessionId,
                                                          @Valid @RequestBody AddMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatService.addMessage(sessionId, request));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    @Operation(summary = "Get paginated messages for a chat session")
    public ResponseEntity<Page<ChatMessageResponse>> getMessages(@PathVariable("sessionId") UUID sessionId,
                                                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                                                 @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(chatService.getMessages(sessionId, page, size));
    }
}

