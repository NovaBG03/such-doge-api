package xyz.suchdoge.webapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.suchdoge.webapi.model.token.EmailConfirmationToken;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailConfirmationTokenRepository extends JpaRepository<EmailConfirmationToken, UUID> {
    Optional<EmailConfirmationToken> getByHashedToken(String hashedToken);
}
