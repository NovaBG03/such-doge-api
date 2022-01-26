package xyz.suchdoge.webapi.service;

import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.dto.notification.NotificationResponseDto;
import xyz.suchdoge.webapi.mapper.notification.NotificationMapper;
import xyz.suchdoge.webapi.model.notification.Notification;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.repository.NotificationRepository;
import xyz.suchdoge.webapi.websocket.WebSocketService;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final WebSocketService webSocketService;
    private final DogeUserService userService;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationMapper notificationMapper,
                               WebSocketService webSocketService,
                               DogeUserService userService) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.webSocketService = webSocketService;
        this.userService = userService;
    }

    public void pushNotificationTo(Notification notification, DogeUser user) {
        notification.setUser(user);
        final Notification savedNotification = this.notificationRepository.save(notification);

        final NotificationResponseDto notificationDto = notificationMapper
                .notificationToNotificationResponseDto(savedNotification);
        webSocketService.sendTo(user.getUsername(), notificationDto);
    }

    public Collection<Notification> getAllNotifications(String username) {
        return this.notificationRepository.findAllByUserUsername(username);
    }

    public void deleteAll(Collection<Long> ids, String username) {
        DogeUser user = this.userService.getUserByUsername(username);
        Collection<Notification> notificationsToDelete = user.getNotifications()
                .stream()
                .filter(notification -> ids.contains(notification.getId()))
                .collect(Collectors.toList());
        this.notificationRepository.deleteAll(notificationsToDelete);
    }
}
