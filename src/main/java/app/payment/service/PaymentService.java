package app.payment.service;

import app.account.model.Account;
import app.account.service.AccountService;
import app.payment.model.Payment;
import app.payment.repository.PaymentRepository;
import app.paymentMethod.service.PaymentMethodService;
import app.web.dto.PaymentRequest;
import app.web.mapper.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentService {

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
    public void insert(PaymentRequest paymentRequest, Account account) {

        if (paymentRequest.isSavePaymentMethod()) {
            paymentMethodService.createNew(Mapper.mapPaymentRequestToPaymentMethodRequest(paymentRequest), account.getId());
        }

        if (paymentRequest.isAutoRenewal()) {
            accountService.allowAutoRenewal(paymentRequest, account);
        }

        if (!account.isActive()) {
            accountService.setToActive(account);
        }

        paymentRepository.save(initiate(paymentRequest, account));
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