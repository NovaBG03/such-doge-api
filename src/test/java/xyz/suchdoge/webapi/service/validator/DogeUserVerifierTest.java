package xyz.suchdoge.webapi.service.validator;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.repository.DogeUserRepository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DogeUserVerifierTest {
    static {
        System.setProperty("application.blockchain.doge.app-wallet-label", "default");
    }

    @Mock
    DogeUserRepository userRepository;
    EmailVerifier emailVerifier = new EmailVerifier();

    DogeUserVerifier userVerifier;
    String appWalletLabel = "default";

    @BeforeEach
    void initUserVerifier() {
        userVerifier = new DogeUserVerifier(userRepository, emailVerifier, appWalletLabel);
    }

    @ParameterizedTest
    @DisplayName("Should verify that username is valid")
    @ValueSource(strings = {"ivan", "nova", " test  ", "Ivan124", "!@#$%^&*()_+{}|:;,./<?><"})
    void shouldVerifyThatUsernameIsValid(String username) {
        when(userRepository.existsByUsername(username.trim())).thenReturn(false);
        userVerifier.verifyUsername(username);
    }

    @ParameterizedTest
    @DisplayName("Should verify that username is not valid")
    @ValueSource(strings = {" Nova aa test", "", "default"})
    void shouldVerifyThatUsernameIsNotValid(String username) {
        assertThatThrownBy(() -> userVerifier.verifyUsername(username))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("DOGE_USER_USERNAME_INVALID");
    }

    @Test
    @DisplayName("Should verify that username is not valid when already exists")
    void shouldVerifyThatUsernameIsNotValidWhenAlreadyExists() {
        String username = "username";
        when(userRepository.existsByUsername(username.trim())).thenReturn(true);
        assertThatThrownBy(() -> userVerifier.verifyUsername(username))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("DOGE_USER_USERNAME_EXISTS");
    }

    @Test
    @DisplayName("Should verify that email is valid")
    void shouldVerifyThatEmailIsValid() {
        String email = "test@dev.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        userVerifier.verifyEmail(email);
        verify(userRepository).existsByEmail(email);
    }

    @ParameterizedTest
    @DisplayName("Should verify that email is not valid")
    @ValueSource(strings = {"test@bg", "@gmail.com", "", " ", "cool-mail.com"})
    void shouldVerifyThatEmailIsNotValid(String email) {
        assertThatThrownBy(() -> userVerifier.verifyEmail(email))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("DOGE_USER_EMAIL_INVALID");
    }

    @Test
    @DisplayName("Should verify that email exists and is not valid")
    void shouldVerifyThatEmailExistsAndIsNotValid() {
        String email = "test@dev.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThatThrownBy(() -> userVerifier.verifyEmail(email))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("DOGE_USER_EMAIL_EXISTS");
    }

    @ParameterizedTest
    @DisplayName("Should verify that password is valid")
    @ValueSource(strings = {"a12332", "#@!p1@#", "sadasdmak213 qW QQEQ 1§sad ", "   1  d ", "??asdad1"})
    void shouldVerifyThatPasswordIsValid(String password) {
        userVerifier.verifyPassword(password);
    }

    @Test
    @DisplayName("Should verify that password is invalid when null")
    void shouldVerifyThatPasswordIsInvalidWhenNull() {
        assertThatThrownBy(() -> userVerifier.verifyPassword(null))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("DOGE_USER_PASSWORD_NULL");
    }

    @Test
    @DisplayName("Should verify that password is invalid when too short")
    void shouldVerifyThatPasswordIsInvalidWhenTooShort() {
        String password = "a";
        assertThatThrownBy(() -> userVerifier.verifyPassword(password))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("DOGE_USER_PASSWORD_TOO_SHORT");
    }

    @Test
    @DisplayName("Should verify that password is invalid when too long")
    void shouldVerifyThatPasswordIsInvalidWhenTooLong() {
        String password = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        assertThatThrownBy(() -> userVerifier.verifyPassword(password))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("DOGE_USER_PASSWORD_TOO_LONG");
    }

    @Test
    @DisplayName("Should verify that password is invalid when no digit")
    void shouldVerifyThatPasswordIsInvalidWhenNoDigit() {
        String password = "testtest";
        assertThatThrownBy(() -> userVerifier.verifyPassword(password))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("DOGE_USER_PASSWORD_NO_DIGITS");
    }

    @Test
    @DisplayName("Should verify that password is invalid when no alphabetic characters")
    void shouldVerifyThatPasswordIsInvalidWhenNoAlphabeticCharacters() {
        String password = "123456";
        assertThatThrownBy(() -> userVerifier.verifyPassword(password))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("DOGE_USER_PASSWORD_NO_ALPHABETIC_CHARACTERS");
    }
}