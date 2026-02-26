package com.ragchat.service;

import com.ragchat.repository.ChatSessionRepository;
import com.ragchat.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatSessionRepository sessionRepository;

    @Mock
    private ChatMessageRepository messageRepository;

    @InjectMocks
    private ChatService chatService;

    @Test
    void listSessions_shouldReturnEmptyList() {

        when(sessionRepository.findByUserIdOrderByUpdatedAtDesc("Ram"))
                .thenReturn(Collections.emptyList());

        var result = chatService.listSessions("Ram");

        assertTrue(result.isEmpty());
        verify(sessionRepository).findByUserIdOrderByUpdatedAtDesc("Ram");
    }

    @Test
    void deleteSession_whenExists_shouldDelete() {

        UUID id = UUID.randomUUID();

        when(sessionRepository.existsById(id))
                .thenReturn(true);

        chatService.deleteSession(id);

        verify(sessionRepository).deleteById(id);
    }

    @Test
    void deleteSession_whenNotExists_shouldThrow() {

        UUID id = UUID.randomUUID();

        when(sessionRepository.existsById(id))
                .thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                chatService.deleteSession(id));
    }

    @Test
    void renameSession_whenNotFound_shouldThrow() {

        UUID id = UUID.randomUUID();

        when(sessionRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                chatService.renameSession(id, "Title"));
    }

    @Test
    void addMessage_whenSessionNotFound_shouldThrow() {

        UUID id = UUID.randomUUID();

        when(sessionRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                chatService.addMessage(id, null));
    }
}