package xyz.suchdoge.webapi.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.suchdoge.webapi.exception.DogeHttpException;
import xyz.suchdoge.webapi.model.DogeUser;
import xyz.suchdoge.webapi.model.Meme;
import xyz.suchdoge.webapi.repository.MemeRepository;
import xyz.suchdoge.webapi.service.storage.CloudStorageService;
import xyz.suchdoge.webapi.service.validator.ModelValidatorService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MemeService {
    private final MemeRepository memeRepository;
    private final DogeUserService dogeUserService;
    private final CloudStorageService cloudStorageService;
    private final ModelValidatorService modelValidatorService;

    public MemeService(MemeRepository memeRepository,
                       DogeUserService dogeUserService,
                       CloudStorageService cloudStorageService,
                       ModelValidatorService modelValidatorService) {
        this.memeRepository = memeRepository;
        this.dogeUserService = dogeUserService;
        this.cloudStorageService = cloudStorageService;
        this.modelValidatorService = modelValidatorService;
    }

    public long getApprovedMemesCount() {
        return this.memeRepository.countByApprovedOnNotNull();
    }

    public long getNotApprovedMemesCount() {
        return this.memeRepository.countByApprovedOnNull();
    }

    public long getMyMemeCount(boolean isApproved, boolean isPending, String principalUsername) {
        if (!isApproved && !isPending) {
            throw new DogeHttpException("MEME_COUNT_FILTER_REQUEST_PARAMS_INVALID", HttpStatus.BAD_REQUEST);
        }

        if (isApproved && isPending) {
            return this.memeRepository.countByPublisherUsername(principalUsername);
        }

        if (isApproved) {
            return this.memeRepository.countByPublisherUsernameAndApprovedOnNotNull(principalUsername);
        }

        return this.memeRepository.countByPublisherUsernameAndApprovedOnNull(principalUsername);
    }

    public Collection<Meme> getMemes(int page, int size) {
        final PageRequest pageRequest = PageRequest
                .of(page, size, Sort.by(Sort.Direction.DESC, "approvedOn"));

        Page<Meme> memePage = this.memeRepository.findAllByApprovedOnNotNull(pageRequest);
        return memePage.stream().collect(Collectors.toList());
    }

    public Collection<Meme> getNotApprovedMemes(int page, int size) {
        final PageRequest pageRequest = PageRequest
                .of(page, size, Sort.by(Sort.Direction.DESC, "publishedOn"));

        Page<Meme> memePage = this.memeRepository.findAllByApprovedOnNull(pageRequest);
        return memePage.stream().collect(Collectors.toList());
    }

    public Collection<Meme> getPrincipalMemes(int page, int size, boolean isApproved, boolean isPending, String principalUsername) {
        if (!isApproved && !isPending) {
            throw new DogeHttpException("PRINCIPAL_MEMES_FILTER_REQUEST_PARAMS_INVALID", HttpStatus.BAD_REQUEST);
        }

        final PageRequest pageRequest = PageRequest.of(page, size);

        Page<Meme> memePage;

        if (isApproved && isPending) {
            memePage = this.memeRepository.findAllByPublisherUsername(principalUsername,
                    pageRequest.withSort(Sort.Direction.DESC, "approvedOn", "publishedOn"));
        } else if (isApproved) {
            memePage = this.memeRepository.findAllByPublisherUsernameAndApprovedOnNotNull(principalUsername,
                    pageRequest.withSort(Sort.Direction.DESC, "approvedOn"));
        } else {
            memePage = this.memeRepository.findAllByPublisherUsernameAndApprovedOnNull(principalUsername,
                    pageRequest.withSort(Sort.Direction.DESC, "publishedOn"));
        }

        return memePage.stream().collect(Collectors.toList());
    }

    public Meme getMeme(Long memeId, String principalUsername) {
        final Optional<Meme> optionalMeme = this.memeRepository.getOptionalById(memeId);

        final DogeUser user = this.dogeUserService.getUserByUsername(principalUsername);
        if (optionalMeme.isPresent() && (optionalMeme.get().isApproved() || user.isAdminOrModerator())) {
            return optionalMeme.get();
        }

        throw new DogeHttpException("MEME_ID_INVALID", HttpStatus.NOT_FOUND);
    }

    public Meme createMeme(MultipartFile image, Meme meme, String principalUsername) {
        final DogeUser publisher = this.dogeUserService.getUserByUsername(principalUsername);

        if (!publisher.isConfirmed()) {
            throw new DogeHttpException("USER_NOT_CONFIRMED", HttpStatus.METHOD_NOT_ALLOWED);
        }

        try {
            final String imageId = UUID.randomUUID() + ".png";
            this.cloudStorageService.upload(image.getBytes(), imageId);
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
        final DogeUser user = this.dogeUserService.getUserByUsername(principalUsername);
        if (!user.isConfirmed()) {
            throw new DogeHttpException("USER_NOT_CONFIRMED", HttpStatus.METHOD_NOT_ALLOWED);
        }

        final Meme meme = this.getMeme(memeId, principalUsername);

        if (meme.isApproved()) {
            throw new DogeHttpException("MEME_ALREADY_APPROVED", HttpStatus.BAD_REQUEST);
        }

        meme.setApprovedBy(user);
        meme.setApprovedOn(LocalDateTime.now());

        return this.memeRepository.save(meme);
    }
}
