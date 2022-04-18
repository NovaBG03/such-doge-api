package xyz.suchdoge.webapi.service;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;
import xyz.suchdoge.webapi.dto.meme.response.MemePageResponseDto;
import xyz.suchdoge.webapi.dto.meme.filter.MemeOrderFilter;
import xyz.suchdoge.webapi.dto.meme.filter.MemePublishFilter;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.mapper.meme.MemeMapper;
import xyz.suchdoge.webapi.model.Meme;
import xyz.suchdoge.webapi.model.user.DogeRole;
import xyz.suchdoge.webapi.model.user.DogeRoleLevel;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.repository.MemeRepository;
import xyz.suchdoge.webapi.service.storage.CloudStorageService;
import xyz.suchdoge.webapi.service.storage.StoragePath;
import xyz.suchdoge.webapi.service.validator.ModelValidatorService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemeServiceTest {
    @Mock
    MemeRepository memeRepository;
    @Mock
    DogeUserService userService;
    @Mock
    CloudStorageService cloudStorageService;
    @Mock
    NotificationService notificationService;
    @Mock
    ModelValidatorService modelValidatorService;
    @Mock
    MemeMapper memeMapper;

    MemeService memeService;

    @BeforeEach
    void setUp() {
        memeService = new MemeService(
                memeRepository,
                userService,
                cloudStorageService,
                notificationService,
                modelValidatorService,
                memeMapper
        );
    }

    @Test
    @DisplayName("Should get memes count correctly")
    void shouldGetMemesCountCorrectly() {
        String username = "username";
        Long count = 10L;

        when(memeRepository.countByPublisherUsernameAndApprovedOnNotNull(username)).thenReturn(count);

        Long actual = memeService.getMemesCount(username);
        assertThat(actual).isEqualTo(count);
    }

    @Test
    @DisplayName("Should get approved memes correctly")
    void shouldGetApprovedMemesCorrectly() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        MemePublishFilter publishFilter = MemePublishFilter.APPROVED;
        MemeOrderFilter orderFilter = MemeOrderFilter.NEWEST;
        String publisherUsername = null;
        String principalUsername = null;

        Page<Meme> memePage = new PageImpl<>(Lists.newArrayList());
        MemePageResponseDto memePageResponseDto = MemePageResponseDto.builder().build();

        when(userService.getUserByUsername(principalUsername)).thenThrow(UsernameNotFoundException.class);
        when(memeRepository.findAllByApprovedOnNotNull(any())).thenReturn(memePage);
        when(memeMapper.createMemePageResponseDto(memePage, false)).thenReturn(memePageResponseDto);

        MemePageResponseDto actual = memeService.getMemes(pageRequest, publishFilter, orderFilter, publisherUsername, principalUsername);

        assertThat(actual).isEqualTo(memePageResponseDto);
    }

    @Test
    @DisplayName("Should get approved meme without principal correctly")
    void shouldGetApprovedMemeWithoutPrincipalCorrectly() {
        Long memeId = 1L;
        Meme meme = Meme.builder()
                .id(memeId)
                .build();

        when(memeRepository.findByIdAndApprovedOnNotNull(memeId)).thenReturn(Optional.of(meme));

        Meme actual = memeService.getMeme(memeId, null);
        assertThat(actual).isEqualTo(meme);
    }

    @Test
    @DisplayName("Should throw exception when meme not found without principal")
    void shouldThrowExceptionWhenMemeNotFoundWithoutPrincipalCorrectly() {
        Long memeId = 1L;

        when(memeRepository.findByIdAndApprovedOnNotNull(memeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memeService.getMeme(memeId, null))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("MEME_ID_INVALID");
    }

    @Test
    @DisplayName("Should throw exception when meme not found with principal")
    void shouldThrowExceptionWhenMemeNotFoundWithPrincipal() {
        Long memeId = 1L;
        String username = "principal";

        when(memeRepository.findById(memeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memeService.getMeme(memeId, username))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("MEME_ID_INVALID");
    }

    @Test
    @DisplayName("Should get approved meme with user correctly")
    void shouldGetApprovedMemeWithUserCorrectly() {
        DogeRole role = DogeRole.builder().level(DogeRoleLevel.USER).build();
        String username = "principal";
        DogeUser user = DogeUser.builder()
                .username(username)
                .roles(Lists.newArrayList(role))
                .build();
        Long memeId = 1L;
        Meme meme = Meme.builder()
                .id(memeId)
                .approvedOn(LocalDateTime.now())
                .build();

        when(memeRepository.findById(memeId)).thenReturn(Optional.of(meme));
        when(userService.getUserByUsername(username)).thenReturn(user);

        Meme actual = memeService.getMeme(memeId, username);
        assertThat(actual).isEqualTo(meme);
    }

    @Test
    @DisplayName("Should get meme with publisher correctly")
    void shouldGetMemeWithPublisherCorrectly() {
        DogeRole role = DogeRole.builder().level(DogeRoleLevel.USER).build();
        String username = "principal";
        DogeUser user = DogeUser.builder()
                .username(username)
                .roles(Lists.newArrayList(role))
                .build();
        Long memeId = 1L;
        Meme meme = Meme.builder()
                .id(memeId)
                .publisher(user)
                .build();

        when(memeRepository.findById(memeId)).thenReturn(Optional.of(meme));
        when(userService.getUserByUsername(username)).thenReturn(user);

        Meme actual = memeService.getMeme(memeId, username);
        assertThat(actual).isEqualTo(meme);
    }

    @Test
    @DisplayName("Should get meme with admin correctly")
    void shouldGetMemeWithAdminCorrectly() {
        DogeRole role = DogeRole.builder().level(DogeRoleLevel.ADMIN).build();
        String username = "principal";
        DogeUser user = DogeUser.builder()
                .username(username)
                .roles(Lists.newArrayList(role))
                .build();
        Long memeId = 1L;
        Meme meme = Meme.builder()
                .id(memeId)
                .build();

        when(memeRepository.findById(memeId)).thenReturn(Optional.of(meme));
        when(userService.getUserByUsername(username)).thenReturn(user);

        Meme actual = memeService.getMeme(memeId, username);
        assertThat(actual).isEqualTo(meme);
    }

    @Test
    @DisplayName("Should throw exception when get not approved meme with user")
    void shouldThrowExceptionWhenGetNotApprovedMemeWithUser() {
        DogeRole role = DogeRole.builder().level(DogeRoleLevel.USER).build();
        String username = "principal";
        DogeUser user = DogeUser.builder()
                .username(username)
                .roles(Lists.newArrayList(role))
                .build();
        Long memeId = 1L;
        Meme meme = Meme.builder()
                .id(memeId)
                .build();

        when(memeRepository.findById(memeId)).thenReturn(Optional.of(meme));
        when(userService.getUserByUsername(username)).thenReturn(user);

        assertThatThrownBy(() -> memeService.getMeme(memeId, username))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("MEME_ID_INVALID");
    }

    @Test
    @DisplayName("Should create meme successfully")
    void shouldCreateMemeSuccessfully() throws IOException {
        MultipartFile image = Mockito.mock(MultipartFile.class);
        byte[] imageBytes = new byte[10];
        Meme meme = Meme.builder().build();
        String username = "username";
        DogeUser user = DogeUser.builder()
                .id(UUID.randomUUID())
                .username(username)
                .build();

        when(userService.getConfirmedUser(username)).thenReturn(user);
        when(image.getBytes()).thenReturn(imageBytes);

        memeService.createMeme(image, meme, username);

        ArgumentCaptor<Meme> memeArgumentCaptor = ArgumentCaptor.forClass(Meme.class);
        verify(memeRepository).save(memeArgumentCaptor.capture());
        Meme caputredMeme = memeArgumentCaptor.getValue();

        assertThat(caputredMeme)
                .matches(x -> x.getId() == null, "id is null")
                .matches(x -> x.getApprovedBy() == null, "approved by is null")
                .matches(x -> x.getApprovedOn() == null, "approved on is null")
                .matches(x -> x.getPublisher().equals(user), "is correct publisher")
                .matches(x -> x.getPublishedOn().isBefore(LocalDateTime.now()), "is published before now");

        verify(cloudStorageService).upload(imageBytes, caputredMeme.getImageKey(), StoragePath.MEME);
        verify(modelValidatorService).validate(caputredMeme);
    }

    @Test
    @DisplayName("Should throw exception when can not read image bytes")
    void shouldThrowExceptionWhenCanNotReadImageBytes() throws IOException {
        MultipartFile image = Mockito.mock(MultipartFile.class);
        Meme meme = Meme.builder().build();
        String username = "username";
        DogeUser user = DogeUser.builder()
                .id(UUID.randomUUID())
                .username(username)
                .build();

        when(userService.getConfirmedUser(username)).thenReturn(user);
        when(image.getBytes()).thenThrow(IOException.class);

        assertThatThrownBy(() -> memeService.createMeme(image, meme, username))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("CAN_NOT_READ_IMAGE_BYTES");

        verify(cloudStorageService, never()).upload(any(), any(), any());
        verify(modelValidatorService, never()).validate(any(Meme.class));
        verify(memeRepository, never()).save(any(Meme.class));
    }
    
    @Test
    @DisplayName("Should throw exception when can not save image")
    void shouldThrowExceptionWhenCanNotSaveImage() throws IOException {
        MultipartFile image = Mockito.mock(MultipartFile.class);
        Meme meme = Meme.builder().build();
        String username = "username";
        DogeUser user = DogeUser.builder()
                .id(UUID.randomUUID())
                .username(username)
                .build();

        when(userService.getConfirmedUser(username)).thenReturn(user);
        when(image.getBytes()).thenReturn(new byte[10]);
        doThrow(DogeHttpException.class).when(cloudStorageService).upload(any(), anyString(), any());

        assertThatThrownBy(() -> memeService.createMeme(image, meme, username))
                .isInstanceOf(DogeHttpException.class);

        verify(modelValidatorService, never()).validate(any(Meme.class));
        verify(memeRepository, never()).save(any(Meme.class));
    }

    @Test
    @DisplayName("Should approve meme successfully")
    void shouldApproveMemeSuccessfully() {
        String username = "username";
        DogeRole role = DogeRole.builder().level(DogeRoleLevel.ADMIN).build();
        DogeUser admin = DogeUser.builder()
                .id(UUID.randomUUID())
                .username(username)
                .roles(Lists.newArrayList(role))
                .build();
        DogeUser publisher = DogeUser.builder().id(UUID.randomUUID()).build();
        Long memeId = 1L;
        Meme meme = Meme.builder()
                .id(memeId)
                .publisher(publisher)
                .build();

        when(userService.getConfirmedUser(username)).thenReturn(admin);
        when(userService.getUserByUsername(username)).thenReturn(admin);
        when(memeRepository.findById(memeId)).thenReturn(Optional.of(meme));
        when(memeRepository.save(meme)).thenReturn(meme);

        memeService.approveMeme(memeId, username);

        ArgumentCaptor<Meme> memeArgumentCaptor = ArgumentCaptor.forClass(Meme.class);
        verify(memeRepository).save(memeArgumentCaptor.capture());
        assertThat(memeArgumentCaptor.getValue())
                .matches(x -> x.getApprovedBy().equals(admin), "approved by is set")
                .matches(x -> x.getApprovedOn().isBefore(LocalDateTime.now()), "approved on date is set");

        ArgumentCaptor<DogeUser> publisherArgumetCapture = ArgumentCaptor.forClass(DogeUser.class);
        verify(notificationService).pushNotificationTo(any(), publisherArgumetCapture.capture());
        assertThat(publisherArgumetCapture.getValue()).isEqualTo(publisher);
    }

    @Test
    @DisplayName("Should throw exception when approve meme aleady approved")
    void shouldThrowExceptionWhenApproveMemeAleadyApproved() {
        String username = "username";
        DogeRole role = DogeRole.builder().level(DogeRoleLevel.ADMIN).build();
        DogeUser user = DogeUser.builder()
                .username(username)
                .roles(Lists.newArrayList(role))
                .build();
        Long memeId = 1L;
        Meme meme = Meme.builder()
                .id(memeId)
                .approvedOn(LocalDateTime.now())
                .build();

        when(userService.getConfirmedUser(username)).thenReturn(user);
        when(memeRepository.findById(memeId)).thenReturn(Optional.of(meme));

        assertThatThrownBy(() -> memeService.approveMeme(memeId, username))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("MEME_ALREADY_APPROVED");
    }

    @Test
    @DisplayName("Should reject meme successfully")
    void shouldRejectMemeSuccessfully() {
        String username = "username";
        DogeRole role = DogeRole.builder().level(DogeRoleLevel.ADMIN).build();
        DogeUser admin = DogeUser.builder()
                .id(UUID.randomUUID())
                .username(username)
                .roles(Lists.newArrayList(role))
                .build();
        DogeUser publisher = DogeUser.builder().id(UUID.randomUUID()).build();
        Long memeId = 1L;
        String imageKey = "imagekey.png";
        Meme meme = Meme.builder()
                .id(memeId)
                .publisher(publisher)
                .imageKey(imageKey)
                .build();

        when(userService.getConfirmedUser(username)).thenReturn(admin);
        when(userService.getUserByUsername(username)).thenReturn(admin);
        when(memeRepository.findById(memeId)).thenReturn(Optional.of(meme));

        memeService.rejectMeme(memeId, username);

        verify(cloudStorageService).remove(imageKey, StoragePath.MEME);
        verify(memeRepository).delete(meme);

        ArgumentCaptor<DogeUser> publisherArgumetCapture = ArgumentCaptor.forClass(DogeUser.class);
        verify(notificationService).pushNotificationTo(any(), publisherArgumetCapture.capture());
        assertThat(publisherArgumetCapture.getValue()).isEqualTo(publisher);
    }

    @Test
    @DisplayName("Should thow exception when reject meme already approved")
    void shouldThowExceptionWhenRejectMemeAlreadyApproved() {
        String username = "username";
        DogeRole role = DogeRole.builder().level(DogeRoleLevel.ADMIN).build();
        DogeUser user = DogeUser.builder()
                .username(username)
                .roles(Lists.newArrayList(role))
                .build();
        Long memeId = 1L;
        Meme meme = Meme.builder()
                .id(memeId)
                .approvedOn(LocalDateTime.now())
                .build();

        when(userService.getConfirmedUser(username)).thenReturn(user);
        when(memeRepository.findById(memeId)).thenReturn(Optional.of(meme));

        assertThatThrownBy(() -> memeService.rejectMeme(memeId, username))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("MEME_ALREADY_APPROVED");
    }

    @Test
    @DisplayName("Should delete meme by admin successfully")
    void shouldDeleteMemeByAdminSuccessfully() {
        String username = "username";
        DogeRole role = DogeRole.builder().level(DogeRoleLevel.ADMIN).build();
        DogeUser admin = DogeUser.builder()
                .id(UUID.randomUUID())
                .username(username)
                .roles(Lists.newArrayList(role))
                .build();
        DogeUser publisher = DogeUser.builder().id(UUID.randomUUID()).build();
        Long memeId = 1L;
        String imageKey = "imagekey.png";
        Meme meme = Meme.builder()
                .id(memeId)
                .publisher(publisher)
                .imageKey(imageKey)
                .build();

        when(userService.getConfirmedUser(username)).thenReturn(admin);
        when(userService.getUserByUsername(username)).thenReturn(admin);
        when(memeRepository.findById(memeId)).thenReturn(Optional.of(meme));

        memeService.deleteMeme(memeId, username);

        verify(cloudStorageService).remove(imageKey, StoragePath.MEME);
        verify(memeRepository).delete(meme);

        ArgumentCaptor<DogeUser> publisherArgumetCapture = ArgumentCaptor.forClass(DogeUser.class);
        verify(notificationService).pushNotificationTo(any(), publisherArgumetCapture.capture());
        assertThat(publisherArgumetCapture.getValue()).isEqualTo(publisher);
    }

    @Test
    @DisplayName("Should delete meme by publisher successfully")
    void shouldDeleteMemeByPublisherSuccessfully() {
        String username = "username";
        DogeRole role = DogeRole.builder().level(DogeRoleLevel.USER).build();
        DogeUser user = DogeUser.builder()
                .id(UUID.randomUUID())
                .username(username)
                .roles(Lists.newArrayList(role))
                .build();
        Long memeId = 1L;
        String imageKey = "imagekey.png";
        Meme meme = Meme.builder()
                .id(memeId)
                .publisher(user)
                .imageKey(imageKey)
                .build();

        when(userService.getConfirmedUser(username)).thenReturn(user);
        when(userService.getUserByUsername(username)).thenReturn(user);
        when(memeRepository.findById(memeId)).thenReturn(Optional.of(meme));

        memeService.deleteMeme(memeId, username);

        verify(cloudStorageService).remove(imageKey, StoragePath.MEME);
        verify(memeRepository).delete(meme);

        verify(notificationService, never()).pushNotificationTo(any(), any());
    }

    @Test
    @DisplayName("Should throw exception when delete meme not admin or publisher")
    void shouldThrowExceptionWhenDeleteMemeNotAdminOrPublisher() {
        String username = "username";
        DogeRole role = DogeRole.builder().level(DogeRoleLevel.USER).build();
        DogeUser admin = DogeUser.builder()
                .id(UUID.randomUUID())
                .username(username)
                .roles(Lists.newArrayList(role))
                .build();
        DogeUser publisher = DogeUser.builder().id(UUID.randomUUID()).build();
        Long memeId = 1L;
        Meme meme = Meme.builder()
                .id(memeId)
                .publisher(publisher)
                .approvedOn(LocalDateTime.now())
                .build();

        when(userService.getConfirmedUser(username)).thenReturn(admin);
        when(userService.getUserByUsername(username)).thenReturn(admin);
        when(memeRepository.findById(memeId)).thenReturn(Optional.of(meme));

        assertThatThrownBy(() -> memeService.deleteMeme(memeId, username))
                .isInstanceOf(DogeHttpException.class)
                .hasMessage("CAN_NOT_DELETE_FOREIGN_MEME");
    }
}