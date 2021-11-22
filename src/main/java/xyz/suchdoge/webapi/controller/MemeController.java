package xyz.suchdoge.webapi.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.suchdoge.webapi.dto.meme.MemeCountDto;
import xyz.suchdoge.webapi.dto.meme.MemeDataDto;
import xyz.suchdoge.webapi.dto.meme.MemeListDto;
import xyz.suchdoge.webapi.dto.meme.MemeMyListDto;
import xyz.suchdoge.webapi.mapper.meme.MemeMapper;
import xyz.suchdoge.webapi.model.Meme;
import xyz.suchdoge.webapi.service.MemeService;

import java.security.Principal;
import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/meme")
public class MemeController {
    private final MemeService memeService;
    private final MemeMapper memeMapper;

    public MemeController(MemeService memeService, MemeMapper memeMapper) {
        this.memeService = memeService;
        this.memeMapper = memeMapper;
    }

    @GetMapping("/public/count")
    public MemeCountDto getMemesCount() {
        return new MemeCountDto(this.memeService.getApprovedMemesCount());
    }

    @GetMapping("/public")
    public MemeListDto getMemes(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "5") int size) {
        Collection<Meme> memes = this.memeService.getMemes(page, size);
        return new MemeListDto(memes.stream()
                .map(memeMapper::memeToMemeResponseDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/my/count")
    public MemeCountDto getMyMemesCount(@RequestParam(name = "approved", defaultValue = "true") boolean isApproved,
                                        @RequestParam(name = "pending", defaultValue = "true") boolean isPending,
                                        Principal principal) {
        return new MemeCountDto(this.memeService.getMyMemeCount(isApproved, isPending, principal.getName()));
    }

    @GetMapping("/my")
    public MemeMyListDto getMyMemes(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "5") int size,
                                    @RequestParam(name = "approved", defaultValue = "true") boolean isApproved,
                                    @RequestParam(name = "pending", defaultValue = "true") boolean isPending,
                                    Principal principal) {
        Collection<Meme> memes = this.memeService
                .getPrincipalMemes(page, size, isApproved, isPending, principal.getName());

        return new MemeMyListDto(memes.stream()
                .map(memeMapper::memeToMemeMyResponseDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/pending/count")
    @Secured({"ROLE_ADMIN", "ROLE_MODERATOR"})
    public MemeCountDto getPendingMemesCount() {
        return new MemeCountDto(this.memeService.getNotApprovedMemesCount());
    }

    @GetMapping("/pending")
    @Secured({"ROLE_ADMIN", "ROLE_MODERATOR"})
    public MemeListDto getNotApprovedMemes(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "5") int size) {
        Collection<Meme> memes = this.memeService.getNotApprovedMemes(page, size);
        return new MemeListDto(memes.stream()
                .map(memeMapper::memeToMemeResponseDto)
                .collect(Collectors.toList()));
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
}
