package com.ragchat.service;

import com.ragchat.api.dto.AddMessageRequest;
import com.ragchat.api.dto.CreateSessionRequest;
import com.ragchat.api.dto.ChatMessageResponse;
import com.ragchat.api.dto.ChatSessionResponse;
import com.ragchat.domain.ChatMessage;
import com.ragchat.domain.ChatSession;
import com.ragchat.repository.ChatMessageRepository;
import com.ragchat.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    private ChatSessionRepository sessionRepository;
    private ChatMessageRepository messageRepository;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        sessionRepository = mock(ChatSessionRepository.class);
        messageRepository = mock(ChatMessageRepository.class);
        chatService = new ChatService(sessionRepository, messageRepository);
    }

    @Test
    void createSession_savesAndReturnsDto() {
        CreateSessionRequest request = new CreateSessionRequest();
        request.setUserId("user-1");
        request.setTitle("My chat");

        ChatSession saved = new ChatSession();
        saved.setUserId("user-1");
        saved.setTitle("My chat");

        when(sessionRepository.save(any(ChatSession.class))).thenReturn(saved);

        var response = chatService.createSession(request);

        assertThat(response.getUserId()).isEqualTo("user-1");
        assertThat(response.getTitle()).isEqualTo("My chat");
        verify(sessionRepository, times(1)).save(any(ChatSession.class));
    }

    @Test
    void addMessage_throwsWhenSessionMissing() {
        UUID id = UUID.randomUUID();
        when(sessionRepository.findById(id)).thenReturn(Optional.empty());

        AddMessageRequest request = new AddMessageRequest();
        request.setSender("USER");
        request.setContent("hello");

        assertThrows(ResourceNotFoundException.class, () -> chatService.addMessage(id, request));
    }

    @Test
    void renameSession_updatesTitle() {
        UUID id = UUID.randomUUID();
        ChatSession existing = new ChatSession();
        existing.setId(id);
        existing.setUserId("user-1");
        existing.setTitle("Old title");

        when(sessionRepository.findById(id)).thenReturn(Optional.of(existing));

        ChatSessionResponse response = chatService.renameSession(id, "New title");

        assertThat(response.getTitle()).isEqualTo("New title");
        verify(sessionRepository, times(1)).findById(id);
    }

    @Test
    void setFavorite_togglesFlag() {
        UUID id = UUID.randomUUID();
        ChatSession existing = new ChatSession();
        existing.setId(id);
        existing.setUserId("user-1");
        existing.setTitle("Chat");
        existing.setFavorite(false);

        when(sessionRepository.findById(id)).thenReturn(Optional.of(existing));

        ChatSessionResponse response = chatService.setFavorite(id, true);

        assertThat(response.isFavorite()).isTrue();
        verify(sessionRepository, times(1)).findById(id);
    }

    @Test
    void deleteSession_whenMissing_throws() {
        UUID id = UUID.randomUUID();
        when(sessionRepository.existsById(id)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> chatService.deleteSession(id));
    }

    @Test
    void getMessages_returnsPageOfResponses() {
        UUID sessionId = UUID.randomUUID();
        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setUserId("user-1");
        session.setTitle("Chat");

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        ChatMessage message = new ChatMessage();
        message.setSession(session);
        message.setSender("USER");
        message.setContent("hello");
        message.setCreatedAt(OffsetDateTime.now());

        PageRequest pageable = PageRequest.of(0, 10);
        Page<ChatMessage> page = new PageImpl<>(List.of(message), pageable, 1);

        when(messageRepository.findBySessionOrderByCreatedAtAsc(session, pageable)).thenReturn(page);

        Page<ChatMessageResponse> result = chatService.getMessages(sessionId, 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("hello");
        verify(sessionRepository, times(1)).findById(sessionId);
        verify(messageRepository, times(1)).findBySessionOrderByCreatedAtAsc(session, pageable);
    }
}

