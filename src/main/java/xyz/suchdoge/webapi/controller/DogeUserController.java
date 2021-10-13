package xyz.suchdoge.webapi.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import xyz.suchdoge.webapi.dto.user.UserRegisterDto;
import xyz.suchdoge.webapi.service.register.RegisterService;

@RestController
public class DogeUserController {
    private final RegisterService registerService;

    public DogeUserController(RegisterService registerService) {
        this.registerService = registerService;
    }


    @PostMapping("/register")
    public void register(@RequestBody UserRegisterDto userDto) {
        this.registerService.registerUser(
                userDto.getUsername(),
                userDto.getEmail(),
                userDto.getPassword()
        );
    }

    @PostMapping("/activate/{token}")
    public void activate(@PathVariable String token) {
        this.registerService.activateUser(token);
    }
}
