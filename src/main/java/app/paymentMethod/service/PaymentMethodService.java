package app.paymentMethod.service;

import app.account.model.Account;
import app.paymentMethod.model.PaymentMethod;
import app.paymentMethod.repository.PaymentMethodRepository;
import app.user.model.User;
import app.web.dto.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    @Autowired
    public PaymentMethodService(PaymentMethodRepository paymentMethodRepository) {

        this.paymentMethodRepository = paymentMethodRepository;
    }

    public void createNew(PaymentRequest paymentRequest, UUID accountId) {

        if(paymentMethodRepository.getByCreditCardNumberAndExpirationDate(paymentRequest.getCardNumber(),
                paymentRequest.getExpirationDate()).isEmpty()) {

            PaymentMethod paymentMethod = initiate(paymentRequest, accountId);
            paymentMethodRepository.save(paymentMethod);
            log.info("New payment method added for account id [%s]".formatted(accountId) );
        }

    }

    private PaymentMethod initiate(PaymentRequest paymentRequest, UUID accountId) {

        return PaymentMethod.builder()
                .creditCardNumber(paymentRequest.getCardNumber())
                .cardHolderName(paymentRequest.getCardholderName())
                .expirationDate(paymentRequest.getExpirationDate())
                .CVV(paymentRequest.getCVV())
                .accountId(accountId)
                .build();
    }

    public List<PaymentMethod> getAllByAccountId(UUID accountId) {
        return paymentMethodRepository.findAllByAccountId(accountId);
    }
}
