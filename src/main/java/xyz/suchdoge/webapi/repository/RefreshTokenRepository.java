package xyz.suchdoge.webapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.suchdoge.webapi.model.token.RefreshToken;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> getByHashedToken(String hashedToken);

    int countAllByUserUsername(String username);

    Collection<RefreshToken> getAllByUserUsername(String username);
}
