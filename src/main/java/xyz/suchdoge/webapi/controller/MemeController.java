package xyz.suchdoge.webapi.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.suchdoge.webapi.dto.meme.MemeCountDto;
import xyz.suchdoge.webapi.dto.meme.MemeDataDto;
import xyz.suchdoge.webapi.dto.meme.MemeListDto;
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

    @PostMapping
    public void postMeme(@RequestParam MultipartFile image,
                         @ModelAttribute MemeDataDto memeDto,
                         Principal principal) {
        this.memeService.createMeme(image, memeMapper.memeDataDtoToMeme(memeDto), principal.getName());
    }

    @GetMapping
    public MemeListDto getMemes(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "5") int size) {
        Collection<Meme> memes = this.memeService.getMemes(page, size);
        return new MemeListDto(memes.stream()
                .map(memeMapper::memeToMemeResponseDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("count")
    public MemeCountDto getMemeCount() {
        return new MemeCountDto(this.memeService.getApprovedMemeCount());
    }
}
