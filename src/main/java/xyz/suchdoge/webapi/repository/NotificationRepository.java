package xyz.suchdoge.webapi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xyz.suchdoge.webapi.model.notification.Notification;

import java.util.Collection;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, Long> {
    Collection<Notification> findAllByUserUsername(String username);
}
