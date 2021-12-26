package xyz.suchdoge.webapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.token.EmailConfirmationToken;
import xyz.suchdoge.webapi.model.DogeUser;
import xyz.suchdoge.webapi.service.register.RegisterConfig;
import xyz.suchdoge.webapi.service.validator.EmailVerifier;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final EmailVerifier emailVerifier;
    private final RegisterConfig registerConfig;

    @Value("${spring.mail.username}")
    private String businessEmail;

    public EmailService(JavaMailSender mailSender, EmailVerifier emailVerifier, RegisterConfig registerConfig) {
        this.mailSender = mailSender;
        this.emailVerifier = emailVerifier;
        this.registerConfig = registerConfig;
    }

    public void sendToken(DogeUser user, String confirmationToken) {
        final String registerUrl = registerConfig.tokenActivationWebUrl + "/" + confirmationToken;
        final String subject = "Activate your SuchDoge account";
        final String content = "To activate your SuchDoge account click here: " + registerUrl;

        this.sendEmail(user.getEmail(), subject, content);
    }

    public void sendEmail(String email, String subject, String content) {
        if (!this.emailVerifier.isValidEmail(email)) {
            throw new DogeHttpException("SENDING_EMAIL_INVALID", HttpStatus.BAD_REQUEST);
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(businessEmail);
        message.setTo(email);
        message.setSubject(subject);
        message.setText(content);

        try {
            mailSender.send(message);
        } catch (MailException me) {
            throw new DogeHttpException("CAN_NOT_SEND", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
