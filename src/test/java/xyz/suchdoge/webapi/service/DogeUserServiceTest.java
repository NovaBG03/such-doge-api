package xyz.suchdoge.webapi.service;

import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.blockchain.Address;
import xyz.suchdoge.webapi.model.blockchain.Network;
import xyz.suchdoge.webapi.model.user.DogeRole;
import xyz.suchdoge.webapi.model.user.DogeRoleLevel;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.repository.DogeRoleRepository;
import xyz.suchdoge.webapi.repository.DogeUserRepository;
import xyz.suchdoge.webapi.service.blockchain.DogeBlockchainService;
import xyz.suchdoge.webapi.service.imageGenerator.ImageGeneratorService;
import xyz.suchdoge.webapi.service.register.event.OnEmailConfirmationNeededEvent;
import xyz.suchdoge.webapi.service.storage.CloudStorageService;
import xyz.suchdoge.webapi.service.storage.StoragePath;
import xyz.suchdoge.webapi.service.validator.DogeUserVerifier;
import xyz.suchdoge.webapi.service.validator.ModelValidatorService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DogeUserServiceTest {
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Mock()
    DogeUserRepository userRepository;
    @Mock()
    DogeRoleRepository roleRepository;
    @Mock()
    CloudStorageService cloudStorageService;
    @Mock()
    ImageGeneratorService imageGeneratorService;
    @Mock()
    DogeBlockchainService blockchainService;
    @Mock()
    DogeUserVerifier userVerifier;
    @Mock()
    ModelValidatorService modelValidatorService;
    @Mock()
    ApplicationEventPublisher applicationEventPublisher;

    DogeUserService userService;

    String username = "ivan";

    @BeforeEach
    void initUserService() {
        userService = new DogeUserService(passwordEncoder,
                userRepository,
                roleRepository,
                cloudStorageService,
                imageGeneratorService,
                blockchainService,
                userVerifier,
                modelValidatorService,
                applicationEventPublisher);
    }

    @Test
    @DisplayName("Should get user by username successfully")
    void shouldGetUserByUsernameSuccessfully() {
        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(DogeUser.builder().username(username).build()));

        final DogeUser user = userService.getUserByUsername(username);

        assertThat(user.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("Should throw exception when get user username not found")
    void shouldThrowExceptionWhenGetUserUsernameNotFound() {
        when(userRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("Should load user details successfully")
    void shouldLoadUserDetailsSuccessfully() {
        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(DogeUser.builder().username(username).build()));

        final UserDetails userDetails = userService.loadUserByUsername(username);

        assertThat(userDetails.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("Should throw exception when load user details username not found")
    void shouldThrowExceptionWhenLoadUserDetailsUsernameNotFound() {
        when(userRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("Should get confirmed user by username successfully")
    void shouldGetConfirmedUserByUsernameSuccessfully() {
        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(DogeUser.builder()
                        .username(username)
                        .roles(List.of(DogeRole.builder().level(DogeRoleLevel.USER).build()))
                        .build()));

        DogeUser user = userService.getConfirmedUser(username);

        assertThat(user.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("Should throw exception when get confirmed user not confirmed")
    void shouldThrowExceptionWhenGetConfirmedUserNotConfirmed() {
        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(DogeUser.builder()
                        .username(username)
                        .roles(List.of(DogeRole.builder().level(DogeRoleLevel.NOT_CONFIRMED_USER).build()))
                        .build()));

        assertThatThrownBy(() -> userService.getConfirmedUser(username))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("USER_NOT_CONFIRMED");
    }

    @Test
    @DisplayName("Should throw exception when get confirmed user not found")
    void shouldThrowExceptionWhenGetConfirmedUserNotFound() {
        when(userRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getConfirmedUser(username))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("Should create new user successfully")
    void shouldCreateNewUserSuccessfully() {
        String email = "test@abv.bg";
        String password = "test";
        byte[] generatedImageBytes = new byte[100];
        String publicKey = "address";
        Network addressNetwork = Network.DOGETEST;
        Address address = new Address(publicKey, 4L, username, addressNetwork.toString());

        when(roleRepository.getByLevel(DogeRoleLevel.NOT_CONFIRMED_USER))
                .thenReturn(getRole(DogeRoleLevel.NOT_CONFIRMED_USER));

        when(userRepository.save(any(DogeUser.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        when(imageGeneratorService.generateProfilePic(username))
                .thenReturn(generatedImageBytes);

        when(blockchainService.createOrGetAddress(username))
                .thenReturn(address);

        DogeUser user = userService.createUser(username, email, password);

        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getEncodedPassword()).matches(x -> passwordEncoder.matches(password, x));
        assertThat(user.isConfirmed()).isFalse();
        assertThat(user.getDogePublicKey()).isEqualTo(publicKey);
        verify(userVerifier).verifyUsername(username);
        verify(userVerifier).verifyEmail(email);
        verify(userVerifier).verifyPassword(password);
        verify(modelValidatorService, atLeastOnce()).validate(any(DogeUser.class));
        verify(userRepository, atLeastOnce()).save(any(DogeUser.class));
        verify(imageGeneratorService).generateProfilePic(username);
        verify(cloudStorageService).upload(generatedImageBytes, username + ".png", StoragePath.USER);
    }

    @Test
    @DisplayName("Should create new user successfully when can not generate profile pic")
    void shouldCreateNewUserSuccessfullyWhenCanNotGenerateProfilePic() {
        String email = "test@abv.bg";
        String password = "test";
        String publicKey = "address";
        Network addressNetwork = Network.DOGETEST;
        Address address = new Address(publicKey, 4L, username, addressNetwork.toString());

        when(roleRepository.getByLevel(DogeRoleLevel.NOT_CONFIRMED_USER))
                .thenReturn(getRole(DogeRoleLevel.NOT_CONFIRMED_USER));

        when(userRepository.save(any(DogeUser.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        when(imageGeneratorService.generateProfilePic(username))
                .thenThrow(DogeHttpException.class);

        when(blockchainService.createOrGetAddress(username))
                .thenReturn(address);

        DogeUser user = userService.createUser(username, email, password);

        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getEncodedPassword()).matches(x -> passwordEncoder.matches(password, x));
        assertThat(user.isConfirmed()).isFalse();
        verify(userVerifier).verifyUsername(username);
        verify(userVerifier).verifyEmail(email);
        verify(userVerifier).verifyPassword(password);
        verify(modelValidatorService, atLeastOnce()).validate(any(DogeUser.class));
        verify(userRepository, atLeastOnce()).save(any(DogeUser.class));
        verify(imageGeneratorService).generateProfilePic(username);
        verify(cloudStorageService, never()).upload(any(), any(), any());
    }

    @Test
    @DisplayName("Should create new user successfully when can not upload profile pic to cloud storage")
    void shouldCreateNewUserSuccessfullyWhenCanNotUploadProfilePicToCloudStorage() {
        String email = "test@abv.bg";
        String password = "test";
        byte[] generatedImageBytes = new byte[100];
        Address address = new Address("address", 1L, username, Network.DOGETEST.toString());

        when(roleRepository.getByLevel(DogeRoleLevel.NOT_CONFIRMED_USER))
                .thenReturn(getRole(DogeRoleLevel.NOT_CONFIRMED_USER));

        when(userRepository.save(any(DogeUser.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        when(imageGeneratorService.generateProfilePic(username))
                .thenReturn(generatedImageBytes);

        when(blockchainService.createOrGetAddress(username))
                .thenReturn(address);

        doThrow(new DogeHttpException("SOMETHING_WENT_WONG", HttpStatus.BAD_REQUEST))
                .when(cloudStorageService)
                .upload(generatedImageBytes, username + ".png", StoragePath.USER);

        DogeUser user = userService.createUser(username, email, password);

        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getEncodedPassword()).matches(x -> passwordEncoder.matches(password, x));
        assertThat(user.isConfirmed()).isFalse();
        verify(userVerifier).verifyUsername(username);
        verify(userVerifier).verifyEmail(email);
        verify(userVerifier).verifyPassword(password);
        verify(modelValidatorService, atLeastOnce()).validate(any(DogeUser.class));
        verify(userRepository, atLeastOnce()).save(any(DogeUser.class));
        verify(imageGeneratorService).generateProfilePic(username);
        verify(cloudStorageService).upload(generatedImageBytes, username + ".png", StoragePath.USER);
    }

    @Test
    @DisplayName("Should change user email successfully")
    void shouldChangeUserEmailSuccessfully() {
        String oldEmail = "test@test.com";
        String newEmail = "new_email@gmail.com";

        DogeUser user = DogeUser.builder()
                .username(username)
                .email(oldEmail)
                .roles(Sets.newHashSet(getRole(DogeRoleLevel.USER)))
                .build();


        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(roleRepository.getByLevel(DogeRoleLevel.NOT_CONFIRMED_USER))
                .thenReturn(getRole(DogeRoleLevel.NOT_CONFIRMED_USER));

        when(userRepository.save(any(DogeUser.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        DogeUser updatedUser = userService.changeUserEmail(newEmail, username);

        assertThat(updatedUser.getEmail()).isEqualTo(newEmail);
        assertThat(user.isConfirmed()).isFalse();
        verify(userVerifier).verifyEmail(newEmail);
        verify(modelValidatorService).validate(user);
        verify(applicationEventPublisher).publishEvent(any(OnEmailConfirmationNeededEvent.class));
    }

    @Test
    @DisplayName("Should change admin email successfully")
    void shouldChangeAdminEmailSuccessfully() {
        String oldEmail = "test@test.com";
        String newEmail = "new_email@gmail.com";

        DogeUser user = DogeUser.builder()
                .username(username)
                .email(oldEmail)
                .roles(Sets.newHashSet(getRole(DogeRoleLevel.ADMIN), getRole(DogeRoleLevel.MODERATOR)))
                .build();

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(roleRepository.getByLevel(DogeRoleLevel.NOT_CONFIRMED_USER))
                .thenReturn(getRole(DogeRoleLevel.NOT_CONFIRMED_USER));

        when(userRepository.save(any(DogeUser.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        DogeUser updatedUser = userService.changeUserEmail(newEmail, username);

        assertThat(updatedUser.getEmail()).isEqualTo(newEmail);
        assertThat(updatedUser.isAdminOrModerator()).isTrue();
        assertThat(updatedUser.isConfirmed()).isFalse();
        verify(userVerifier).verifyEmail(newEmail);
        verify(modelValidatorService).validate(user);
        verify(userRepository).save(any(DogeUser.class));
        verify(applicationEventPublisher).publishEvent(any(OnEmailConfirmationNeededEvent.class));
    }

    @Test
    @DisplayName("Should change public key successfully")
    void shouldChangePublicKeySuccessfully() {
        String oldPublicKey = "3434898034te34";
        String newPublicKey = "eadap989klioda";

        when(userRepository.save(any(DogeUser.class)))
                .then(AdditionalAnswers.returnsFirstArg());

        DogeUser user = DogeUser.builder()
                .username(username)
                .dogePublicKey(oldPublicKey)
                .roles(List.of(getRole(DogeRoleLevel.ADMIN), getRole(DogeRoleLevel.MODERATOR)))
                .build();

        DogeUser updatedUser = userService.changeDogePublicKey(newPublicKey, user);

        assertThat(updatedUser.getDogePublicKey()).isEqualTo(newPublicKey);
        verify(modelValidatorService).validate(user);
        verify(userRepository).save(any(DogeUser.class));
    }

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {
        String oldPassword = "old_password";
        String newPassword = "new_password";
        String confirmPassword = "new_password";

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(DogeUser.builder()
                        .username(username)
                        .encodedPassword(passwordEncoder.encode(oldPassword))
                        .build()));

        userService.changePassword(oldPassword, newPassword, confirmPassword, username);

        verify(userVerifier).verifyPassword(newPassword);
        verify(modelValidatorService).validate(any(DogeUser.class));

        ArgumentCaptor<DogeUser> userArgumentCaptor = ArgumentCaptor.forClass(DogeUser.class);
        verify(userRepository).save(userArgumentCaptor.capture());

        final DogeUser capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser.getEncodedPassword()).matches(x -> passwordEncoder.matches(newPassword, x));
    }

    @Test
    @DisplayName("Should throw exception when change password new and confirm passwords does not match")
    void shouldThrowExceptionWhenChangePasswordNewAndConfirmPasswordsDoesNotMatch() {
        String oldPassword = "old_password";
        String newPassword = "new_password";
        String confirmPassword = "confirm_password";

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(DogeUser.builder()
                        .username(username)
                        .encodedPassword(passwordEncoder.encode(oldPassword))
                        .build()));

        assertThatThrownBy(() -> userService.changePassword(oldPassword, newPassword, confirmPassword, username))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("PASSWORDS_DOES_NOT_MATCH");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when change password wrong old password")
    void shouldThrowExceptionWhenChangePasswordWrongOldPassword() {
        String oldPassword = "old_password";
        String wrongOldPassword = "wrong_old_password";
        String newPassword = "new_password";
        String confirmPassword = "new_password";

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(DogeUser.builder()
                        .username(username)
                        .encodedPassword(passwordEncoder.encode(oldPassword))
                        .build()));

        assertThatThrownBy(() -> userService.changePassword(wrongOldPassword, newPassword, confirmPassword, username))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("WRONG_OLD_PASSWORD");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when change password new and old passwords are the same")
    void shouldThrowExceptionWhenChangePasswordNewAndOldPasswordsAreTheSame() {
        String oldPassword = "old_password";
        String newPassword = "old_password";
        String confirmPassword = "old_password";

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(DogeUser.builder()
                        .username(username)
                        .encodedPassword(passwordEncoder.encode(oldPassword))
                        .build()));

        assertThatThrownBy(() -> userService.changePassword(oldPassword, newPassword, confirmPassword, username))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("NEW_PASSWORD_AND_OLD_PASSWORD_ARE_THE_SAME");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should set profile image successfully")
    void shouldSetProfileImageSuccessfully() throws Exception {
        MultipartFile multipartFile = new MockMultipartFile("image", new byte[100]);

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(DogeUser.builder()
                        .username(username)
                        .build()));

        userService.setProfileImage(multipartFile, username);

        verify(cloudStorageService).upload(multipartFile.getBytes(), username + ".png", StoragePath.USER);
    }

    @Test
    @DisplayName("Should throw exception when set profile image can not read image bytes")
    void shouldThrowExceptionWhenSetProfileImageCanNotReadImageBytes() {
        MultipartFile multipartFile = new MockMultipartFile("image", new byte[100]) {
            @NotNull
            @Override
            public byte[] getBytes() throws IOException {
                throw new IOException();
            }
        };

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(DogeUser.builder()
                        .username(username)
                        .build()));

        assertThatThrownBy(() -> userService.setProfileImage(multipartFile, username))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("CAN_NOT_READ_IMAGE_BYTES");

        verify(cloudStorageService, never()).upload(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw exception when set profile image can not be saved")
    void shouldThrowExceptionWhenSetProfileImageCanNotBeSaved() throws Exception {
        MultipartFile multipartFile = new MockMultipartFile("image", new byte[100]);

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(DogeUser.builder()
                        .username(username)
                        .build()));

        doThrow(new RuntimeException())
                .when(cloudStorageService)
                .upload(multipartFile.getBytes(), username + ".png", StoragePath.USER);

        assertThatThrownBy(() -> userService.setProfileImage(multipartFile, username))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("CAN_NOT_SAVE_IMAGE");

        verify(cloudStorageService).upload(multipartFile.getBytes(), username + ".png", StoragePath.USER);
    }

    private DogeRole getRole(DogeRoleLevel dogeRoleLevel) {
        return DogeRole.builder().level(dogeRoleLevel).build();
    }
}
