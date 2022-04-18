package xyz.suchdoge.webapi.mapper.notification;

import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.dto.notification.response.NotificationResponseDto;
import xyz.suchdoge.webapi.model.notification.Notification;

/**
 * Notification mapper.
 *
 * @author Nikita
 */
@Component
public class NotificationMapperImpl implements NotificationMapper {
    @Override
    public NotificationResponseDto notificationToNotificationResponseDto(Notification notification) {
        if (notification == null) {
            return null;
        }

        return NotificationResponseDto.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .category(notification.getCategory().name().toLowerCase())
                .build();
    }
}
