package app.paymentMethod.service;

import app.paymentMethod.model.PaymentMethod;
import app.paymentMethod.repository.PaymentMethodRepository;
import app.web.dto.PaymentSettingsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PaymentMethodService {

    private static final String PAYMENT_METHOD_WITH_CARD_NUMBER_EXISTS = "Payment method with card number [%s] already exists";

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMethodDefaultService paymentMethodDefaultService;

    @Autowired
    public PaymentMethodService(PaymentMethodRepository paymentMethodRepository,
                                PaymentMethodDefaultService paymentMethodDefaultService) {

        this.paymentMethodRepository = paymentMethodRepository;
        this.paymentMethodDefaultService = paymentMethodDefaultService;
    }

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
            paymentMethodDefaultService.setAsDefaultMethod(paymentMethod);
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
