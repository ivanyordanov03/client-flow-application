package app.paymentMethod.service;

import app.paymentMethod.model.PaymentMethod;
import app.paymentMethod.repository.PaymentMethodRepository;
import app.web.dto.PaymentSettingsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PaymentMethodService {

    private static final String PAYMENT_METHOD_WITH_CARD_NUMBER_EXISTS = "Payment method with card number [%s] already exists";

    private final PaymentMethodRepository paymentMethodRepository;

    @Autowired
    public PaymentMethodService(PaymentMethodRepository paymentMethodRepository) {

        this.paymentMethodRepository = paymentMethodRepository;
    }

    @Transactional
    public void createNew(PaymentSettingsRequest paymentSettingsRequest, UUID accountId) {

        if (paymentMethodRepository.getByCreditCardNumberAndExpirationDate(paymentSettingsRequest.getCardNumber(),
                paymentSettingsRequest.getExpirationDate()).isPresent()) {
            throw new IllegalArgumentException(PAYMENT_METHOD_WITH_CARD_NUMBER_EXISTS.formatted(paymentSettingsRequest.getCardNumber()));
        }

        PaymentMethod paymentMethod = initiate(paymentSettingsRequest, accountId);

        if (paymentMethodRepository.findAllByAccountId(accountId).isEmpty()) {
            paymentMethod.setDefaultMethod(true);
        }

        if (paymentSettingsRequest.isDefaultMethod()) {
            setAsDefaultMethod(paymentMethod);
        }

        paymentMethodRepository.save(paymentMethod);
        log.info("New payment method added for account id [%s]".formatted(accountId) );

    }

    private PaymentMethod initiate(PaymentSettingsRequest paymentSettingsRequest, UUID accountId) {

        return PaymentMethod.builder()
                .creditCardNumber(paymentSettingsRequest.getCardNumber())
                .cardHolderName(paymentSettingsRequest.getCardholderName())
                .expirationDate(paymentSettingsRequest.getExpirationDate())
                .CVV(paymentSettingsRequest.getCvv())
                .accountId(accountId)
                .build();
    }

    @Transactional
    public void setAsDefaultMethod(PaymentMethod paymentMethod) {

        List<PaymentMethod> defaultMethodAsList = paymentMethodRepository.findByAccountIdAndDefaultMethodIsTrue(paymentMethod.getAccountId());

        if (defaultMethodAsList.isEmpty()) {
            log.error("Default payment method does not exist for account id [%s]".formatted(paymentMethod.getAccountId()));
        } else if (defaultMethodAsList.size() > 1) {
            log.error("More than one default payment methods found for account id [%s]".formatted(paymentMethod.getAccountId()));
            defaultMethodAsList.forEach(defaultMethod -> {defaultMethod.setDefaultMethod(false);
                paymentMethodRepository.save(defaultMethod);});
        }
        PaymentMethod currentDefaultMethod = defaultMethodAsList.get(0);
        currentDefaultMethod.setDefaultMethod(false);
        paymentMethodRepository.save(currentDefaultMethod);
        paymentMethod.setDefaultMethod(true);
        paymentMethodRepository.save(paymentMethod);
    }

    public PaymentMethod getById(UUID id) {

        return paymentMethodRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Payment method with id [%s] does not exist".formatted(id)));
    }

    public List<PaymentMethod> getAllByAccountId(UUID accountId) {
        return paymentMethodRepository.findAllByAccountId(accountId);
    }

    public List<PaymentMethod> getAll() {

        return paymentMethodRepository.findAll();
    }

    public void edit(UUID id, PaymentSettingsRequest paymentSettingsRequest) {

        PaymentMethod paymentMethod = getById(id);
        paymentMethod.setCardHolderName(paymentSettingsRequest.getCardholderName());
        paymentMethod.setCreditCardNumber(paymentSettingsRequest.getCardNumber());
        paymentMethod.setExpirationDate(paymentSettingsRequest.getExpirationDate());
        paymentMethod.setCVV(paymentSettingsRequest.getCvv());
        paymentMethod.setDefaultMethod(paymentSettingsRequest.isDefaultMethod());

        paymentMethodRepository.save(paymentMethod);
    }
}
