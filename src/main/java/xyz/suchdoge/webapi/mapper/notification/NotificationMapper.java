package xyz.suchdoge.webapi.mapper.notification;

import xyz.suchdoge.webapi.dto.notification.NotificationResponseDto;
import xyz.suchdoge.webapi.model.notification.Notification;

public interface NotificationMapper {
    NotificationResponseDto notificationToNotificationResponseDto(Notification notification);
}
