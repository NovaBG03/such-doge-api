package xyz.suchdoge.webapi.model;

import com.google.common.collect.Sets;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
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
    @Column(columnDefinition = "VARCHAR(255)", nullable = false, updatable = false)
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
    private Set<DogeRole> roles = Sets.newHashSet();

    // TODO add validation for doge public key
    @Column()
    private String dogePublicKey;

    // TODO add validation for enabledAt
    // @FutureOrPresent
    @Column()
    private LocalDateTime enabledAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private Collection<ConfirmationToken> confirmationTokens;

    public void addRole(DogeRole role) {
        this.roles.add(role);
    }

    public void addRoles(Collection<DogeRole> roles) {
        this.roles.addAll(roles);
    }
}
