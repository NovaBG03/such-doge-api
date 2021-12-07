package xyz.suchdoge.webapi.model;

import lombok.*;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DogeRole {
    @Id
    @GeneratedValue
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 25, unique = true)
    private DogeRoleLevel level;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DogeRole dogeRole = (DogeRole) o;
        return Objects.equals(id, dogeRole.id) && level == dogeRole.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, level);
    }
}
