package xyz.suchdoge.webapi.service;

import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.model.DogeUser;

@Service
public class RegisterService {
    private final DogeUserService dogeUserService;

    public RegisterService(DogeUserService dogeUserService) {
        this.dogeUserService = dogeUserService;
    }

    public DogeUser registerUser(String username, String email, String password) {

        DogeUser user = dogeUserService.createUser(username, email, password);

        // Create Activation Token

        // Send Activation Token

        return null;
    }
}
