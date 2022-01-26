package xyz.suchdoge.webapi.dto.notification;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class NotificationIdList {
    private Collection<Long> notificationIds;
}
