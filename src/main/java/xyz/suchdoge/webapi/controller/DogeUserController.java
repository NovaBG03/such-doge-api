package xyz.suchdoge.webapi.controller;

import org.springframework.web.bind.annotation.*;
import xyz.suchdoge.webapi.dto.user.PasswordDto;
import xyz.suchdoge.webapi.dto.user.UserInfoDto;
import xyz.suchdoge.webapi.dto.user.UserInfoResponseDto;
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
    public UserInfoResponseDto getPrincipalInfo(Principal principal) {
        final DogeUser user = dogeUserService.getUserByUsername(principal.getName());
        return this.userMapper.dogeUserToUserInfoResponseDto(user);
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

    @PatchMapping("/me")
    public UserInfoResponseDto updatePrincipalInfo(@RequestBody UserInfoDto userInfoDto, Principal principal) {
        final DogeUser user = this.dogeUserService
                .updateUserInfo(userInfoDto.getEmail(), userInfoDto.getPublicKey(), principal.getName());

        return this.userMapper.dogeUserToUserInfoResponseDto(user);
    }

    @PostMapping("/me/password")
    public void changePassword(@RequestBody PasswordDto passwordDto, Principal principal) {
        this.dogeUserService.changePassword(passwordDto.getOldPassword(),
                passwordDto.getNewPassword(),
                passwordDto.getConfirmPassword(),
                principal.getName());
    }
}
