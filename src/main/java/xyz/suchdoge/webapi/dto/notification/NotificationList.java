package xyz.suchdoge.webapi.dto.notification;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class NotificationList {
    private Collection<NotificationResponseDto> notifications;

    public NotificationList(Collection<NotificationResponseDto> notifications) {
        this.notifications = notifications;
    }
}
