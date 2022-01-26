package xyz.suchdoge.webapi.controller;

import org.springframework.web.bind.annotation.*;
import xyz.suchdoge.webapi.dto.notification.NotificationIdList;
import xyz.suchdoge.webapi.dto.notification.NotificationList;
import xyz.suchdoge.webapi.mapper.notification.NotificationMapper;
import xyz.suchdoge.webapi.model.notification.Notification;
import xyz.suchdoge.webapi.model.notification.NotificationCategory;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.service.DogeUserService;
import xyz.suchdoge.webapi.service.NotificationService;

import java.security.Principal;
import java.util.stream.Collectors;

@RestController
@RequestMapping("notification")
public class NotificationController {
    private final NotificationService notificationService;
    private final DogeUserService dogeUserService;
    private final NotificationMapper notificationMapper;

    public NotificationController(NotificationService notificationService,
                                  DogeUserService dogeUserService,
                                  NotificationMapper notificationMapper) {
        this.notificationService = notificationService;
        this.dogeUserService = dogeUserService;
        this.notificationMapper = notificationMapper;
    }

    @GetMapping
    public NotificationList getAll(Principal principal) {
        return new NotificationList(this.notificationService
                .getAllNotifications(principal.getName())
                .stream()
                .map(notificationMapper::notificationToNotificationResponseDto)
                .collect(Collectors.toList()));
    }

    @DeleteMapping
    public void closeNotifications(@RequestBody NotificationIdList idList, Principal principal) {
        this.notificationService.deleteAll(idList.getNotificationIds(), principal.getName());
    }

    @PostMapping("/{username}")
    public void SendNotification(@PathVariable String username) {
        // todo remove this
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
