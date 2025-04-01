package app.account.repository;

import app.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByOwnerId(UUID ownerId);

    List<Account> findAllByDateExpiringIsLessThanEqualAndAutoRenewalEnabledIsTrue(LocalDateTime localDateTime);

    List<Account> findAllByDateExpiringIsLessThanEqual(LocalDateTime dateExpiringIsLessThan);
}
