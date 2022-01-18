package xyz.suchdoge.webapi.model.notification;

import lombok.*;
import org.hibernate.validator.constraints.Length;
import xyz.suchdoge.webapi.model.user.DogeUser;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long Id;

    @NotNull(message = "NOTIFICATION_TITLE_NULL")
    @Length.List({
            @Length(min = 3, message = "NOTIFICATION_TITLE_TOO_SHORT"),
            @Length(max= 30, message = "NOTIFICATION_TITLE_TOO_LONG")
    })
    private String title;

    @NotNull(message = "NOTIFICATION_MESSAGE_NULL")
    @Length.List({
            @Length(min = 3, message = "NOTIFICATION_MESSAGE_TOO_SHORT"),
            @Length(max= 150, message = "NOTIFICATION_MESSAGE_TOO_LONG")
    })
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 25)
    private NotificationCategory category;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private DogeUser user;
}