package xyz.suchdoge.webapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.suchdoge.webapi.model.DogeUser;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DogeUserRepository extends JpaRepository<DogeUser, UUID> {
    Optional<DogeUser> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
