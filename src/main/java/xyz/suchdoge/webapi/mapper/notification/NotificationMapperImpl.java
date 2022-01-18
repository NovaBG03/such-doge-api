package xyz.suchdoge.webapi.mapper.notification;

import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.dto.notification.NotificationResponseDto;
import xyz.suchdoge.webapi.model.notification.Notification;

@Component
public class NotificationMapperImpl implements NotificationMapper {
    @Override
    public NotificationResponseDto notificationToNotificationResponseDto(Notification notification) {
        if (notification == null) {
            return null;
        }

        final NotificationResponseDto dto = NotificationResponseDto.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .category(notification.getCategory().name().toLowerCase())
                .build();

        return dto;
    }
}
