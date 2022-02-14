package xyz.suchdoge.webapi.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.suchdoge.webapi.dto.meme.MemePageResponseDto;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.mapper.meme.MemeMapper;
import xyz.suchdoge.webapi.model.notification.Notification;
import xyz.suchdoge.webapi.model.notification.NotificationCategory;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.model.Meme;
import xyz.suchdoge.webapi.repository.MemeRepository;
import xyz.suchdoge.webapi.service.storage.CloudStorageService;
import xyz.suchdoge.webapi.service.validator.ModelValidatorService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class MemeService {
    private final MemeRepository memeRepository;
    private final DogeUserService dogeUserService;
    private final CloudStorageService cloudStorageService;
    private final NotificationService notificationService;
    private final ModelValidatorService modelValidatorService;
    private final MemeMapper memeMapper;

    public MemeService(MemeRepository memeRepository,
                       DogeUserService dogeUserService,
                       CloudStorageService cloudStorageService,
                       NotificationService notificationService,
                       ModelValidatorService modelValidatorService,
                       MemeMapper memeMapper) {
        this.memeRepository = memeRepository;
        this.dogeUserService = dogeUserService;
        this.cloudStorageService = cloudStorageService;
        this.notificationService = notificationService;
        this.modelValidatorService = modelValidatorService;
        this.memeMapper = memeMapper;
    }

    public MemePageResponseDto getMemes(PageRequest pageRequest,
                                        String type,
                                        String publisherUsername,
                                        String principalUsername) throws DogeHttpException {
        // retrieve user from database
        DogeUser principal;
        try {
            principal = this.dogeUserService.getUserByUsername(principalUsername);
        } catch (Exception e) {
            principal = null;
        }

        // properties to be populated after the filtration
        Page<Meme> memePage;
        long count;

        // determine and get requested memes
        if (Objects.equals(type, "approved")) {
            // only approved memes are requested
            if (publisherUsername != null) {
                // approved memes from a specific user
                memePage = this.memeRepository.findAllByPublisherUsernameAndApprovedOnNotNull(publisherUsername, pageRequest);
                count = this.memeRepository.countByPublisherUsernameAndApprovedOnNotNull(publisherUsername);
            } else {
                // approved memes from all users
                memePage = this.memeRepository.findAllByApprovedOnNotNull(pageRequest);
                count = this.memeRepository.countByApprovedOnNotNull();
            }
        } else if (principal != null
                && (principal.isAdminOrModerator() || principal.getUsername().equals(publisherUsername))) {
            // only admins/moderators and the publisher have access to the pending memes
            if (Objects.equals(type, "pending")) {
                // only pending memes requested
                if (publisherUsername != null) {
                    // pending memes from a specific user
                    memePage = this.memeRepository.findAllByPublisherUsernameAndApprovedOnNull(publisherUsername, pageRequest);
                    count = this.memeRepository.countByPublisherUsernameAndApprovedOnNull(publisherUsername);
                } else {
                    // pending memes from all users
                    memePage = this.memeRepository.findAllByApprovedOnNull(pageRequest);
                    count = this.memeRepository.countByApprovedOnNull();
                }
            } else if (Objects.equals(type, "all")) {
                // all memes requested
                if (publisherUsername != null) {
                    // all memes from a specific user
                    memePage = this.memeRepository.findAllByPublisherUsername(publisherUsername, pageRequest);
                    count = this.memeRepository.countByPublisherUsername(publisherUsername);
                } else {
                    // all memes from all users
                    memePage = this.memeRepository.findAll(pageRequest);
                    count = this.memeRepository.count();
                }
            } else {
                // invalid filter type
                throw new DogeHttpException("MEME_FILTER_TYPE_NOT_ALLOWED", HttpStatus.FORBIDDEN);
            }
        } else {
            // user have no access to the requested resource (meme)
            throw new DogeHttpException("MEME_FILTER_TYPE_NOT_ALLOWED", HttpStatus.FORBIDDEN);
        }

        boolean isAdminOrModerator = principal != null && principal.isAdminOrModerator();
        return memeMapper.createMemePageResponseDto(memePage, isAdminOrModerator);
    }

    public Meme getMeme(Long memeId, String principalUsername) {
        final Meme meme = this.memeRepository.getOptionalById(memeId)
                .orElseThrow(() -> new DogeHttpException("MEME_ID_INVALID", HttpStatus.NOT_FOUND));

        final DogeUser user = this.dogeUserService.getUserByUsername(principalUsername);
        if (meme.isApproved() || user.isAdminOrModerator() || user.equals(meme.getPublisher())) {
            return meme;
        }

        throw new DogeHttpException("MEME_ID_INVALID", HttpStatus.NOT_FOUND);
    }

    public Meme createMeme(MultipartFile image, Meme meme, String principalUsername) {
        final DogeUser publisher = this.dogeUserService.getConfirmedUser(principalUsername);

        try {
            final String imageId = UUID.randomUUID() + ".png";
            this.cloudStorageService.upload(image.getBytes(), imageId, "meme");
            meme.setImageKey(imageId);
        } catch (IOException e) {
            throw new DogeHttpException("CAN_NOT_READ_IMAGE_BYTES", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            throw new DogeHttpException("CAN_NOT_SAVE_IMAGE", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        meme.setPublisher(publisher);
        meme.setApprovedBy(null);
        meme.setApprovedOn(null);
        meme.setPublishedOn(LocalDateTime.now());

        if (meme.getDescription() != null && meme.getDescription().length() == 0) {
            meme.setDescription(null);
        }

        this.modelValidatorService.validate(meme);
        return this.memeRepository.save(meme);
    }

    public Meme approveMeme(Long memeId, String principalUsername) {
        final DogeUser user = this.dogeUserService.getConfirmedUser(principalUsername);
        final Meme meme = this.getMeme(memeId, principalUsername);

        if (meme.isApproved()) {
            throw new DogeHttpException("MEME_ALREADY_APPROVED", HttpStatus.BAD_REQUEST);
        }

        meme.setApprovedBy(user);
        meme.setApprovedOn(LocalDateTime.now());

        return this.memeRepository.save(meme);
    }

    public void deleteMeme(Long memeId, String principalUsername) {
        // checks user is confirmed
        final DogeUser user = this.dogeUserService.getConfirmedUser(principalUsername);
        final Meme meme = this.getMeme(memeId, principalUsername);

        if (meme.isApproved()) {
            this.removeMeme(meme);
            return;
        }

        this.denyMeme(meme);
    }

    private void removeMeme(Meme meme) {
        // todo make delete meme and persist point earned with it
        throw new RuntimeException("Not Implemented");
    }

    private void denyMeme(Meme meme) {
        final DogeUser publisher = meme.getPublisher();
        this.memeRepository.delete(meme);
        this.notificationService.pushNotificationTo(
                Notification.builder()
                        .title("Disapproved")
                        .message("Your meme \"" + meme.getTitle() + "\" has been rejected!")
                        .category(NotificationCategory.DANGER)
                        .build(),
                publisher
        );
    }

}
