package xyz.suchdoge.webapi.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.suchdoge.webapi.dto.user.*;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.mapper.user.UserMapper;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.service.DogeUserService;
import xyz.suchdoge.webapi.service.jwt.RefreshTokenService;
import xyz.suchdoge.webapi.service.register.RegisterService;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;

@RestController
public class DogeUserController {
    private final RefreshTokenService refreshTokenService;
    private final RegisterService registerService;
    private final DogeUserService dogeUserService;
    private final UserMapper userMapper;

    public DogeUserController(RefreshTokenService refreshTokenService,
                              RegisterService registerService,
                              DogeUserService dogeUserService,
                              UserMapper userMapper) {
        this.refreshTokenService = refreshTokenService;
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
                userDto.getUsername().trim(),
                userDto.getEmail().trim(),
                userDto.getPassword()
        );
    }

    @PostMapping("/requestActivation")
    public RequestActivationResponseDto requestActivation(Principal principal) {
        return new RequestActivationResponseDto(
                this.registerService.resentActivationLink(principal.getName()));
    }

    @PostMapping("/activate/{token}")
    public void activate(@PathVariable String token) {
        this.registerService.activateUser(token);
    }

    @PostMapping("/refresh/{token}")
    public void refresh(@PathVariable String token, HttpServletResponse response) {
        this.refreshTokenService.refreshAccess(token, response);
    }

    @PatchMapping("/me")
    public UserInfoPatchResponseDto patchPrincipalInfo(@RequestBody UserInfoDto userInfoDto, Principal principal) {
        DogeUser user = this.dogeUserService.getUserByUsername(principal.getName());
        Collection<DogeHttpException> exceptions = new ArrayList<>();

        try {
            if (userInfoDto.getEmail() != null) {
                user = this.dogeUserService.changeUserEmail(userInfoDto.getEmail(), user);
            }
        } catch (DogeHttpException e) {
            exceptions.add(e);
        }

        // todo refactor to only change email
//        try {
//            if (userInfoDto.getPublicKey() != null) {
//                user = this.dogeUserService.changeDogePublicKey(userInfoDto.getPublicKey(), user);
//            }
//        } catch (DogeHttpException e) {
//            exceptions.add(e);
//        }

        return this.userMapper.dogeUserToUserInfoPatchResponseDto(user, exceptions);
    }

    @PostMapping("/me/password")
    public void changePassword(@RequestBody PasswordDto passwordDto, Principal principal) {
        this.dogeUserService.changePassword(passwordDto.getOldPassword(),
                passwordDto.getNewPassword(),
                passwordDto.getConfirmPassword(),
                principal.getName());
    }

    @PostMapping("me/image")
    public void updateProfilePic(@RequestParam MultipartFile image, Principal principal) {
        this.dogeUserService.setProfileImage(image, principal.getName());
    }
}
