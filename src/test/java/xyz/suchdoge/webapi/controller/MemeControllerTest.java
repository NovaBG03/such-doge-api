package xyz.suchdoge.webapi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import xyz.suchdoge.webapi.dto.meme.request.MemeDataDto;
import xyz.suchdoge.webapi.dto.meme.filter.MemeOrderFilter;
import xyz.suchdoge.webapi.dto.meme.filter.MemePublishFilter;
import xyz.suchdoge.webapi.mapper.meme.MemeMapper;
import xyz.suchdoge.webapi.model.Meme;
import xyz.suchdoge.webapi.model.user.DogeRoleLevel;
import xyz.suchdoge.webapi.service.MemeService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class MemeControllerTest {
    @MockBean
    MemeService memeService;
    @MockBean
    MemeMapper memeMapper;

    @Autowired
    WebApplicationContext context;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Should get all memes")
    void shouldGetAllMemes() throws Exception {
        int page = 1;
        int size = 12;
        MemePublishFilter publishFilter = MemePublishFilter.APPROVED;
        MemeOrderFilter orderFilter = MemeOrderFilter.NEWEST;
        String publisher = "publisher";
        String principal = "principal";

        mvc.perform(get("/api/v1/meme")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("publishFilter", publishFilter.toString())
                        .param("orderFilter", orderFilter.toString())
                        .param("publisher", publisher)
                        .with(user(principal)))
                .andExpect(status().isOk());

        ArgumentCaptor<PageRequest> pageArgumentCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(memeService).getMemes(pageArgumentCaptor.capture(), eq(publishFilter), eq(orderFilter), eq(publisher), eq(principal));
        assertThat(pageArgumentCaptor.getValue())
                .matches(x -> x.getPageNumber() == page, "is correct page")
                .matches(x -> x.getPageSize() == size, "is correct size");
    }

    @Test
    @DisplayName("Should post meme")
    void shouldPostMeme() throws Exception {
        String username = "ivan";
        MockMultipartFile image = new MockMultipartFile("image", new byte[0]);
        String title = "title";
        String description = "desc";
        Meme meme = Meme.builder().title(title).description(description).build();

        when(memeMapper.memeDataDtoToMeme(any())).thenReturn(meme);

        mvc.perform(multipart("/api/v1/meme")
                        .file(image)
                        .param("title", title)
                        .param("description", description)
                        .with(user(username)))
                .andExpect(status().isOk());

        ArgumentCaptor<MemeDataDto> dtoArgumentCaptor = ArgumentCaptor.forClass(MemeDataDto.class);
        verify(memeMapper).memeDataDtoToMeme(dtoArgumentCaptor.capture());
        assertThat(dtoArgumentCaptor.getValue())
                .matches(x -> x.getTitle().equals(title), "is title set")
                .matches(x -> x.getDescription().equals(description), "is description set");

        verify(memeService).createMeme(image, meme, username);
    }

    @Test
    @DisplayName("Should approve meme admin")
    void shouldApproveMemeAdmin() throws Exception {
        String username = "ivan";
        Long memeId = 1L;
        mvc.perform(post("/api/v1/meme/approve/" + memeId)
                        .with(user(username)
                                .roles(DogeRoleLevel.ADMIN.toString())))
                .andExpect(status().isOk());

        verify(memeService).approveMeme(memeId, username);
    }

    @Test
    @DisplayName("Should not allow approve meme user")
    void shouldNotAllowApproveMemeUser() throws Exception {
        String username = "ivan";
        long memeId = 1L;
        mvc.perform(post("/api/v1/meme/approve/" + memeId)
                        .with(user(username)
                                .roles(DogeRoleLevel.USER.toString())))
                .andExpect(status().isForbidden());

        verify(memeService, never()).approveMeme(anyLong(), anyString());
    }

    @Test
    @DisplayName("Should reject meme admin")
    void shouldRejectMemeAdmin() throws Exception {
        String username = "ivan";
        Long memeId = 1L;
        mvc.perform(delete("/api/v1/meme/reject/" + memeId)
                        .with(user(username)
                                .roles(DogeRoleLevel.ADMIN.toString())))
                .andExpect(status().isOk());

        verify(memeService).rejectMeme(memeId, username);
    }

    @Test
    @DisplayName("Should not allow reject meme user")
    void shouldNotAllowRejectMemeUser() throws Exception {
        String username = "ivan";
        long memeId = 1L;
        mvc.perform(delete("/api/v1/meme/reject/" + memeId)
                        .with(user(username)
                                .roles(DogeRoleLevel.USER.toString())))
                .andExpect(status().isForbidden());

        verify(memeService, never()).rejectMeme(anyLong(), anyString());
    }

    @Test
    @DisplayName("Should delete meme")
    void shouldDeleteMeme() throws Exception {
        String username = "ivan";
        Long memeId = 1L;
        mvc.perform(delete("/api/v1/meme/" + memeId)
                        .with(user(username)))
                .andExpect(status().isOk());

        verify(memeService).deleteMeme(memeId, username);
    }
}
