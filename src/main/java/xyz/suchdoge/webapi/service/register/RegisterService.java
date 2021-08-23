package xyz.suchdoge.webapi.service.register;

import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.model.ConfirmationToken;
import xyz.suchdoge.webapi.model.DogeUser;
import xyz.suchdoge.webapi.service.DogeUserService;
import xyz.suchdoge.webapi.service.EmailService;

@Service
public class RegisterService {
    private final DogeUserService dogeUserService;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailService mailSenderService;

    public RegisterService(DogeUserService dogeUserService,
                           ConfirmationTokenService confirmationTokenService,
                           EmailService mailSenderService) {
        this.dogeUserService = dogeUserService;
        this.confirmationTokenService = confirmationTokenService;
        this.mailSenderService = mailSenderService;
    }

    public DogeUser registerUser(String username, String email, String password) {

        DogeUser user = dogeUserService.createUser(username, email, password);

        ConfirmationToken confirmationToken = confirmationTokenService.createToken(user);

        mailSenderService.sendToken(user, confirmationToken);

        return user;
    }
}
