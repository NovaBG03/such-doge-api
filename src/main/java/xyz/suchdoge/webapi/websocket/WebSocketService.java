package xyz.suchdoge.webapi.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


@Service
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public WebSocketService(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendTo(String username, String destination, Object payload) {
        String serializedPayload = "";
        try {
            serializedPayload = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println("Sending to: " + username );
        messagingTemplate.convertAndSendToUser(username ,destination, serializedPayload);
    }
}
