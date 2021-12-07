package xyz.suchdoge.webapi.service;

import org.springframework.context.ApplicationEventPublisher;
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
import xyz.suchdoge.webapi.service.register.event.OnEmailConfirmationNeededEvent;
import xyz.suchdoge.webapi.service.validator.DogeUserVerifier;
import xyz.suchdoge.webapi.service.validator.ModelValidatorService;

@Service
public class DogeUserService implements UserDetailsService {
    private final PasswordEncoder passwordEncoder;
    private final DogeUserRepository dogeUserRepository;
    private final DogeRoleRepository dogeRoleRepository;
    private final DogeUserVerifier dogeUserVerifier;
    private final ModelValidatorService modelValidatorService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public DogeUserService(PasswordEncoder passwordEncoder,
                           DogeUserRepository dogeUserRepository,
                           DogeRoleRepository dogeRoleRepository,
                           DogeUserVerifier dogeUserVerifier,
                           ModelValidatorService modelValidatorService,
                           ApplicationEventPublisher applicationEventPublisher) {
        this.passwordEncoder = passwordEncoder;
        this.dogeUserRepository = dogeUserRepository;
        this.dogeRoleRepository = dogeRoleRepository;
        this.dogeUserVerifier = dogeUserVerifier;
        this.modelValidatorService = modelValidatorService;
        this.applicationEventPublisher = applicationEventPublisher;
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

        DogeRole userRole = this.dogeRoleRepository.getByLevel(DogeRoleLevel.NOT_CONFIRMED_USER);
        user.addRole(userRole);

        return dogeUserRepository.save(user);
    }

    public DogeUser updateUserInfo(String email, String publicKey, String username) {
        final DogeUser user = this.getUserByUsername(username);

        boolean isEmailChanged = false;
        if (email != null && !email.trim().isBlank() && !email.equals(user.getEmail())) {
            this.dogeUserVerifier.verifyEmail(email.trim());
            user.setEmail(email.trim());
            user.getRoles().removeIf(dogeRole -> dogeRole.getLevel().equals(DogeRoleLevel.USER));
            user.addRole(this.dogeRoleRepository.getByLevel(DogeRoleLevel.NOT_CONFIRMED_USER));
            isEmailChanged = true;
        }

        if (publicKey != null) {
            user.setDogePublicKey(publicKey.trim());
        }

        this.modelValidatorService.validate(user);
        final DogeUser updatedUser = dogeUserRepository.save(user);

        if (isEmailChanged) {
            this.applicationEventPublisher.publishEvent(new OnEmailConfirmationNeededEvent(this, updatedUser));
        }

        return updatedUser;
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
