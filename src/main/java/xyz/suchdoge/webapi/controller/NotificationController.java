package xyz.suchdoge.webapi.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.suchdoge.webapi.websocket.WebSocketService;

@RestController
@RequestMapping("notification")
public class NotificationController {
    private final WebSocketService webSocketService;

    public NotificationController(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @PostMapping("/{username}")
    public void SendNotification(@PathVariable String username) {
        this.webSocketService.sendTo(username, "default message");
    }
}
