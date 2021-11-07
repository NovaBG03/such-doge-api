package xyz.suchdoge.webapi.bootstrap;

import com.google.common.collect.Lists;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.model.DogeRole;
import xyz.suchdoge.webapi.model.DogeRoleLevel;
import xyz.suchdoge.webapi.model.DogeUser;
import xyz.suchdoge.webapi.repository.DogeRoleRepository;
import xyz.suchdoge.webapi.repository.DogeUserRepository;

import java.time.LocalDateTime;

@Component
public class Bootstrap implements CommandLineRunner {
    private final PasswordEncoder passwordEncoder;
    private final DogeUserRepository dogeUserRepository;
    private final DogeRoleRepository dogeRoleRepository;

    public Bootstrap(PasswordEncoder passwordEncoder,
                     DogeUserRepository dogeUserRepository,
                     DogeRoleRepository dogeRoleRepository) {
        this.passwordEncoder = passwordEncoder;
        this.dogeUserRepository = dogeUserRepository;
        this.dogeRoleRepository = dogeRoleRepository;
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
        DogeRole userRole = DogeRole.builder().level(DogeRoleLevel.USER).build();
        dogeRoleRepository.save(userRole);

        DogeRole moderatorRole = DogeRole.builder().level(DogeRoleLevel.MODERATOR).build();
        dogeRoleRepository.save(moderatorRole);

        DogeRole adminRole = DogeRole.builder().level(DogeRoleLevel.ADMIN).build();
        dogeRoleRepository.save(adminRole);
    }

    private void loadUsers() {
        DogeRole userRole = this.dogeRoleRepository.getByLevel(DogeRoleLevel.USER);
        DogeRole moderatorRole = this.dogeRoleRepository.getByLevel(DogeRoleLevel.MODERATOR);
        DogeRole adminRole = this.dogeRoleRepository.getByLevel(DogeRoleLevel.ADMIN);

        DogeUser user = DogeUser.builder()
                .username("ivan")
                .email("ivan@abv.bg")
                .encodedPassword(passwordEncoder.encode("Ivan123"))
                .enabledAt(LocalDateTime.now())
                .build();
        user.addRole(userRole);
        dogeUserRepository.save(user);

        DogeUser moderator = DogeUser.builder()
                .username("moderen")
                .email("mod@abv.bg")
                .encodedPassword(passwordEncoder.encode("Moderen123"))
                .enabledAt(LocalDateTime.now())
                .build();
        moderator.addRoles(Lists.newArrayList(userRole, moderatorRole));
        dogeUserRepository.save(moderator);

        DogeUser admin = DogeUser.builder()
                .username("admin")
                .email("admin@abv.bg")
                .encodedPassword(passwordEncoder.encode("Admin123"))
                .enabledAt(LocalDateTime.now())
                .build();
        admin.addRoles(Lists.newArrayList(userRole, moderatorRole, adminRole));
        dogeUserRepository.save(admin);
    }
}
