package xyz.suchdoge.webapi.model.user;

import com.google.common.collect.Sets;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;
import xyz.suchdoge.webapi.model.Meme;
import xyz.suchdoge.webapi.model.notification.Notification;
import xyz.suchdoge.webapi.model.token.EmailConfirmationToken;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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

    /**
     * Add a new role.
     * @param role role to be added.
     */
    public void addRole(DogeRole role) {
        if (this.roles.stream().noneMatch(r -> r.equals(role))) {
            this.roles.add(role);
        }
    }

    /**
     * Add multiple roles.
     * @param roles roles to be added.
     */
    public void addRoles(Collection<DogeRole> roles) {
        this.roles.addAll(roles);
    }

    /**
     * Remove specific role by role level if exists.
     * @param roleLevel role level to be removed.
     */
    public void removeRole(DogeRoleLevel roleLevel) {
        this.roles.removeIf(dogeRole -> dogeRole.getLevel().equals(roleLevel));
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DogeUser user = (DogeUser) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
