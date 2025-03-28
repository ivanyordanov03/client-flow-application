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
    private static final String PAYMENT_METHOD_WITH_ID_DELETED_BY_USER_ID = "Payment method with id [%s] deleted by user with id [%s]";
    private static final String NEW_PAYMENT_METHOD_WITH_ID_ADDED_BY_USER_ID = "New payment method with id [%s] added to account with id [%s]";

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
            setAsDefaultMethod(paymentMethod.getId());
        }

        paymentMethodRepository.save(paymentMethod);
        log.info(NEW_PAYMENT_METHOD_WITH_ID_ADDED_BY_USER_ID.formatted(paymentMethod.getId(), accountId));

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
    public void setAsDefaultMethod(UUID id) {

        PaymentMethod paymentMethod = getById(id);
        List<PaymentMethod> defaultMethodsAsList = paymentMethodRepository.findByAccountIdAndDefaultMethodIsTrue(paymentMethod.getAccountId());

        if (defaultMethodsAsList.size() > 1) {
            log.error("More than one default payment methods found for account id [%s]".formatted(paymentMethod.getAccountId()));
        }

        if (!defaultMethodsAsList.isEmpty()) {
            defaultMethodsAsList.forEach(defaultMethod -> {defaultMethod.setDefaultMethod(false);
                                                        paymentMethodRepository.save(defaultMethod);});
        }

        paymentMethod.setDefaultMethod(true);
        paymentMethodRepository.save(paymentMethod);
        log.error("Saved payment methods with id [%s] was set as default for account with id [%s]".formatted(id, paymentMethod.getAccountId()));
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

    @Transactional
    public void delete(UUID id, UUID userId, UUID accountId) {
        PaymentMethod paymentMethod = getById(id);
        List<PaymentMethod> paymentMethods = getAllByAccountId(accountId);

        if (paymentMethod.isDefaultMethod() && paymentMethods.size() > 1) {
            PaymentMethod newDefaultMethod = paymentMethods
                    .stream()
                    .filter(m -> !m.getId().equals(id))
                    .findFirst()
                    .get();
            setAsDefaultMethod(newDefaultMethod.getId());
        }

        paymentMethodRepository.delete(paymentMethod);
        log.info(PAYMENT_METHOD_WITH_ID_DELETED_BY_USER_ID.formatted(id, userId));
    }

    public PaymentMethod getById(UUID id) {

        return paymentMethodRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Payment method with id [%s] does not exist".formatted(id)));
    }

    public List<PaymentMethod> getAllByAccountId(UUID accountId) {
        return paymentMethodRepository.findAllByAccountId(accountId);
    }
}
