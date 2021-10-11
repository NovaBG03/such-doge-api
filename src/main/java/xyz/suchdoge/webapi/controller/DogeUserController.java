package xyz.suchdoge.webapi.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import xyz.suchdoge.webapi.dto.user.RegisterUserDto;
import xyz.suchdoge.webapi.service.register.RegisterService;

@RestController
public class DogeUserController {
    private final RegisterService registerService;

    public DogeUserController(RegisterService registerService) {
        this.registerService = registerService;
    }


    @PostMapping("/register")
    public void register(@RequestBody RegisterUserDto registerUserDto) {
        this.registerService.registerUser(
                registerUserDto.getUsername(),
                registerUserDto.getEmail(),
                registerUserDto.getPassword()
        );
    }

    @PostMapping("/activate/{token}")
    public void activate(@PathVariable String token) {
        this.registerService.activateUser(token);
    }
}
