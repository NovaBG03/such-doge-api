package xyz.suchdoge.webapi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.suchdoge.webapi.dto.notification.NotificationResponseDto;
import xyz.suchdoge.webapi.mapper.notification.NotificationMapper;
import xyz.suchdoge.webapi.model.notification.Notification;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.repository.NotificationRepository;
import xyz.suchdoge.webapi.service.validator.ModelValidatorService;
import xyz.suchdoge.webapi.websocket.WebSocketService;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Service for managing user account notifications
 * @author Nikita
 */
@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final WebSocketService webSocketService;
    private final DogeUserService userService;
    private final ModelValidatorService modelValidatorService;

    /**
     * Constructs new instance with needed dependencies.
     */
    public NotificationService(NotificationRepository notificationRepository,
                               NotificationMapper notificationMapper,
                               WebSocketService webSocketService,
                               DogeUserService userService, ModelValidatorService modelValidatorService) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.webSocketService = webSocketService;
        this.userService = userService;
        this.modelValidatorService = modelValidatorService;
    }

    /**
     * Saves notification to the database and sends it to a specific user.
     * @param notification notification to be sent.
     * @param user user to receive the notification.
     */
    @Transactional
    public void pushNotificationTo(Notification notification, DogeUser user) {
        // link the notification to the specified user
        notification.setUser(user);

        // validate notification and save it to the database
        modelValidatorService.validate(notification);
        final Notification savedNotification = this.notificationRepository.save(notification);

        // map notification to dto
        final NotificationResponseDto notificationDto = notificationMapper
                .notificationToNotificationResponseDto(savedNotification);

        // send notification to live user connections
        webSocketService.sendTo(user.getUsername(), "/queue/notification", notificationDto);
    }

    /**
     * Retrieves all notifications related to a specific user from the database.
     * @param username username of the user to search notifications for.
     * @return collection of all notifications related to the specified user
     */
    public Collection<Notification> getAllNotifications(String username) {
        return this.notificationRepository.findAllByUserUsername(username);
    }

    /**
     * Deletes all notifications associated with the specified user.
     * @param ids ids of the notifications to be deleted.
     * @param username username of the user to delete notifications for.
     */
    @Transactional
    public void deleteAll(Collection<Long> ids, String username) {
        // retrieve user from database
        DogeUser user = this.userService.getUserByUsername(username);

        // get only notifications related to the specified user
        Collection<Notification> notificationsToDelete = user.getNotifications()
                .stream()
                .filter(notification -> ids.contains(notification.getId()))
                .collect(Collectors.toList());

        // delete notifications from the database
        this.notificationRepository.deleteAll(notificationsToDelete);
    }
}
