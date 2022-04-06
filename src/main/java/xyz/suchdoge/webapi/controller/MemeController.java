package xyz.suchdoge.webapi.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.suchdoge.webapi.dto.meme.MemeDataDto;
import xyz.suchdoge.webapi.dto.meme.MemePageResponseDto;
import xyz.suchdoge.webapi.dto.meme.filter.MemeOrderFilter;
import xyz.suchdoge.webapi.mapper.meme.MemeMapper;
import xyz.suchdoge.webapi.dto.meme.filter.MemePublishFilter;
import xyz.suchdoge.webapi.service.MemeService;

import java.security.Principal;

@RestController
@RequestMapping("/meme")
public class MemeController {
    private final MemeService memeService;
    private final MemeMapper memeMapper;

    public MemeController(MemeService memeService, MemeMapper memeMapper) {
        this.memeService = memeService;
        this.memeMapper = memeMapper;
    }

    //    GET meme? page=0 (default 0) (greater or equal 0)
    //            & size=0 (default 5) (greater or equal 1)
    //            & type=ALL,
    //                   APPROVED, (default)
    //                   PENDING,
    //	          & publishFilter=username (default all users)
    //            & orderFilter=NEWEST, (default)
    //                    OLDEST,
    //                    LATEST_TIPPED,
    //                    MOST_TIPPED,
    //                    TOP_TIPPED_LAST_3_DAYS,
    //                    TOP_TIPPED_LAST_WEEK,
    //                    TOP_TIPPED_LAST_MONTH,
    @GetMapping()
    public MemePageResponseDto getMemes(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "4") int size,
                                        @RequestParam(defaultValue = "APPROVED") MemePublishFilter publishFilter,
                                        @RequestParam(defaultValue = "NEWEST") MemeOrderFilter orderFilter,
                                        @RequestParam(name = "publisher", required = false) String publisherUsername,
                                        @AuthenticationPrincipal() String principalUsername) {
        final PageRequest pageRequest = PageRequest.of(page, size);
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
