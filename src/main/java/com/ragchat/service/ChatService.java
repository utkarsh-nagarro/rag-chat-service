package com.ragchat.service;

import com.ragchat.api.dto.AddMessageRequest;
import com.ragchat.api.dto.ChatMessageResponse;
import com.ragchat.api.dto.ChatSessionResponse;
import com.ragchat.api.dto.CreateSessionRequest;
import com.ragchat.domain.ChatMessage;
import com.ragchat.domain.ChatSession;
import com.ragchat.repository.ChatMessageRepository;
import com.ragchat.repository.ChatSessionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;

    public ChatService(ChatSessionRepository sessionRepository, ChatMessageRepository messageRepository) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public ChatSessionResponse createSession(CreateSessionRequest request) {
        ChatSession session = new ChatSession();
        session.setUserId(request.getUserId());
        session.setTitle(request.getTitle());
        ChatSession saved = sessionRepository.save(session);
        return toSessionResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ChatSessionResponse> listSessions(String userId) {
        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(this::toSessionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatSessionResponse renameSession(UUID sessionId, String title) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        session.setTitle(title);
        return toSessionResponse(session);
    }

    @Transactional
    public ChatSessionResponse setFavorite(UUID sessionId, boolean favorite) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        session.setFavorite(favorite);
        return toSessionResponse(session);
    }

    @Transactional
    public void deleteSession(UUID sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Session not found");
        }
        sessionRepository.deleteById(sessionId);
    }

    @Transactional
    public ChatMessageResponse addMessage(UUID sessionId, AddMessageRequest request) {
        ChatSession session;
        if (sessionId == null) {
            // Auto create session
            session = new ChatSession();
            session.setUserId(request.getUserId());

            // Use first 10 chars of message as title
            String title = request.getContent().length() > 30
                    ? request.getContent().substring(0, 10)
                    : request.getContent();

            session.setTitle(title);
            session = sessionRepository.save(session);
        } else {
            session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        }

        ChatMessage message = new ChatMessage();
        message.setSession(session);
        message.setSender(request.getSender());
        message.setContent(request.getContent());
        message.setContext(request.getContext());

        ChatMessage saved = messageRepository.save(message);

        return toMessageResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessages(UUID sessionId, int page, int size) {
        ChatSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository.findBySessionOrderByCreatedAtAsc(session, pageable)
                .map(this::toMessageResponse);
    }

    private ChatSessionResponse toSessionResponse(ChatSession session) {
        ChatSessionResponse dto = new ChatSessionResponse();
        dto.setId(session.getId());
        dto.setUserId(session.getUserId());
        dto.setTitle(session.getTitle());
        dto.setFavorite(session.isFavorite());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setUpdatedAt(session.getUpdatedAt());
        return dto;
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message) {
        ChatMessageResponse dto = new ChatMessageResponse();
        dto.setId(message.getId());
        dto.setSender(message.getSender());
        dto.setContent(message.getContent());
        dto.setContext(message.getContext());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
}

