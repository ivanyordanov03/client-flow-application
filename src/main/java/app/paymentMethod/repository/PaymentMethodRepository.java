package app.paymentMethod.repository;

import app.paymentMethod.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    Optional<PaymentMethod> getByCreditCardNumberAndExpirationDate(String creditCardNumber, String expirationDate);
}
