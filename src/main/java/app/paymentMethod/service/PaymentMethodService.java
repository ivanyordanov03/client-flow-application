package app.paymentMethod.service;

import app.account.model.Account;
import app.paymentMethod.model.PaymentMethod;
import app.paymentMethod.repository.PaymentMethodRepository;
import app.user.model.User;
import app.web.dto.PaymentRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;

    @Autowired
    public PaymentMethodService(PaymentMethodRepository paymentMethodRepository) {

        this.paymentMethodRepository = paymentMethodRepository;
    }

    public void createNew(PaymentRequest paymentRequest, Account account) {

        if(paymentMethodRepository.getByCreditCardNumberAndExpirationDate(paymentRequest.getCardNumber(),
                paymentRequest.getExpirationDate()).isEmpty()) {

            PaymentMethod paymentMethod = initiate(paymentRequest, account);
            paymentMethodRepository.save(paymentMethod);
            log.info("New payment method added for account id [%s]".formatted(account.getId()) );
        }

    }

    private PaymentMethod initiate(PaymentRequest paymentRequest, Account account) {

        return PaymentMethod.builder()
                .creditCardNumber(paymentRequest.getCardNumber())
                .cardHolderName(paymentRequest.getCardholderName())
                .expirationDate(paymentRequest.getExpirationDate())
                .CVV(paymentRequest.getCVV())
                .account(account)
                .build();
    }
}
