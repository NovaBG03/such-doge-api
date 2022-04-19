package xyz.suchdoge.webapi.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.suchdoge.webapi.dto.meme.filter.MemeOrderFilter;
import xyz.suchdoge.webapi.dto.meme.filter.MemePublishFilter;
import xyz.suchdoge.webapi.dto.meme.request.MemeDataDto;
import xyz.suchdoge.webapi.dto.meme.response.MemePageResponseDto;
import xyz.suchdoge.webapi.mapper.meme.MemeMapper;
import xyz.suchdoge.webapi.service.MemeService;

import java.security.Principal;

/**
 * Meme controller.
 *
 * @author Nikita
 */
@RestController
@RequestMapping("/api/v1/meme")
public class MemeController {
    private final MemeService memeService;
    private final MemeMapper memeMapper;

    public MemeController(MemeService memeService, MemeMapper memeMapper) {
        this.memeService = memeService;
        this.memeMapper = memeMapper;
    }

    /**
     * Get a page of memes by a specific creteria.
     *
     * @param page              default page 0
     * @param size              default size 4
     * @param publishFilter     default APPROVED
     * @param orderFilter       default NEWEST
     * @param publisherUsername default null -> all users
     * @param authentication    user authentication
     * @return meme page response dto.
     */
    @GetMapping()
    public MemePageResponseDto getMemes(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "4") int size,
                                        @RequestParam(defaultValue = "APPROVED") MemePublishFilter publishFilter,
                                        @RequestParam(defaultValue = "NEWEST") MemeOrderFilter orderFilter,
                                        @RequestParam(name = "publisher", required = false) String publisherUsername,
                                        Authentication authentication) {
        final PageRequest pageRequest = PageRequest.of(page, size);
        final String principalUsername = authentication != null ? authentication.getName() : null;
        return this.memeService.getMemes(pageRequest, publishFilter, orderFilter, publisherUsername, principalUsername);
    }

    @PostMapping
    public void postMeme(@RequestParam MultipartFile image,
                         @ModelAttribute MemeDataDto memeDto,
                         Principal principal) {
        this.memeService.createMeme(image, memeMapper.memeDataDtoToMeme(memeDto), principal.getName());
    }

    @PostMapping("/approve/{memeId}")
    @Secured({"ROLE_ADMIN", "ROLE_MODERATOR"})
    public void approveMeme(@PathVariable Long memeId, Principal principal) {
        this.memeService.approveMeme(memeId, principal.getName());
    }

    @DeleteMapping("/reject/{memeId}")
    @Secured({"ROLE_ADMIN", "ROLE_MODERATOR"})
    public void rejectMeme(@PathVariable Long memeId, Principal principal) {
        this.memeService.rejectMeme(memeId, principal.getName());
    }

    @DeleteMapping("/{memeId}")
    public void deleteMeme(@PathVariable Long memeId, Principal principal) {
        this.memeService.deleteMeme(memeId, principal.getName());
    }
}
