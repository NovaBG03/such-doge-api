package xyz.suchdoge.webapi.model;

import com.google.common.collect.Sets;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;

public enum DogeUserRole {
    USER("ROLE_USER"),
    MODERATOR("ROLE_MODERATOR"),
    ADMIN("ROLE_ADMIN");

    private final String role;

    DogeUserRole(String role) {
        this.role = role;
    }

    public Set<GrantedAuthority> getAuthorities() {
        return Sets.newHashSet(new SimpleGrantedAuthority(role));
    }
}
