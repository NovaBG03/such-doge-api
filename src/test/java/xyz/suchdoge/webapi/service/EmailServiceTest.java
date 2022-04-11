package xyz.suchdoge.webapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.service.register.RegisterProps;
import xyz.suchdoge.webapi.service.validator.EmailVerifier;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    static {
        System.setProperty("spring.mail.username", "suchdoge@test.com");
    }

    @Mock
    JavaMailSender mailSender;
    @Mock
    EmailVerifier emailVerifier;
    @Mock
    RegisterProps registerProps;

    EmailService emailService;

    @Value("${spring.mail.username}")
    String businessEmail;

    String email = "test@dev.com";
    String subject = "test mail";
    String content = "mail content";

    @BeforeEach
    void initEmailService() {
        emailService = new EmailService(mailSender, emailVerifier, registerProps);
    }

    @Test
    @DisplayName("Should send email successfully")
    void shouldSendEmailSuccessfully() {
        when(emailVerifier.isValidEmail(email)).thenReturn(true);

        emailService.sendEmail(email, subject, content);

        verify(emailVerifier, times(1)).isValidEmail(email);

        ArgumentCaptor<SimpleMailMessage> simpleMailMessageArgumentCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(simpleMailMessageArgumentCaptor.capture());
        SimpleMailMessage message = simpleMailMessageArgumentCaptor.getValue();
        assertEquals(message.getFrom(), businessEmail);
        assertNotNull(message.getTo());
        assertTrue(Arrays.stream(message.getTo()).anyMatch(x -> x.equals(email)));
        assertEquals(message.getSubject(), subject);
        assertEquals(message.getText(), content);
    }

    @Test
    @DisplayName("Should throw exception when email is not valid")
    void shouldThrowExceptionWhenEmailIsNotValid() {
        when(emailVerifier.isValidEmail(email)).thenReturn(false);

        DogeHttpException exception = assertThrows(DogeHttpException.class, () -> emailService.sendEmail(email, subject, content));
        assertEquals(exception.getMessage(), "SENDING_EMAIL_INVALID");
    }

    @Test
    @DisplayName("Should throw exception when can not sent message")
    void shouldThrowExceptionWhenCanNotSentMessage() {
        when(emailVerifier.isValidEmail(email)).thenReturn(true);
        doThrow(new RuntimeException()).when(mailSender).send(any(SimpleMailMessage.class));

        DogeHttpException exception = assertThrows(DogeHttpException.class, () -> emailService.sendEmail(email, subject, content));
        assertEquals(exception.getMessage(), "CAN_NOT_SEND");
    }


    @Test
    @DisplayName("Should send confirmation token to user email successfully")
    void shouldSendConfirmationTokenToUserEmailSuccessfully() {
        String email = "user@dev.com";
        DogeUser user = DogeUser.builder().email(email).build();
        String confirmationToken = "confirmation token";

        when(emailVerifier.isValidEmail(email)).thenReturn(true);

        emailService.sendToken(user, confirmationToken);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}