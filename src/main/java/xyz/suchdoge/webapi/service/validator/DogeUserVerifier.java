package xyz.suchdoge.webapi.service.validator;

import com.google.common.base.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.repository.DogeUserRepository;

@Service
public class DogeUserVerifier {
    private final DogeUserRepository dogeUserRepository;
    private final EmailVerifier emailVerifier;

    public DogeUserVerifier(DogeUserRepository dogeUserRepository, EmailVerifier emailVerifier) {
        this.dogeUserRepository = dogeUserRepository;
        this.emailVerifier = emailVerifier;
    }

    public void verifyUsername(String username) {
        // todo add more validations
        if (dogeUserRepository.existsByUsername(username)) {
            throw new DogeHttpException("DOGE_USER_USERNAME_EXISTS", HttpStatus.BAD_REQUEST);
        }
    }

    public void verifyEmail(String email) {
        if (!emailVerifier.isValidEmail(email)) {
            throw new DogeHttpException("DOGE_USER_EMAIL_INVALID", HttpStatus.BAD_REQUEST);
        }

        if (dogeUserRepository.existsByEmail(email)) {
            throw new DogeHttpException("DOGE_USER_EMAIL_EXISTS", HttpStatus.BAD_REQUEST);
        }
    }

    public void verifyPassword(String password) {
        // TODO move to env variables
        int minLength = 6;
        int maxLength = 50;

        if (Strings.isNullOrEmpty(password)) {
            throw new DogeHttpException("DOGE_USER_PASSWORD_NULL", HttpStatus.BAD_REQUEST);
        }

        if (password.length() < minLength) {
            throw new DogeHttpException("DOGE_USER_PASSWORD_TOO_SHORT", HttpStatus.BAD_REQUEST);
        }

        if (password.length() > maxLength) {
            throw new DogeHttpException("DOGE_USER_PASSWORD_TOO_LONG", HttpStatus.BAD_REQUEST);
        }

        boolean hasDigits = password.chars()
                .anyMatch(Character::isDigit);
        if (!hasDigits) {
            throw new DogeHttpException("DOGE_USER_PASSWORD_NO_DIGITS", HttpStatus.BAD_REQUEST);
        }

        boolean hasAlphabeticCharacters = password.chars()
                .anyMatch(Character::isAlphabetic);
        if (!hasAlphabeticCharacters) {
            throw new DogeHttpException("DOGE_USER_PASSWORD_NO_ALPHABETIC_CHARACTERS", HttpStatus.BAD_REQUEST);
        }
    }
}
