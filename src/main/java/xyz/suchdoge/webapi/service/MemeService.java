package xyz.suchdoge.webapi.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        } catch (UsernameNotFoundException e) {
            principal = null;
        }

        // properties to be populated after the filtration
        Page<Meme> memePage;

        final boolean isPublisherOrAdmin = principal != null
                && (principal.isAdminOrModerator() || principal.getUsername().equals(publisherUsername));

        // determine and get requested memes
        if (Objects.equals(type, "approved")) {
            // only approved memes are requested
            if (publisherUsername != null) {
                // approved memes from a specific user
                memePage = this.memeRepository.findAllByPublisherUsernameAndApprovedOnNotNull(publisherUsername, pageRequest);
            } else {
                // approved memes from all users
                memePage = this.memeRepository.findAllByApprovedOnNotNull(pageRequest);
            }
        } else if (isPublisherOrAdmin) {
            // only the publisher and admins/moderators have access to the pending memes
            if (Objects.equals(type, "pending")) {
                // only pending memes requested
                if (publisherUsername != null) {
                    // pending memes from a specific user
                    memePage = this.memeRepository.findAllByPublisherUsernameAndApprovedOnNull(publisherUsername, pageRequest);
                } else {
                    // pending memes from all users
                    memePage = this.memeRepository.findAllByApprovedOnNull(pageRequest);
                }
            } else if (Objects.equals(type, "all")) {
                // all memes requested
                if (publisherUsername != null) {
                    // all memes from a specific user
                    memePage = this.memeRepository.findAllByPublisherUsername(publisherUsername, pageRequest);
                } else {
                    // all memes from all users
                    memePage = this.memeRepository.findAll(pageRequest);
                }
            } else {
                // invalid filter type
                throw new DogeHttpException("MEME_FILTER_TYPE_NOT_ALLOWED", HttpStatus.FORBIDDEN);
            }
        } else {
            // user have no access to the requested resource (meme)
            throw new DogeHttpException("MEME_FILTER_TYPE_NOT_ALLOWED", HttpStatus.FORBIDDEN);
        }

        return memeMapper.createMemePageResponseDto(memePage, isPublisherOrAdmin);
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

    @Transactional
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

    @Transactional
    public Meme approveMeme(Long memeId, String principalUsername) {
        final DogeUser principal = this.dogeUserService.getConfirmedUser(principalUsername);
        final Meme meme = this.getMeme(memeId, principalUsername);

        if (meme.isApproved()) {
            throw new DogeHttpException("MEME_ALREADY_APPROVED", HttpStatus.BAD_REQUEST);
        }

        meme.setApprovedBy(principal);
        meme.setApprovedOn(LocalDateTime.now());

        final Meme approvedMeme = this.memeRepository.save(meme);
        this.notificationService.pushNotificationTo(
                Notification.builder()
                        .title("Your meme is public!")
                        .message("Your meme \"" + approvedMeme.getTitle() + "\" is approved by " + principal.getUsername())
                        .category(NotificationCategory.SUCCESS)
                        .build(),
                approvedMeme.getPublisher());

        return approvedMeme;
    }

    /**
     * Delete meme.
     *
     * @param memeId            id of the meme to be deleted.
     * @param principalUsername principal's username.
     */
    @Transactional
    public void deleteMeme(Long memeId, String principalUsername) {
        // retrieve confirmed user and meme from database
        final DogeUser principal = this.dogeUserService.getConfirmedUser(principalUsername);
        final Meme meme = this.getMeme(memeId, principalUsername);

        // check if principal is publisher or admin/moderator
        if (!meme.getPublisher().equals(principal) && !principal.isAdminOrModerator()) {
            throw new DogeHttpException("CAN_NOT_DELETE_FOREIGN_MEME", HttpStatus.UNAUTHORIZED);
        }

        // remove meme from the storage
        this.removeMeme(meme);

        // if publisher is different from the principal, push notification to the publisher
        if (!meme.getPublisher().equals(principal)) {
            this.notificationService.pushNotificationTo(
                    Notification.builder()
                            .title("Meme deleted!")
                            .message("Your meme \"" + meme.getTitle() + "\" has been deleted by " + principalUsername)
                            .category(NotificationCategory.DANGER)
                            .build(),
                    meme.getPublisher());
        }
    }

    /**
     * Reject meme upload request and delete it.
     *
     * @param memeId            id of the meme to be rejected.
     * @param principalUsername principal's username.
     */
    @Transactional
    public void rejectMeme(Long memeId, String principalUsername) {
        // retrieve meme from database
        final Meme meme = this.getMeme(memeId, principalUsername);

        // check if meme is already approved
        if (meme.isApproved()) {
            throw new DogeHttpException("MEME_ALREADY_APPROVED", HttpStatus.BAD_REQUEST);
        }

        // remove meme from the storage
        this.removeMeme(meme);

        // push notification to the meme publisher
        this.notificationService.pushNotificationTo(
                Notification.builder()
                        .title("Meme rejected!")
                        .message("Your meme \"" + meme.getTitle() + "\" has been rejected by " + principalUsername)
                        .category(NotificationCategory.DANGER)
                        .build(),
                meme.getPublisher());
    }

    /**
     * Remove meme from the database and the cloud storage
     *
     * @param meme meme to be deleted
     */
    private void removeMeme(Meme meme) {
        this.cloudStorageService.remove(meme.getImageKey(), "meme");
        this.memeRepository.delete(meme);
    }
}
