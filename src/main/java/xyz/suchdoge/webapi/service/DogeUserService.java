package xyz.suchdoge.webapi.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.model.DogeUser;
import xyz.suchdoge.webapi.repository.DogeUserRepository;
import xyz.suchdoge.webapi.security.DogeUserDetails;

@Service
public class DogeUserService implements UserDetailsService {
    private final DogeUserRepository dogeUserRepository;

    public DogeUserService(DogeUserRepository dogeUserRepository) {
        this.dogeUserRepository = dogeUserRepository;
    }

    public DogeUser getUserByUsername(String username) {
        return dogeUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User with username %s not found", username)));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new DogeUserDetails(getUserByUsername(username));
    }
}
