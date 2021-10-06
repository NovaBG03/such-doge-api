package xyz.suchdoge.webapi.bootstrap;

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
        DogeUser user = DogeUser.builder()
                .username("ivan")
                .email("ivan@abv.bg")
                .encodedPassword(passwordEncoder.encode("Ivan123"))
                .enabledAt(LocalDateTime.now())
                .build();

        DogeRole userRole = this.dogeRoleRepository.getByLevel(DogeRoleLevel.USER);

        user.addRole(userRole);

        dogeUserRepository.save(user);
    }
}
