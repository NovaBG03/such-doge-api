package xyz.suchdoge.webapi.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.suchdoge.webapi.model.notification.Notification;
import xyz.suchdoge.webapi.model.notification.NotificationCategory;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.service.DogeUserService;
import xyz.suchdoge.webapi.service.NotificationService;

@RestController
@RequestMapping("notification")
public class NotificationController {
    private final NotificationService notificationService;
    private final DogeUserService dogeUserService;

    public NotificationController(NotificationService notificationService, DogeUserService dogeUserService) {
        this.notificationService = notificationService;
        this.dogeUserService = dogeUserService;
    }


    @PostMapping("/{username}")
    public void SendNotification(@PathVariable String username) {
        final DogeUser user = this.dogeUserService.getUserByUsername(username);

        this.notificationService.pushNotificationTo(
                Notification.builder()
                        .title("Disapproved")
                        .message("Your meme \"" + "bai ivan filma" + "\" has been rejected!")
                        .category(NotificationCategory.DANGER)
                        .build(),
                user
        );
    }
}
