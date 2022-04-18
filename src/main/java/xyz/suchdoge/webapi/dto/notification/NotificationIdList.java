package xyz.suchdoge.webapi.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationIdList {
    private Collection<Long> notificationIds;
}
