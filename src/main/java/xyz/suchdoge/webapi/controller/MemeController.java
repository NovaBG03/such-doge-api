package xyz.suchdoge.webapi.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.suchdoge.webapi.dto.meme.MemeDataDto;
import xyz.suchdoge.webapi.dto.meme.MemeResponseDto;
import xyz.suchdoge.webapi.mapper.meme.MemeMapper;
import xyz.suchdoge.webapi.model.Meme;
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

    @PostMapping()
    public MemeResponseDto postMeme(@RequestParam MultipartFile image,
                                    @RequestBody MemeDataDto memeDto,
                                    Principal principal) {
        final Meme meme = this.memeService
                .createMeme(image, memeMapper.memeDataDtoToMeme(memeDto), principal.getName());

        return this.memeMapper.memeToMemeResponseDto(meme);
    }
}
