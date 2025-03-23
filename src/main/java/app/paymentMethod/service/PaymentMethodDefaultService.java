package app.paymentMethod.service;

import app.paymentMethod.model.PaymentMethod;
import app.paymentMethod.repository.PaymentMethodRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class PaymentMethodDefaultService {

    private final PaymentMethodRepository paymentMethodRepository;

    @Autowired
    public PaymentMethodDefaultService(PaymentMethodRepository paymentMethodRepository) {

        this.paymentMethodRepository = paymentMethodRepository;
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
}
