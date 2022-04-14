package xyz.suchdoge.webapi.service.register;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.token.EmailConfirmationToken;
import xyz.suchdoge.webapi.model.user.DogeRole;
import xyz.suchdoge.webapi.model.user.DogeRoleLevel;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.repository.DogeRoleRepository;
import xyz.suchdoge.webapi.repository.DogeUserRepository;
import xyz.suchdoge.webapi.service.DogeUserService;
import xyz.suchdoge.webapi.service.EmailService;
import xyz.suchdoge.webapi.service.register.event.OnEmailConfirmTokenNoLongerValidEvent;
import xyz.suchdoge.webapi.service.register.event.OnEmailConfirmationNeededEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {
    @Mock
    DogeRoleRepository dogeRoleRepository;
    @Mock
    DogeUserRepository dogeUserRepository;
    @Mock
    DogeUserService dogeUserService;
    @Mock
    EmailConfirmationTokenService emailConfirmationTokenService;
    @Mock
    EmailService emailService;
    @Mock
    ApplicationEventPublisher eventPublisher;
    @Mock
    RegisterProps registerProps;

    RegisterService registerService;

    @BeforeEach
    void setUp() {
        registerService = new RegisterService(dogeRoleRepository,
                dogeUserRepository,
                dogeUserService,
                emailConfirmationTokenService,
                emailService,
                eventPublisher,
                registerProps);
    }

    @Test
    @DisplayName("Should register new user successfully")
    void shouldRegisterNewUserSuccessfully() {
        UUID id = UUID.randomUUID();
        String username = "username";
        String email = "test@dev.com";
        String password = "password";
        DogeUser user = DogeUser.builder()
                .id(id)
                .username(username)
                .email(email)
                .encodedPassword(password)
                .build();

        when(dogeUserService.createUser(username, email, password)).thenReturn(user);

        registerService.registerUser(username, email, password);

        verify(dogeUserService).createUser(username, email, password);
        ArgumentCaptor<OnEmailConfirmationNeededEvent> eventArgumentCaptor = ArgumentCaptor.forClass(OnEmailConfirmationNeededEvent.class);
        verify(eventPublisher).publishEvent(eventArgumentCaptor.capture());
        OnEmailConfirmationNeededEvent onEmailConfirmationNeededEvent = eventArgumentCaptor.getValue();
        assertThat(onEmailConfirmationNeededEvent.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("Should throw exception when can not register new user")
    void shouldThrowExceptionWhenCanNotRegisterNewUser() {
        String username = "username";
        String email = "test@dev.com";
        String password = "password";

        when(dogeUserService.createUser(username, email, password)).thenThrow(DogeHttpException.class);

        assertThatThrownBy(() -> registerService.registerUser(username, email, password))
                .isInstanceOf(DogeHttpException.class);

        verify(eventPublisher, never()).publishEvent(OnEmailConfirmationNeededEvent.class);
    }

    @Test
    @DisplayName("Should send activation link")
    void shouldSendActivationLink() {
        DogeUser user = DogeUser.builder().build();
        String confirmationToken = "confirmationtoken";
        when(emailConfirmationTokenService.createToken(user)).thenReturn(confirmationToken);

        registerService.sendActivationLink(user);

        verify(emailService).sendToken(user, confirmationToken);
    }

    @Test
    @DisplayName("Should resend activation link successfully")
    void shouldResendActivationLinkSuccessfully() {
        Long minDelaySeconds = 120L;
        String confirmationToken = "confirmationtoken";
        String username = "username";
        DogeUser user = DogeUser.builder()
                .username(username)
                .build();

        when(registerProps.getTokenMinimalDelaySeconds()).thenReturn(minDelaySeconds);
        when(dogeUserService.getUserByUsername(username)).thenReturn(user);
        when(emailConfirmationTokenService.canCreateNewToken(user)).thenReturn(true);
        when(emailConfirmationTokenService.createToken(user)).thenReturn(confirmationToken);

        Long actualMinDelaySeconds = registerService.resendActivationLink(username);

        verify(emailService).sendToken(user, confirmationToken);
        assertThat(actualMinDelaySeconds).isEqualTo(minDelaySeconds);
    }

    @Test
    @DisplayName("Should throw exception when resend activation requested too soon")
    void shouldNotResendActivationLinkWhenCanNotCreateNewToken() {
        String confirmationToken = "confirmationtoken";
        String username = "username";
        DogeUser user = DogeUser.builder()
                .username(username)
                .build();

        when(dogeUserService.getUserByUsername(username)).thenReturn(user);
        when(emailConfirmationTokenService.canCreateNewToken(user)).thenThrow(DogeHttpException.class);

        assertThatThrownBy(() -> registerService.resendActivationLink(username))
                .isInstanceOf(DogeHttpException.class);

        verify(emailService, never()).sendToken(user, confirmationToken);
    }

    @Test
    @DisplayName("Should activate user successfully")
    void shouldActivateUserSuccessfully() {
        String token = "activationtoken";
        String email = "dev@test.com";
        DogeRole userRole = DogeRole.builder().level(DogeRoleLevel.USER).build();
        DogeRole notConfirmedRole = DogeRole.builder().level(DogeRoleLevel.NOT_CONFIRMED_USER).build();
        DogeUser user = DogeUser.builder()
                .email(email)
                .roles(Lists.newArrayList(notConfirmedRole))
                .build();
        EmailConfirmationToken emailConfirmationToken = EmailConfirmationToken.builder()
                .id(1L)
                .user(user)
                .originEmail(email)
                .createdAt(LocalDateTime.now())
                .expirationTime(Duration.ofHours(2))
                .build();

        when(emailConfirmationTokenService.getConfirmationToken(token)).thenReturn(emailConfirmationToken);
        when(dogeRoleRepository.getByLevel(DogeRoleLevel.USER)).thenReturn(userRole);

        registerService.activateUser(token);

        ArgumentCaptor<DogeUser> userArgumentCaptor = ArgumentCaptor.forClass(DogeUser.class);
        verify(dogeUserRepository).save(userArgumentCaptor.capture());
        DogeUser capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser)
                .matches(x -> x.getRoles().contains(userRole), "contains user role")
                .matches(x -> !x.getRoles().contains(notConfirmedRole), "does not contains not confirmed role")
                .matches(DogeUser::isConfirmed, "is confirmed")
                .matches(dogeUser -> !dogeUser.isAdminOrModerator(), "is not admin or moderator");
    }

    @Test
    @DisplayName("Should activate admin successfully")
    void shouldActivateAdminSuccessfully() {
        String token = "activationtoken";
        String email = "dev@test.com";
        DogeRole adminRole = DogeRole.builder().level(DogeRoleLevel.ADMIN).build();
        DogeRole userRole = DogeRole.builder().level(DogeRoleLevel.USER).build();
        DogeRole notConfirmedRole = DogeRole.builder().level(DogeRoleLevel.NOT_CONFIRMED_USER).build();
        DogeUser user = DogeUser.builder()
                .email(email)
                .roles(Lists.newArrayList(notConfirmedRole, adminRole))
                .build();
        EmailConfirmationToken emailConfirmationToken = EmailConfirmationToken.builder()
                .id(1L)
                .user(user)
                .originEmail(email)
                .createdAt(LocalDateTime.now())
                .expirationTime(Duration.ofHours(2))
                .build();

        when(emailConfirmationTokenService.getConfirmationToken(token)).thenReturn(emailConfirmationToken);
        when(dogeRoleRepository.getByLevel(DogeRoleLevel.USER)).thenReturn(userRole);

        registerService.activateUser(token);

        ArgumentCaptor<DogeUser> userArgumentCaptor = ArgumentCaptor.forClass(DogeUser.class);
        verify(dogeUserRepository).save(userArgumentCaptor.capture());
        DogeUser capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser)
                .matches(x -> x.getRoles().contains(userRole), "contains user role")
                .matches(x -> x.getRoles().contains(adminRole), "contains admin role")
                .matches(x -> !x.getRoles().contains(notConfirmedRole), "does not contains not confirmed role")
                .matches(DogeUser::isConfirmed, "is confirmed")
                .matches(DogeUser::isAdminOrModerator, "is admin or moderator");

        verify(eventPublisher).publishEvent(any(OnEmailConfirmTokenNoLongerValidEvent.class));
    }

    @Test
    @DisplayName("Should throw exception when user is already confirmed")
    void shouldThrowExceptionWhenUserIsAlreadyConfirmed() {
        String token = "activationtoken";
        String email = "dev@test.com";
        DogeRole userRole = DogeRole.builder().level(DogeRoleLevel.USER).build();
        DogeUser user = DogeUser.builder()
                .email(email)
                .roles(Lists.newArrayList(userRole))
                .build();
        EmailConfirmationToken emailConfirmationToken = EmailConfirmationToken.builder()
                .id(1L)
                .user(user)
                .originEmail(email)
                .createdAt(LocalDateTime.now())
                .expirationTime(Duration.ofHours(2))
                .build();

        when(emailConfirmationTokenService.getConfirmationToken(token)).thenReturn(emailConfirmationToken);

        assertThatThrownBy(() -> registerService.activateUser(token))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("DOGE_USER_ALREADY_ENABLED");

        verify(eventPublisher).publishEvent(any(OnEmailConfirmTokenNoLongerValidEvent.class));
        verify(dogeUserRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when email confirmation token is expired")
    void shouldThrowExceptionWhenEmailConfirmationTokenIsExpired() {
        String token = "activationtoken";
        String email = "dev@test.com";
        DogeRole notConfirmedRole = DogeRole.builder().level(DogeRoleLevel.NOT_CONFIRMED_USER).build();
        DogeUser user = DogeUser.builder()
                .email(email)
                .roles(Lists.newArrayList(notConfirmedRole))
                .build();
        EmailConfirmationToken emailConfirmationToken = EmailConfirmationToken.builder()
                .id(1L)
                .user(user)
                .originEmail(email)
                .createdAt(LocalDateTime.now())
                .expirationTime(Duration.ZERO)
                .build();

        when(emailConfirmationTokenService.getConfirmationToken(token)).thenReturn(emailConfirmationToken);

        assertThatThrownBy(() -> registerService.activateUser(token))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("CONFIRM_TOKEN_EXPIRED");

        verify(eventPublisher).publishEvent(any(OnEmailConfirmTokenNoLongerValidEvent.class));
        verify(dogeUserRepository, never()).save(any());
    }
}