package xyz.suchdoge.webapi.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.exception.DogeHttpException;
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

    public DogeUserService(PasswordEncoder passwordEncoder,
                           DogeUserRepository dogeUserRepository,
                           DogeRoleRepository dogeRoleRepository,
                           DogeUserVerifier dogeUserVerifier,
                           ModelValidatorService modelValidatorService) {
        this.passwordEncoder = passwordEncoder;
        this.dogeUserRepository = dogeUserRepository;
        this.dogeRoleRepository = dogeRoleRepository;
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

    public DogeUser updateUserInfo(String email, String publicKey, String username) {
        final DogeUser user = this.getUserByUsername(username);

        if (email != null && !email.isBlank()) {
            // todo add email verification
            this.dogeUserVerifier.verifyEmail(email);
            user.setEmail(email);
        }

        if (publicKey != null && !publicKey.isBlank()) {
            user.setDogePublicKey(publicKey);
        }

        this.modelValidatorService.validate(user);
        return dogeUserRepository.save(user);
    }

    public void changePassword(String oldPassword, String newPassword, String confirmPassword, String username) {
        final DogeUser user = this.getUserByUsername(username);

        if (!newPassword.equals(confirmPassword)) {
            throw new DogeHttpException("PASSWORDS_DOES_NOT_MATCH", HttpStatus.BAD_REQUEST);
        }

        if (!passwordEncoder.matches(oldPassword, user.getEncodedPassword())) {
            throw new DogeHttpException("WRONG_OLD_PASSWORD", HttpStatus.BAD_REQUEST);
        }

        if (passwordEncoder.matches(newPassword, user.getEncodedPassword())) {
            throw new DogeHttpException("NEW_PASSWORD_AND_OLD_PASSWORD_ARE_THE_SAME", HttpStatus.BAD_REQUEST);
        }

        this.dogeUserVerifier.verifyPassword(newPassword);
        user.setEncodedPassword(this.passwordEncoder.encode(newPassword));
        dogeUserRepository.save(user);
    }
}
