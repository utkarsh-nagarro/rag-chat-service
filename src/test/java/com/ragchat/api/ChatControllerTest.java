package com.ragchat.api;

import com.ragchat.api.dto.*;
import com.ragchat.service.ChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    // ---------------- CREATE SESSION ----------------

    @Test
    void createSession_shouldReturnCreated() {

        CreateSessionRequest request = new CreateSessionRequest();

        ChatSessionResponse response = new ChatSessionResponse();

        when(chatService.createSession(request))
                .thenReturn(response);

        var result = chatController.createSession(request);

        assertEquals(201, result.getStatusCode().value());
        assertEquals(response, result.getBody());
        verify(chatService).createSession(request);
    }

    // ---------------- LIST SESSIONS ----------------

    @Test
    void listSessions_shouldReturnOk() {

        List<ChatSessionResponse> list = new ArrayList<>();

        when(chatService.listSessions("Ram"))
                .thenReturn(list);

        var result = chatController.listSessions("Ram");

        assertEquals(200, result.getStatusCode().value());
        assertEquals(list, result.getBody());
        verify(chatService).listSessions("Ram");
    }

    // ---------------- RENAME SESSION ----------------

    @Test
    void renameSession_shouldReturnOk() {

        UUID id = UUID.randomUUID();

        RenameSessionRequest request = new RenameSessionRequest();

        ChatSessionResponse response = new ChatSessionResponse();

        when(chatService.renameSession(id, request.getTitle()))
                .thenReturn(response);

        var result = chatController.renameSession(id, request);

        assertEquals(200, result.getStatusCode().value());
        verify(chatService).renameSession(id, request.getTitle());
    }

    // ---------------- SET FAVORITE ----------------

    @Test
    void setFavorite_shouldReturnOk() {

        UUID id = UUID.randomUUID();

        ToggleFavoriteRequest request = new ToggleFavoriteRequest();

        ChatSessionResponse response = new ChatSessionResponse();

        when(chatService.setFavorite(id, request.isFavorite()))
                .thenReturn(response);

        var result = chatController.setFavorite(id, request);

        assertEquals(200, result.getStatusCode().value());
        verify(chatService).setFavorite(id, request.isFavorite());
    }

    // ---------------- DELETE SESSION ----------------

    @Test
    void deleteSession_shouldReturnNoContent() {

        UUID id = UUID.randomUUID();

        doNothing().when(chatService).deleteSession(id);

        var result = chatController.deleteSession(id);

        assertEquals(204, result.getStatusCode().value());
        verify(chatService).deleteSession(id);
    }

    // ---------------- ADD MESSAGE ----------------

    @Test
    void addMessage_shouldReturnCreated() {

        UUID id = UUID.randomUUID();

        AddMessageRequest request = new AddMessageRequest();
        ChatMessageResponse response = new ChatMessageResponse();

        when(chatService.addMessage(id, request))
                .thenReturn(response);

        var result = chatController.addMessage(id, request);

        assertEquals(201, result.getStatusCode().value());
        verify(chatService).addMessage(id, request);
    }

    // ---------------- GET MESSAGES ----------------

    @Test
    void getMessages_shouldReturnOk() {

        UUID id = UUID.randomUUID();

        Page<ChatMessageResponse> page =
                new PageImpl<>(Collections.emptyList());

        when(chatService.getMessages(id, 0, 20))
                .thenReturn(page);

        var result = chatController.getMessages(id, 0, 20);

        assertEquals(200, result.getStatusCode().value());
        verify(chatService).getMessages(id, 0, 20);
    }
}