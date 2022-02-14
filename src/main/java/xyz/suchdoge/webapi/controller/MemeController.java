package xyz.suchdoge.webapi.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.suchdoge.webapi.dto.meme.MemeDataDto;
import xyz.suchdoge.webapi.dto.meme.MemePageResponseDto;
import xyz.suchdoge.webapi.mapper.meme.MemeMapper;
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
//            & type=all/approved/pending (default approved)
//	          & publisher=username (default all users)
    @GetMapping()
    public MemePageResponseDto getMemes(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "4") int size,
                                        @RequestParam(defaultValue = "approved") String type,
                                        @RequestParam(name = "publisher", required = false) String publisherUsername,
                                        @AuthenticationPrincipal() String principalUsername) {
        final Sort sort = Sort.by(Sort.Direction.DESC, "approvedOn", "publishedOn");
        final PageRequest pageRequest = PageRequest.of(page, size, sort);
        return this.memeService.getMemes(pageRequest, type, publisherUsername, principalUsername);
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

    // todo think about different ways of deleting memes
    // todo create delete/deny meme endpoints
    // deny - allow admin/moderator to delete approved or not memes
    // delete - allow user to delete their approved or not memes
}
