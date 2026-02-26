package com.ragchat.api;

import com.ragchat.api.dto.*;
import com.ragchat.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(
        name = "Chat",
        description = "APIs for managing chat sessions and chat messages in the RAG Chat Storage Microservice"
)
@SecurityRequirement(name = "apiKeyAuth")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // -------------------- CREATE SESSION --------------------

    @PostMapping("/sessions")
    @Operation(
            summary = "Create a new chat session",
            description = "Creates a new chat session for a user. " +
                    "Each session represents a conversation between the user and the AI assistant."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Chat session created successfully",
                    content = @Content(schema = @Schema(implementation = ChatSessionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing API key")
    })
    public ResponseEntity<ChatSessionResponse> createSession(
            @Valid @RequestBody CreateSessionRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.createSession(request));
    }

    // -------------------- LIST SESSIONS --------------------

    @GetMapping("/sessions")
    @Operation(
            summary = "List chat sessions",
            description = "Retrieves all chat sessions belonging to a specific user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<ChatSessionResponse>> listSessions(
            @Parameter(
                    name = "userId",
                    description = "Unique identifier of the user whose sessions should be fetched",
                    required = true,
                    example = "Ram",
                    in = ParameterIn.QUERY
            )
            @RequestParam("userId") String userId) {

        return ResponseEntity.ok(chatService.listSessions(userId));
    }

    // -------------------- RENAME SESSION --------------------

    @PutMapping("/sessions/{sessionId}/rename")
    @Operation(
            summary = "Rename a chat session",
            description = "Updates the title of an existing chat session."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session renamed successfully"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ChatSessionResponse> renameSession(
            @Parameter(
                    description = "Unique identifier of the chat session",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable("sessionId") UUID sessionId,
            @Valid @RequestBody RenameSessionRequest request) {

        return ResponseEntity.ok(
                chatService.renameSession(sessionId, request.getTitle())
        );
    }

    // -------------------- FAVORITE SESSION --------------------

    @PutMapping("/sessions/{sessionId}/favorite")
    @Operation(
            summary = "Mark or unmark a session as favorite",
            description = "Allows a user to mark or unmark a chat session as favorite for quick access."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Favorite status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ChatSessionResponse> setFavorite(
            @Parameter(
                    description = "Unique identifier of the chat session",
                    required = true
            )
            @PathVariable("sessionId") UUID sessionId,
            @Valid @RequestBody ToggleFavoriteRequest request) {

        return ResponseEntity.ok(
                chatService.setFavorite(sessionId, request.isFavorite())
        );
    }

    // -------------------- DELETE SESSION --------------------

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(
            summary = "Delete a chat session",
            description = "Deletes a chat session and all its associated messages permanently."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Session deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteSession(
            @Parameter(
                    description = "Unique identifier of the chat session",
                    required = true
            )
            @PathVariable("sessionId") UUID sessionId) {

        chatService.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    // -------------------- ADD MESSAGE --------------------

    @PostMapping({"/sessions/{sessionId}/messages", "/sessions/messages"})
    @Operation(
            summary = "Add a message to a session",
            description = "Adds a user or assistant message to an existing chat session. " +
                    "If sessionId is not provided in the path, it must be present in the request body."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Message added successfully",
                    content = @Content(schema = @Schema(implementation = ChatMessageResponse.class))),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ChatMessageResponse> addMessage(
            @Parameter(
                    description = "Unique identifier of the chat session (optional depending on endpoint)",
                    required = false
            )
            @PathVariable(value = "sessionId", required = false) UUID sessionId,
            @Valid @RequestBody AddMessageRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.addMessage(sessionId, request));
    }

    // -------------------- GET MESSAGES --------------------

    @GetMapping("/sessions/{sessionId}/messages")
    @Operation(
            summary = "Get paginated messages",
            description = "Retrieves paginated chat messages for a specific session."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Page<ChatMessageResponse>> getMessages(
            @Parameter(
                    description = "Unique identifier of the chat session",
                    required = true
            )
            @PathVariable("sessionId") UUID sessionId,

            @Parameter(
                    description = "Page number (zero-based index)",
                    example = "0"
            )
            @RequestParam(name = "page", defaultValue = "0") int page,

            @Parameter(
                    description = "Number of records per page",
                    example = "20"
            )
            @RequestParam(name = "size", defaultValue = "20") int size) {

        return ResponseEntity.ok(
                chatService.getMessages(sessionId, page, size)
        );
    }
}