package xyz.suchdoge.webapi.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import xyz.suchdoge.webapi.dto.user.RegisterUserDto;
import xyz.suchdoge.webapi.mapper.UserMapper;
import xyz.suchdoge.webapi.model.DogeUser;
import xyz.suchdoge.webapi.service.DogeUserService;
import xyz.suchdoge.webapi.service.RegisterService;

@RestController
public class DogeUserController {
    private final RegisterService registerService;
    private final DogeUserService dogeUserService;
    private final UserMapper userMapper;

    public DogeUserController(DogeUserService dogeUserService, RegisterService registerService) {
        this.dogeUserService = dogeUserService;
        this.registerService = registerService;
        this.userMapper = UserMapper.INSTANCE;
    }


    @PostMapping("/register")
    public void register(@RequestBody RegisterUserDto registerUserDto) {
        DogeUser registeredUser = this.registerService.registerUser(
                registerUserDto.getUsername(),
                registerUserDto.getEmail(),
                registerUserDto.getPassword()
        );
    }
}
