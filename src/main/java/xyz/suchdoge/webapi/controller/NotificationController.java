package xyz.suchdoge.webapi.controller;

import org.springframework.web.bind.annotation.*;
import xyz.suchdoge.webapi.dto.notification.NotificationIdList;
import xyz.suchdoge.webapi.dto.notification.NotificationList;
import xyz.suchdoge.webapi.mapper.notification.NotificationMapper;
import xyz.suchdoge.webapi.service.NotificationService;

import java.security.Principal;
import java.util.stream.Collectors;

/**
 * Notification controller.
 *
 * @author Nikita
 */
@RestController
@RequestMapping("/api/v1/notification")
public class NotificationController {
    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    public NotificationController(NotificationService notificationService, NotificationMapper notificationMapper) {
        this.notificationService = notificationService;
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
}
