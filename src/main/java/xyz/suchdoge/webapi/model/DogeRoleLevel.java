package xyz.suchdoge.webapi.model;

import com.google.common.collect.Sets;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;

public enum DogeRoleLevel {
    NOT_CONFIRMED_USER("ROLE_NOT_CONFIRMED_USER"),
    USER("ROLE_USER"),
    MODERATOR("ROLE_MODERATOR"),
    ADMIN("ROLE_ADMIN");

    private final String level;

    DogeRoleLevel(String level) {
        this.level = level;
    }

    public Set<GrantedAuthority> getAuthorities() {
        return Sets.newHashSet(new SimpleGrantedAuthority(level));
    }
}
