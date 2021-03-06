package xyz.suchdoge.webapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xyz.suchdoge.webapi.model.user.DogeRole;
import xyz.suchdoge.webapi.model.user.DogeRoleLevel;

@Repository
public interface DogeRoleRepository extends JpaRepository<DogeRole, Integer> {
    DogeRole getByLevel(DogeRoleLevel level);
}
