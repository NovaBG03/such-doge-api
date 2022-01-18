package xyz.suchdoge.webapi.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import xyz.suchdoge.webapi.model.user.DogeUser;

import java.util.Collection;
import java.util.stream.Collectors;

public class DogeUserDetails implements UserDetails {
    private final DogeUser dogeUser;

    public DogeUserDetails(DogeUser dogeUser) {
        this.dogeUser = dogeUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return dogeUser.getRoles()
                .stream()
                .flatMap(role -> role.getLevel().getAuthorities().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return dogeUser.getEncodedPassword();
    }

    @Override
    public String getUsername() {
        return dogeUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
