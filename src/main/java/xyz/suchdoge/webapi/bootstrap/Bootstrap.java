package xyz.suchdoge.webapi.bootstrap;

import com.google.common.collect.Lists;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.model.user.DogeRole;
import xyz.suchdoge.webapi.model.user.DogeRoleLevel;
import xyz.suchdoge.webapi.model.user.DogeUser;
import xyz.suchdoge.webapi.repository.DogeRoleRepository;
import xyz.suchdoge.webapi.repository.DogeUserRepository;
import xyz.suchdoge.webapi.service.imageGenerator.ImageGeneratorService;
import xyz.suchdoge.webapi.service.storage.CloudStorageService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class Bootstrap implements CommandLineRunner {
    private final PasswordEncoder passwordEncoder;
    private final DogeUserRepository dogeUserRepository;
    private final DogeRoleRepository dogeRoleRepository;
    private final ImageGeneratorService imageGeneratorService;
    private final CloudStorageService cloudStorageService;

    public Bootstrap(PasswordEncoder passwordEncoder,
                     DogeUserRepository dogeUserRepository,
                     DogeRoleRepository dogeRoleRepository,
                     ImageGeneratorService imageGeneratorService,
                     CloudStorageService cloudStorageService) {
        this.passwordEncoder = passwordEncoder;
        this.dogeUserRepository = dogeUserRepository;
        this.dogeRoleRepository = dogeRoleRepository;
        this.imageGeneratorService = imageGeneratorService;
        this.cloudStorageService = cloudStorageService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (dogeRoleRepository.count() == 0) {
            loadRoles();
        }

        if (dogeUserRepository.count() == 0) {
            loadUsers();
        }
    }

    private void loadRoles() {
        Iterable<DogeRole> roles = Arrays.stream(DogeRoleLevel.values())
                .map(dogeRoleLevel -> DogeRole.builder().level(dogeRoleLevel).build())
                .collect(Collectors.toList());

        dogeRoleRepository.saveAll(roles);
    }

    private void loadUsers() {
        Collection<DogeUser> users = new ArrayList<>();
        DogeRole userRole = this.dogeRoleRepository.getByLevel(DogeRoleLevel.USER);
        DogeRole moderatorRole = this.dogeRoleRepository.getByLevel(DogeRoleLevel.MODERATOR);
        DogeRole adminRole = this.dogeRoleRepository.getByLevel(DogeRoleLevel.ADMIN);

        DogeUser user = DogeUser.builder()
                .username("ivan")
                .email("ivan@abv.bg")
                .encodedPassword(passwordEncoder.encode("Ivan123"))
                .build();
        user.addRole(userRole);
        dogeUserRepository.save(user);
        users.add(user);

        DogeUser moderator = DogeUser.builder()
                .username("moderen")
                .email("mod@abv.bg")
                .encodedPassword(passwordEncoder.encode("Moderen123"))
                .build();
        moderator.addRoles(Lists.newArrayList(userRole, moderatorRole));
        dogeUserRepository.save(moderator);
        users.add(moderator);

        DogeUser admin = DogeUser.builder()
                .username("admin")
                .email("admin@abv.bg")
                .encodedPassword(passwordEncoder.encode("Admin123"))
                .build();
        admin.addRoles(Lists.newArrayList(userRole, moderatorRole, adminRole));
        dogeUserRepository.save(admin);
        users.add(admin);

        try {
            users.forEach(u -> cloudStorageService.upload(
                    imageGeneratorService.generateProfilePic(u.getUsername()),
                    u.getUsername() + ".png",
                    "user"));
        } catch (Exception e) {
        }
    }
}
