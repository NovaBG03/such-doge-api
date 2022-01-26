package xyz.suchdoge.webapi.model.user;

import com.google.common.collect.Sets;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import xyz.suchdoge.webapi.model.Meme;
import xyz.suchdoge.webapi.model.notification.Notification;
import xyz.suchdoge.webapi.model.token.EmailConfirmationToken;
import xyz.suchdoge.webapi.model.user.DogeRole;
import xyz.suchdoge.webapi.model.user.DogeRoleLevel;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DogeUser {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(nullable = false, updatable = false)
    @Type(type="org.hibernate.type.UUIDCharType")
    private UUID id;

    @NotNull(message = "DOGE_USER_USERNAME_NULL")
    @Length.List({
            @Length(min = 3, message = "DOGE_USER_USERNAME_TOO_SHORT"),
            @Length(max = 36, message = "DOGE_USER_USERNAME_TOO_LONG")
    })
    @Column(unique = true)
    private String username;

    @NotNull(message = "DOGE_USER_EMAIL_NULL")
    @Length.List({
            @Length(min = 3, message = "DOGE_USER_EMAIL_TOO_SHORT"),
            @Length(max = 254, message = "DOGE_USER_EMAIL_TOO_LONG")
    })
    @Column(unique = true)
    private String email;

    @NotNull(message = "DOGE_USER_PASSWORD_NULL")
    @Column()
    private String encodedPassword;

    @Builder.Default
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.DETACH}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Collection<DogeRole> roles = Sets.newHashSet();

    // TODO add validation for doge public key
    @Column()
    private String dogePublicKey;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private Collection<EmailConfirmationToken> emailConfirmationTokens;

    @Builder.Default
    @OneToMany(mappedBy = "publisher")
    private Collection<Meme> memes = Sets.newHashSet();

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private Collection<Notification> notifications = Sets.newHashSet();

    public void addRole(DogeRole role) {
        this.roles.add(role);
    }

    public void addRoles(Collection<DogeRole> roles) {
        this.roles.addAll(roles);
    }

    public boolean isConfirmed() {
        return !this.hasAuthority(DogeRoleLevel.NOT_CONFIRMED_USER);
    }

    public boolean hasAuthority(DogeRoleLevel roleLevel) {
        return this.roles
                .stream()
                .anyMatch(role -> role.getLevel().equals(roleLevel));
    }

    public boolean isAdminOrModerator() {
        return this.hasAuthority(DogeRoleLevel.MODERATOR) || this.hasAuthority(DogeRoleLevel.ADMIN);
    }
}
