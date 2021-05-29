package xyz.suchdoge.webapi.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import xyz.suchdoge.webapi.model.DogeUser;

import java.util.Collection;

public class DogeUserDetails implements UserDetails {
    private final DogeUser dogeUser;

    public DogeUserDetails(DogeUser dogeUser) {
        this.dogeUser = dogeUser;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return dogeUser.getRole().getAuthorities();
    }

    @Override
    public String getPassword() {
        return dogeUser.getPassword();
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
        return dogeUser.isEnabled();
    }
}
