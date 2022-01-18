package xyz.suchdoge.webapi.service;

import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.dto.notification.NotificationResponseDto;
import xyz.suchdoge.webapi.mapper.notification.NotificationMapper;
import xyz.suchdoge.webapi.model.notification.Notification;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.repository.NotificationRepository;
import xyz.suchdoge.webapi.websocket.WebSocketService;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final WebSocketService webSocketService;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationMapper notificationMapper,
                               WebSocketService webSocketService) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.webSocketService = webSocketService;
    }

    public void pushNotificationTo(Notification notification, DogeUser user) {
        notification.setUser(user);
        final Notification savedNotification = this.notificationRepository.save(notification);

        final NotificationResponseDto notificationDto = notificationMapper
                .notificationToNotificationResponseDto(savedNotification);
        webSocketService.sendTo(user.getUsername(), notificationDto);
    }
}
