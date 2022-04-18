package xyz.suchdoge.webapi.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.suchdoge.webapi.dto.user.request.EmailDto;
import xyz.suchdoge.webapi.dto.user.request.PasswordDto;
import xyz.suchdoge.webapi.dto.user.request.UserRegisterDto;
import xyz.suchdoge.webapi.dto.user.response.AchievementsListResponseDto;
import xyz.suchdoge.webapi.dto.user.response.RequestActivationResponseDto;
import xyz.suchdoge.webapi.dto.user.response.UserInfoResponseDto;
import xyz.suchdoge.webapi.mapper.user.UserMapper;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.service.AchievementsService;
import xyz.suchdoge.webapi.service.DogeUserService;
import xyz.suchdoge.webapi.service.jwt.RefreshTokenService;
import xyz.suchdoge.webapi.service.register.RegisterService;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;

/**
 * User controller.
 *
 * @author Nikita.
 */
@RestController
@RequestMapping("/api/v1")
public class DogeUserController {
    private final RefreshTokenService refreshTokenService;
    private final RegisterService registerService;
    private final DogeUserService dogeUserService;
    private final AchievementsService achievementsService;
    private final UserMapper userMapper;

    /**
     * Constructs new instance with needed dependencies.
     */
    public DogeUserController(RefreshTokenService refreshTokenService,
                              RegisterService registerService,
                              DogeUserService dogeUserService,
                              AchievementsService achievementsService,
                              UserMapper userMapper) {
        this.refreshTokenService = refreshTokenService;
        this.registerService = registerService;
        this.dogeUserService = dogeUserService;
        this.achievementsService = achievementsService;
        this.userMapper = userMapper;
    }

    @GetMapping("/me")
    public UserInfoResponseDto getPrincipalInfo(Principal principal) {
        final DogeUser user = dogeUserService.getUserByUsername(principal.getName());
        return this.userMapper.dogeUserToUserInfoResponseDto(user);
    }

    @GetMapping("/achievements/{username}")
    public AchievementsListResponseDto getAchievements(@PathVariable String username) {
        return this.achievementsService.getAchievements(username);
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
                this.registerService.resendActivationLink(principal.getName()));
    }

    @PostMapping("/activate/{token}")
    public void activate(@PathVariable String token) {
        this.registerService.activateUser(token);
    }

    @PostMapping("/refresh/{token}")
    public void refresh(HttpServletResponse response, @PathVariable String token) {
        this.refreshTokenService.refreshAccess(response, token);
    }

    @PostMapping("/me/email")
    public void changeEmail(@RequestBody EmailDto emailDto, Principal principal) {
        this.dogeUserService.changeUserEmail(emailDto.getEmail(), principal.getName());
    }

    @PostMapping("/me/password")
    public void changePassword(@RequestBody PasswordDto passwordDto, Principal principal) {
        this.dogeUserService.changePassword(passwordDto.getOldPassword(),
                passwordDto.getNewPassword(),
                passwordDto.getConfirmPassword(),
                principal.getName());
    }

    @PostMapping("/me/image")
    public void updateProfilePic(@RequestParam MultipartFile image, Principal principal) {
        this.dogeUserService.setProfileImage(image, principal.getName());
    }
}
