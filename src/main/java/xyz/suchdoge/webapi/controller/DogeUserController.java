package xyz.suchdoge.webapi.controller;

import org.springframework.web.bind.annotation.*;
import xyz.suchdoge.webapi.dto.user.UserInfoDto;
import xyz.suchdoge.webapi.dto.user.UserRegisterDto;
import xyz.suchdoge.webapi.mapper.user.UserMapper;
import xyz.suchdoge.webapi.model.DogeUser;
import xyz.suchdoge.webapi.service.DogeUserService;
import xyz.suchdoge.webapi.service.register.RegisterService;

import java.security.Principal;

@RestController
public class DogeUserController {
    private final RegisterService registerService;
    private final DogeUserService dogeUserService;
    private final UserMapper userMapper;

    public DogeUserController(RegisterService registerService, DogeUserService dogeUserService, UserMapper userMapper) {
        this.registerService = registerService;
        this.dogeUserService = dogeUserService;
        this.userMapper = userMapper;
    }

    @GetMapping("/me")
    public UserInfoDto getPrincipalInfo(Principal principal) {
        final DogeUser user = dogeUserService.getUserByUsername(principal.getName());
        return this.userMapper.dogeUserToUserInfoDto(user);
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
