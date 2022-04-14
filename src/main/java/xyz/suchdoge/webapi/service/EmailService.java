package xyz.suchdoge.webapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.service.register.RegisterProps;
import xyz.suchdoge.webapi.service.validator.EmailVerifier;

/**
 * Service for sending emails.
 *
 * @author Nikita
 */
@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final EmailVerifier emailVerifier;
    private final RegisterProps registerConfig;
    private final String businessEmail;

    /**
     * Constructs new instance with needed dependencies.
     */
    public EmailService(JavaMailSender mailSender,
                        EmailVerifier emailVerifier,
                        RegisterProps registerConfig,
                        @Value("${spring.mail.username}") String businessEmail) {
        this.mailSender = mailSender;
        this.emailVerifier = emailVerifier;
        this.registerConfig = registerConfig;
        this.businessEmail = businessEmail;
    }

    /**
     * Send mail with confirmation token to a specific user.
     *
     * @param user user receiver.
     * @param confirmationToken token to send.
     */
    public void sendToken(DogeUser user, String confirmationToken) {
        final String registerUrl = registerConfig.getTokenActivationUrl() + "/" + confirmationToken;
        final String subject = "Activate your SuchDoge account";
        final String content = "To activate your SuchDoge account click here: " + registerUrl;

        this.sendEmail(user.getEmail(), subject, content);
    }

    /**
     * Send email from application business mail to a specific email.
     *
     * @param email mail receiver.
     * @param subject mail subject.
     * @param content mail content.
     * @throws DogeHttpException when email is invalid or can not send email.
     */
    public void sendEmail(String email, String subject, String content) throws DogeHttpException {
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
        } catch (Exception e) {
            throw new DogeHttpException("CAN_NOT_SEND", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
