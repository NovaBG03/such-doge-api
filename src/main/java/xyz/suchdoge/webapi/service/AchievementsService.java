package xyz.suchdoge.webapi.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.dto.user.AchievementsResponseDto;
import xyz.suchdoge.webapi.service.donation.DonationService;

@Service
public class AchievementsService {
    private final MemeService memeService;
    private final DonationService donationService;

    public AchievementsService(MemeService memeService, DonationService donationService) {
        this.memeService = memeService;
        this.donationService = donationService;
    }

    public AchievementsResponseDto getAchievements(String username) throws UsernameNotFoundException {
        return AchievementsResponseDto.builder()
                .username(username)
                .memesUploaded(memeService.getMemesCount(username))
                .donationsReceived(donationService.getDonationsReceived(username))
                .donationsSent(donationService.getDonationsSent(username))
                .build();
    }
}
