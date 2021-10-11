package xyz.suchdoge.webapi.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.model.DogeRole;
import xyz.suchdoge.webapi.model.DogeRoleLevel;
import xyz.suchdoge.webapi.model.DogeUser;
import xyz.suchdoge.webapi.repository.DogeRoleRepository;
import xyz.suchdoge.webapi.repository.DogeUserRepository;
import xyz.suchdoge.webapi.security.DogeUserDetails;
import xyz.suchdoge.webapi.service.register.ConfirmationTokenService;
import xyz.suchdoge.webapi.service.validator.DogeUserVerifier;
import xyz.suchdoge.webapi.service.validator.ModelValidatorService;

@Service
public class DogeUserService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final DogeUserRepository dogeUserRepository;
    private final DogeRoleRepository dogeRoleRepository;
    private final DogeUserVerifier dogeUserVerifier;
    private final ModelValidatorService modelValidatorService;
    private final ConfirmationTokenService confirmationTokenService;

    public DogeUserService(PasswordEncoder passwordEncoder,
                           DogeUserRepository dogeUserRepository,
                           DogeRoleRepository dogeRoleRepository,
                           DogeUserVerifier dogeUserVerifier,
                           ModelValidatorService modelValidatorService,
                           ConfirmationTokenService confirmationTokenService) {
        this.passwordEncoder = passwordEncoder;
        this.dogeUserRepository = dogeUserRepository;
        this.dogeRoleRepository = dogeRoleRepository;
        this.dogeUserVerifier = dogeUserVerifier;
        this.modelValidatorService = modelValidatorService;
        this.confirmationTokenService = confirmationTokenService;
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

        final DogeUser user = DogeUser.builder()
                .username(username)
                .email(email)
                .encodedPassword(passwordEncoder.encode(password))
                .build();

        modelValidatorService.validate(user);

        DogeRole userRole = this.dogeRoleRepository.getByLevel(DogeRoleLevel.USER);
        user.addRole(userRole);

        return dogeUserRepository.save(user);
    }
}
