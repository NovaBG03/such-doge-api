package xyz.suchdoge.webapi.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.suchdoge.webapi.dto.meme.MemePageResponseDto;
import xyz.suchdoge.webapi.dto.meme.filter.MemeOrderFilter;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.mapper.meme.MemeMapper;
import xyz.suchdoge.webapi.dto.meme.filter.MemePublishFilter;
import xyz.suchdoge.webapi.model.notification.Notification;
import xyz.suchdoge.webapi.model.notification.NotificationCategory;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.model.Meme;
import xyz.suchdoge.webapi.repository.MemeRepository;
import xyz.suchdoge.webapi.service.storage.CloudStorageService;
import xyz.suchdoge.webapi.service.storage.StoragePath;
import xyz.suchdoge.webapi.service.validator.ModelValidatorService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for managing memes.
 *
 * @author Nikita
 */
@Service
public class MemeService {
    private final MemeRepository memeRepository;
    private final DogeUserService userService;
    private final CloudStorageService cloudStorageService;
    private final NotificationService notificationService;
    private final ModelValidatorService modelValidatorService;
    private final MemeMapper memeMapper;

    /**
     * Constructs new instance with needed dependencies.
     */
    public MemeService(MemeRepository memeRepository,
                       DogeUserService userService,
                       CloudStorageService cloudStorageService,
                       NotificationService notificationService,
                       ModelValidatorService modelValidatorService,
                       MemeMapper memeMapper) {
        this.memeRepository = memeRepository;
        this.userService = userService;
        this.cloudStorageService = cloudStorageService;
        this.notificationService = notificationService;
        this.modelValidatorService = modelValidatorService;
        this.memeMapper = memeMapper;
    }

    /**
     * Get count of public memes published by a specific user.
     *
     * @param username memes publisher.
     * @return count of public memes.
     * @throws UsernameNotFoundException if the user does not exist.
     */
    public Long getMemesCount(String username) throws UsernameNotFoundException {
        userService.getUserByUsername(username);
        return memeRepository.countByPublisherUsernameAndApprovedOnNotNull(username);
    }

    /**
     * Get page of memes.
     *
     * @param pageRequest       selected page and size.
     * @param publishFilter     meme publish filter.
     * @param orderFilter       meme order filter.
     * @param publisherUsername publisher username or null.
     * @param principalUsername principal username or null.
     * @return page of memes.
     * @throws DogeHttpException when can not get memes.
     */
    public MemePageResponseDto getMemes(PageRequest pageRequest,
                                        MemePublishFilter publishFilter,
                                        MemeOrderFilter orderFilter,
                                        String publisherUsername,
                                        String principalUsername) throws DogeHttpException {
        // retrieve user from database
        DogeUser principal;
        try {
            principal = this.userService.getUserByUsername(principalUsername);
        } catch (UsernameNotFoundException e) {
            principal = null;
        }

        final boolean isPublisherOrAdmin = principal != null
                && (principal.isAdminOrModerator() || principal.getUsername().equals(publisherUsername));

        // properties to be populated after the filtration
        Page<Meme> memePage;

        // determine and get requested memes
        switch (publishFilter) {
            case APPROVED:
                // only approved memes are requested
                memePage = this.getApprovedMemes(publisherUsername, pageRequest, orderFilter);
                break;
            case PENDING:
                // only the publisher and admins/moderators have access to the pending memes
                if (!isPublisherOrAdmin) {
                    throw new DogeHttpException("MEME_FILTER_NOT_ALLOWED", HttpStatus.FORBIDDEN);
                }

                // only pending memes requested
                memePage = this.getPendingMemes(publisherUsername, pageRequest, orderFilter);
                break;
            case ALL:
                // only the publisher and admins/moderators have access to the pending memes
                if (!isPublisherOrAdmin) {
                    throw new DogeHttpException("MEME_FILTER_NOT_ALLOWED", HttpStatus.FORBIDDEN);
                }

                // all memes requested
                memePage = this.getAllMemes(publisherUsername, pageRequest, orderFilter);
                break;
            default:
                // no filter type provided
                throw new DogeHttpException("MEME_FILTER_NOT_ALLOWED", HttpStatus.FORBIDDEN);
        }

        return memeMapper.createMemePageResponseDto(memePage, isPublisherOrAdmin);
    }

    /**
     * Get a specific meme.
     *
     * @param memeId            meme id.
     * @param principalUsername principal username or null.
     * @return meme.
     * @throws DogeHttpException when can not find meme with that id.
     */
    public Meme getMeme(Long memeId, String principalUsername) throws DogeHttpException {
        if (principalUsername == null) {
            return this.memeRepository.findByIdAndApprovedOnNotNull(memeId)
                    .orElseThrow(() -> new DogeHttpException("MEME_ID_INVALID", HttpStatus.NOT_FOUND));
        }

        final Meme meme = this.memeRepository.findById(memeId)
                .orElseThrow(() -> new DogeHttpException("MEME_ID_INVALID", HttpStatus.NOT_FOUND));

        final DogeUser user = this.userService.getUserByUsername(principalUsername);
        if (meme.isApproved()
                || user.isAdminOrModerator()
                || user.equals(meme.getPublisher())) {
            return meme;
        }

        throw new DogeHttpException("MEME_ID_INVALID", HttpStatus.NOT_FOUND);
    }

    /**
     * Create new meme.
     *
     * @param image             meme image.
     * @param meme              meme object.
     * @param principalUsername publisher username.
     * @throws DogeHttpException when can not create meme.
     */
    @Transactional
    public void createMeme(MultipartFile image, Meme meme, String principalUsername) throws DogeHttpException {
        final DogeUser publisher = this.userService.getConfirmedUser(principalUsername);

        try {
            final String imageId = UUID.randomUUID() + ".png";
            this.cloudStorageService.upload(image.getBytes(), imageId, StoragePath.MEME);
            meme.setImageKey(imageId);
        } catch (IOException e) {
            throw new DogeHttpException("CAN_NOT_READ_IMAGE_BYTES", HttpStatus.BAD_REQUEST);
        }

        meme.setId(null);
        meme.setPublisher(publisher);
        meme.setApprovedBy(null);
        meme.setApprovedOn(null);
        meme.setPublishedOn(LocalDateTime.now());

        if (meme.getDescription() == null
                || meme.getDescription().length() == 0
                || meme.getDescription().equals("null")) {
            meme.setDescription(null);
        }

        this.modelValidatorService.validate(meme);
        this.memeRepository.save(meme);
    }

    /**
     * Approve meme.
     *
     * @param memeId            meme id.
     * @param principalUsername principal username.
     * @throws DogeHttpException when can not approve meme.
     */
    @Transactional
    public void approveMeme(Long memeId, String principalUsername) throws DogeHttpException {
        final DogeUser principal = this.userService.getConfirmedUser(principalUsername);
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
    }

    /**
     * Reject meme upload request and delete it.
     *
     * @param memeId            id of the meme to be rejected.
     * @param principalUsername principal's username.
     * @throws DogeHttpException when can not reject meme.
     */
    @Transactional
    public void rejectMeme(Long memeId, String principalUsername) throws DogeHttpException {
        // retrieve meme from database
        final DogeUser principal = this.userService.getConfirmedUser(principalUsername);
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
                        .message("Your meme \"" + meme.getTitle() + "\" has been rejected by " + principal.getUsername())
                        .category(NotificationCategory.DANGER)
                        .build(),
                meme.getPublisher());
    }

    /**
     * Delete meme.
     *
     * @param memeId            id of the meme to be deleted.
     * @param principalUsername principal's username.
     * @throws DogeHttpException when can not delete meme.
     */
    @Transactional
    public void deleteMeme(Long memeId, String principalUsername) throws DogeHttpException {
        // retrieve confirmed user and meme from database
        final DogeUser principal = this.userService.getConfirmedUser(principalUsername);
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
     * Remove meme from the database and the cloud storage.
     *
     * @param meme meme to be deleted.
     */
    private void removeMeme(Meme meme) {
        this.cloudStorageService.remove(meme.getImageKey(), StoragePath.MEME);
        this.memeRepository.delete(meme);
    }

    private Page<Meme> getApprovedMemes(String publisherUsername, PageRequest pageRequest, MemeOrderFilter orderFilter) {
        try {
            pageRequest = getPageRequestWithBasicOrderFilter(pageRequest, orderFilter);
        } catch (DogeHttpException e) {
            return this.getApprovedMemesWithAdvanceOrderFilter(publisherUsername, pageRequest, orderFilter);
        }

        if (publisherUsername != null) {
            // approved memes from a specific user
            return this.memeRepository.findAllByPublisherUsernameAndApprovedOnNotNull(publisherUsername, pageRequest);
        }
        // approved memes from all users
        return this.memeRepository.findAllByApprovedOnNotNull(pageRequest);
    }

    private Page<Meme> getApprovedMemesWithAdvanceOrderFilter(String publisherUsername, PageRequest pageRequest, MemeOrderFilter orderFilter) {
        int daysFromNow = 0;
        switch (orderFilter) {
            case LATEST_TIPPED:
                if (publisherUsername != null) {
                    return this.memeRepository.findAllByPublisherUsernameApprovedOnNotNullOrderByLatestTipped(publisherUsername, pageRequest);
                }
                return this.memeRepository.findAllByApprovedOnNotNullOrderByLatestTipped(pageRequest);
            case MOST_TIPPED:
                if (publisherUsername != null) {
                    return this.memeRepository.findAllByPublisherUsernameApprovedOnNotNullOrderByMostTipped(publisherUsername, pageRequest);
                }
                return this.memeRepository.findAllByApprovedOnNotNullOrderByMostTipped(pageRequest);
            case TOP_TIPPED_LAST_3_DAYS:
                daysFromNow = 3;
                break;
            case TOP_TIPPED_LAST_WEEK:
                daysFromNow = 7;
                break;
            case TOP_TIPPED_LAST_MONTH:
                daysFromNow = 30;
                break;
        }

        if (publisherUsername == null && daysFromNow > 0) {
            return this.memeRepository.findAllByApprovedOnNotNullOrderByTopTipped(pageRequest, daysFromNow);
        }

        throw new DogeHttpException("MEME_FILTER_NOT_ALLOWED", HttpStatus.FORBIDDEN);
    }

    private Page<Meme> getPendingMemes(String publisherUsername, PageRequest pageRequest, MemeOrderFilter orderFilter) {
        pageRequest = getPageRequestWithBasicOrderFilter(pageRequest, orderFilter);

        if (publisherUsername != null) {
            // pending memes from a specific user
            return this.memeRepository.findAllByPublisherUsernameAndApprovedOnNull(publisherUsername, pageRequest);
        }

        // pending memes from all users
        return this.memeRepository.findAllByApprovedOnNull(pageRequest);
    }

    private Page<Meme> getAllMemes(String publisherUsername, PageRequest pageRequest, MemeOrderFilter orderFilter) {
        pageRequest = getPageRequestWithBasicOrderFilter(pageRequest, orderFilter);

        if (publisherUsername != null) {
            // all memes from a specific user
            return this.memeRepository.findAllByPublisherUsername(publisherUsername, pageRequest);
        }

        // all memes from all users
        return this.memeRepository.findAll(pageRequest);
    }

    private PageRequest getPageRequestWithBasicOrderFilter(PageRequest pageRequest, MemeOrderFilter orderFilter) {
        switch (orderFilter) {
            case NEWEST:
                return pageRequest.withSort(Sort.by(Sort.Direction.DESC, "approvedOn", "publishedOn"));
            case OLDEST:
                return pageRequest.withSort(Sort.by(Sort.Direction.ASC, "approvedOn", "publishedOn"));
            default:
                throw new DogeHttpException("MEME_FILTER_NOT_ALLOWED", HttpStatus.FORBIDDEN);
        }
    }
}
