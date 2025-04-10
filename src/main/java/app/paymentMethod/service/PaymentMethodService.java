package app.paymentMethod.service;

import app.paymentMethod.model.PaymentMethod;
import app.paymentMethod.repository.PaymentMethodRepository;
import app.web.dto.PaymentMethodRequest;
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
    public UUID createNew(PaymentMethodRequest paymentMethodRequest, UUID accountId) {

        if (paymentMethodRepository.getByCreditCardNumberAndExpirationDate(paymentMethodRequest.getCardNumber(),
                paymentMethodRequest.getExpirationDate()).isPresent()) {
            throw new IllegalArgumentException(PAYMENT_METHOD_WITH_CARD_NUMBER_EXISTS.formatted(paymentMethodRequest.getCardNumber()));
        }

        PaymentMethod paymentMethod = initiate(paymentMethodRequest, accountId);

        if (paymentMethodRepository.findAllByAccountId(accountId).isEmpty()) {
            paymentMethod.setDefaultMethod(true);
        }

        if (paymentMethodRequest.isDefaultMethod()) {
            setAsDefaultMethod(paymentMethod);
        }

        paymentMethodRepository.save(paymentMethod);
        log.info(NEW_PAYMENT_METHOD_WITH_ID_ADDED_BY_USER_ID.formatted(paymentMethod.getId(), accountId));
        return paymentMethod.getId();
    }

    private PaymentMethod initiate(PaymentMethodRequest paymentMethodRequest, UUID accountId) {

        return PaymentMethod.builder()
                .creditCardNumber(paymentMethodRequest.getCardNumber())
                .cardHolderName(paymentMethodRequest.getCardholderName())
                .expirationDate(paymentMethodRequest.getExpirationDate())
                .CVV(paymentMethodRequest.getCvv())
                .accountId(accountId)
                .build();
    }

    @Transactional
    public void setAsDefaultMethod(PaymentMethod paymentMethod) {

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
        log.info("Saved payment methods with id [%s] was set as default for account with id [%s]".formatted(paymentMethod.getId(), paymentMethod.getAccountId()));
    }

    public PaymentMethod edit(UUID id, PaymentMethodRequest paymentMethodRequest) {

        PaymentMethod paymentMethod = getById(id);
        paymentMethod.setCardHolderName(paymentMethodRequest.getCardholderName());
        paymentMethod.setCreditCardNumber(paymentMethodRequest.getCardNumber());
        paymentMethod.setExpirationDate(paymentMethodRequest.getExpirationDate());
        paymentMethod.setCVV(paymentMethodRequest.getCvv());
        paymentMethod.setDefaultMethod(paymentMethodRequest.isDefaultMethod());

        return paymentMethodRepository.save(paymentMethod);
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
            setAsDefaultMethod(newDefaultMethod);
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

    @Transactional
    public void prepareToSetAsDefaultMethod(UUID id) {
        setAsDefaultMethod(getById(id));
    }
}
