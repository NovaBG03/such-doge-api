package xyz.suchdoge.webapi.service.validator;

import com.google.common.base.Strings;
import org.springframework.stereotype.Service;
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
            throw new RuntimeException("DOGE_USER_USERNAME_EXISTS");
        }
    }

    public void verifyEmail(String email) {
        if (!emailVerifier.isValidEmail(email)) {
            throw new RuntimeException("DOGE_USER_EMAIL_INVALID");
        }

        if (dogeUserRepository.existsByEmail(email)) {
            throw new RuntimeException("DOGE_USER_EMAIL_EXISTS");
        }
    }

    public void verifyPassword(String password) {
        int minLength = 6;
        int maxLength = 50;

        if (Strings.isNullOrEmpty(password)) {
            throw new RuntimeException("DOGE_USER_PASSWORD_NULL");
        }

        if (password.length() < minLength) {
            throw new RuntimeException("DOGE_USER_PASSWORD_TOO_SHORT");
        }

        if (password.length() > maxLength) {
            throw new RuntimeException("DOGE_USER_PASSWORD_TOO_LONG");
        }

        boolean hasDigits = password.chars()
                .anyMatch(Character::isDigit);
        if (!hasDigits) {
            throw new RuntimeException("DOGE_USER_PASSWORD_NO_DIGITS");
        }

        boolean hasAlphabeticCharacters = password.chars()
                .anyMatch(Character::isAlphabetic);
        if (!hasAlphabeticCharacters) {
            throw new RuntimeException("DOGE_USER_PASSWORD_NO_ALPHABETIC_CHARACTERS");
        }
    }
}
