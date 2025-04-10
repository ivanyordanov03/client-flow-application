package app.user.repository;

import app.user.model.User;
import app.user.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    List<User> findAllByAccountIdAndArchivedIsFalseOrderByUserRoleAscFirstNameAscLastNameAsc(UUID accountId);

    List<User> findAllByAccountIdAndArchivedIsTrueOrderByUserRoleAscFirstNameAscLastNameAsc(UUID accountId);

    List<User> findAllByAccountId(UUID accountId);

    List<User> findAllByAccountIdAndUserRole(UUID accountId, UserRole userRole);

    Optional<User> getUserByEmail(String email);
}
