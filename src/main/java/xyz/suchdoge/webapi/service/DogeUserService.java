package xyz.suchdoge.webapi.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.model.DogeUser;
import xyz.suchdoge.webapi.model.DogeUserRole;
import xyz.suchdoge.webapi.repository.DogeUserRepository;
import xyz.suchdoge.webapi.security.DogeUserDetails;
import xyz.suchdoge.webapi.service.validator.DogeUserVerifier;
import xyz.suchdoge.webapi.service.validator.ModelValidatorService;

@Service
public class DogeUserService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final DogeUserRepository dogeUserRepository;
    private final DogeUserVerifier dogeUserVerifier;
    private final ModelValidatorService modelValidatorService;

    public DogeUserService(PasswordEncoder passwordEncoder,
                           DogeUserRepository dogeUserRepository,
                           DogeUserVerifier dogeUserVerifier,
                           ModelValidatorService modelValidatorService) {
        this.passwordEncoder = passwordEncoder;
        this.dogeUserRepository = dogeUserRepository;
        this.dogeUserVerifier = dogeUserVerifier;
        this.modelValidatorService = modelValidatorService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new DogeUserDetails(getUserByUsername(username));
    }

    public DogeUser getUserByUsername(String username) {
        return dogeUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    public DogeUser createUser(String username, String email, String password) {
        dogeUserVerifier.verifyUsername(username);
        dogeUserVerifier.verifyEmail(email);
        dogeUserVerifier.verifyPassword(password);

        DogeUser user = DogeUser.builder()
                .username(username)
                .email(email)
                .encodedPassword(passwordEncoder.encode(password))
                .role(DogeUserRole.USER)
                .build();

        modelValidatorService.validate(user);
        return dogeUserRepository.save(user);
    }
}
