package xyz.suchdoge.webapi.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import xyz.suchdoge.webapi.model.DogeUser;
import xyz.suchdoge.webapi.repository.DogeUserRepository;

import java.time.LocalDateTime;

@Component
public class Bootstrap implements CommandLineRunner {
    private final PasswordEncoder passwordEncoder;
    private final DogeUserRepository dogeUserRepository;

    public Bootstrap(PasswordEncoder passwordEncoder, DogeUserRepository dogeUserRepository) {
        this.passwordEncoder = passwordEncoder;
        this.dogeUserRepository = dogeUserRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        loadUsers();
    }

    private void loadUsers() {
        DogeUser user = DogeUser.builder()
                .username("ivan")
                .email("ivan@abv.bg")
                .encodedPassword(passwordEncoder.encode("ivan"))
                .enabledAt(LocalDateTime.now())
                .build();

        dogeUserRepository.save(user);
    }
}
