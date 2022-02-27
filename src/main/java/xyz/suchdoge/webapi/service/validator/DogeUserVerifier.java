package xyz.suchdoge.webapi.service.validator;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.repository.DogeUserRepository;

/**
 * Service for validating user information
 * @author Nikita
 */
@Service
public class DogeUserVerifier {
    // TODO maybe move to env variables
    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final int PASSWORD_MAX_LENGTH = 50;

    private final DogeUserRepository dogeUserRepository;
    private final EmailVerifier emailVerifier;

    @Value("${BLOCK_IO_APP_WALLET_LABEL}")
    private String appWalletLabel;

    /**
     * Constructs a UserVerifier with needed dependencies
     */
    public DogeUserVerifier(DogeUserRepository dogeUserRepository, EmailVerifier emailVerifier) {
        this.dogeUserRepository = dogeUserRepository;
        this.emailVerifier = emailVerifier;
    }

    /**
     * Verify that provided username is valid
     * @param username the username to validate.
     * @throws DogeHttpException DOGE_USER_USERNAME_INVALID if is not valid username
     * @throws DogeHttpException DOGE_USER_USERNAME_EXISTS if account with this username already exists
     */
    public void verifyUsername(String username) throws DogeHttpException {
        // todo add more validations
        if (username == null || username.isBlank()) {
            throw new DogeHttpException("DOGE_USER_USERNAME_INVALID", HttpStatus.BAD_REQUEST);
        }

        if (dogeUserRepository.existsByUsername(username.trim())) {
            throw new DogeHttpException("DOGE_USER_USERNAME_EXISTS", HttpStatus.BAD_REQUEST);
        }

        if (username.trim().equals(this.appWalletLabel)) {
            throw new DogeHttpException("DOGE_USER_USERNAME_INVALID", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Verify that provided email is valid.
     * @param email the email to validate.
     * @throws DogeHttpException DOGE_USER_EMAIL_INVALID if is not valid email.
     * @throws DogeHttpException DOGE_USER_EMAIL_EXISTS if account with this email already exists.
     */
    public void verifyEmail(String email) throws DogeHttpException {
        if (!emailVerifier.isValidEmail(email)) {
            throw new DogeHttpException("DOGE_USER_EMAIL_INVALID", HttpStatus.BAD_REQUEST);
        }

        if (dogeUserRepository.existsByEmail(email.trim())) {
            throw new DogeHttpException("DOGE_USER_EMAIL_EXISTS", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Verify that provided password is valid
     * @param password the password to validate.
     * @throws DogeHttpException DOGE_USER_PASSWORD_NULL if password is null or empty
     * @throws DogeHttpException DOGE_USER_PASSWORD_TOO_SHORT if password is less than {@value PASSWORD_MIN_LENGTH} characters
     * @throws DogeHttpException DOGE_USER_PASSWORD_TOO_LONG if password is more than {@value PASSWORD_MAX_LENGTH} characters
     * @throws DogeHttpException DOGE_USER_PASSWORD_NO_DIGITS if password contains no digits
     * @throws DogeHttpException DOGE_USER_PASSWORD_NO_ALPHABETIC_CHARACTERS if password contains no alphabetic characters
     */
    public void verifyPassword(String password) throws DogeHttpException {
        if (Strings.isNullOrEmpty(password)) {
            throw new DogeHttpException("DOGE_USER_PASSWORD_NULL", HttpStatus.BAD_REQUEST);
        }

        if (password.length() < PASSWORD_MIN_LENGTH) {
            throw new DogeHttpException("DOGE_USER_PASSWORD_TOO_SHORT", HttpStatus.BAD_REQUEST);
        }

        if (password.length() > PASSWORD_MAX_LENGTH) {
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

    /**
     * Verify that provided public key is valid
     * @param publicKey the public key to validate.
     * @throws DogeHttpException DOGE_USER_PUBLIC_KEY_INVALID if public key is invalid
     */
    public void verifyDogePublicKey(String publicKey) throws DogeHttpException {
        // todo add validation
//        if (publicKey == null) {
//            throw new DogeHttpException("DOGE_USER_PUBLIC_KEY_INVALID", HttpStatus.BAD_REQUEST);
//        }
    }
}
