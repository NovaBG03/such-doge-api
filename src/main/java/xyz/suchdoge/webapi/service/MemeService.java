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
import xyz.suchdoge.webapi.service.validator.ModelValidatorService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class MemeService {
    private final MemeRepository memeRepository;
    private final DogeUserService dogeUserService;
    private final ModelValidatorService modelValidatorService;

    public MemeService(MemeRepository memeRepository,
                       DogeUserService dogeUserService,
                       ModelValidatorService modelValidatorService) {
        this.memeRepository = memeRepository;
        this.dogeUserService = dogeUserService;
        this.modelValidatorService = modelValidatorService;
    }

    public long getApprovedMemeCount() {
        return this.memeRepository.countByApprovedOnNotNull();
    }

    public Collection<Meme> getMemes(int page, int size) {
        final PageRequest pageRequest = PageRequest
                .of(page, size, Sort.by(Sort.Direction.DESC, "approvedOn"));

        Page<Meme> memePage = this.memeRepository.findAllByApprovedOnNotNull(pageRequest);

        return memePage.stream().collect(Collectors.toList());
    }

    public Meme getMeme(Long memeId, String principalUsername) {
        final Meme meme = this.memeRepository.getById(memeId);

        final DogeUser user = this.dogeUserService.getUserByUsername(principalUsername);
        if (meme != null && (meme.isApproved() || user.isAdminOrModerator())) {
            return meme;
        }

        throw new DogeHttpException("MEME_ID_INVALID", HttpStatus.NOT_FOUND);
    }

    public Meme createMeme(MultipartFile image, Meme meme, String principalUsername) {
        try {
            meme.setImage(image.getBytes());
        } catch (IOException e) {
            throw new DogeHttpException("CAN_NOT_READ_IMAGE_BYTES", HttpStatus.BAD_REQUEST);
        }

        final DogeUser publisher = this.dogeUserService.getUserByUsername(principalUsername);
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
        if (!user.isAdminOrModerator()) {
            throw new DogeHttpException("APPROVE_MEME_USER_NOT_AUTHORISED", HttpStatus.FORBIDDEN);
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
