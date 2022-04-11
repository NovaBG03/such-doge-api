package xyz.suchdoge.webapi.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import xyz.suchdoge.webapi.dto.user.AchievementResponseDto;
import xyz.suchdoge.webapi.dto.user.AchievementsListResponseDto;
import xyz.suchdoge.webapi.service.donation.DonationService;

import java.util.List;

/**
 * Service for user achievements management.
 *
 * @author Nikita
 */
@Service
public class AchievementsService {
    private final MemeService memeService;
    private final DonationService donationService;

    /**
     * Constructs new instance with needed dependencies.
     */
    public AchievementsService(MemeService memeService, DonationService donationService) {
        this.memeService = memeService;
        this.donationService = donationService;
    }

    /**
     * Get achievements for a specific user.
     *
     * @param username user to search for.
     * @return object containing all achievements of a specific user.
     * @throws UsernameNotFoundException when the user does not exist
     */
    public AchievementsListResponseDto getAchievements(String username) throws UsernameNotFoundException {
        return AchievementsListResponseDto.builder()
                .username(username)
                .achievements(List.of(
                        AchievementResponseDto.builder()
                                .name("Memes uploaded")
                                .value(memeService.getMemesCount(username).toString())
                                .build(),
                        AchievementResponseDto.builder()
                                .name("Donations received")
                                .value(donationService.getDonationsReceived(username).toString() + " DOGE")
                                .build(),
                        AchievementResponseDto.builder()
                                .name("Donations sent")
                                .value(donationService.getDonationsSent(username).toString() + " DOGE")
                                .build()
                )).build();
    }
}
