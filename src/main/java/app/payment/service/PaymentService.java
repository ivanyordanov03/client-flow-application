package app.payment.service;

import app.account.model.Account;
import app.account.service.AccountService;
import app.payment.model.Payment;
import app.payment.repository.PaymentRepository;
import app.paymentMethod.service.PaymentMethodService;
import app.web.dto.PaymentRequest;
import app.web.mapper.Mapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class PaymentService {

    private static final String NEW_PAYMENT_WITH_ID_HAS_BEEN_MADE_FOR_ACCOUNT_WITH_ID = "New payment with id [%s] made for account with id [%s]";

    private final PaymentRepository paymentRepository;
    private final AccountService accountService;
    private final PaymentMethodService paymentMethodService;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository,
                          AccountService accountService,
                          PaymentMethodService paymentMethodService) {

        this.paymentRepository = paymentRepository;
        this.accountService = accountService;
        this.paymentMethodService = paymentMethodService;
    }

    @Transactional
    public void insert(PaymentRequest paymentRequest, UUID accountId) {

        Account account = accountService.getById(accountId);

        if (paymentRequest.isSavePaymentMethod()) {
            paymentMethodService.createNew(Mapper.mapPaymentRequestToPaymentMethodRequest(paymentRequest), accountId);
        }

        if (paymentRequest.isAutoRenewal()) {
            accountService.enableAutoRenewalForNewSubscription(paymentRequest, accountId);
        }

        if (!account.isActive()) {
            accountService.setToActive(accountId);
        }

        accountService.setExpirationDate(accountId);
        Payment payment = initiate(paymentRequest, account);
        paymentRepository.save(payment);

        log.info(NEW_PAYMENT_WITH_ID_HAS_BEEN_MADE_FOR_ACCOUNT_WITH_ID.formatted(payment.getId(), accountId));
    }

    private Payment initiate(PaymentRequest paymentRequest, Account account) {

        String last4 = getLast4(paymentRequest);

        return Payment.builder()
                .amount(account.getPlan().getPricePerMonth().toString())
                .accountId(account.getId())
                .last4Digits(last4)
                .paymentDate(LocalDateTime.now())
                .build();
    }

    private String getLast4(PaymentRequest paymentRequest) {

        String cardNumber = paymentRequest.getCardNumber();
        return cardNumber.substring(cardNumber.length() - 4);
    }
}