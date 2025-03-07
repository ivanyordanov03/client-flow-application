package app.account.service;

import app.account.model.Account;
import app.account.repository.AccountRepository;
import app.paymentMethod.service.PaymentMethodService;
import app.plan.model.Plan;
import app.user.model.User;
import app.web.dto.PaymentRequest;
import app.web.dto.RegisterOwnerRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final PaymentMethodService paymentMethodService;

    @Autowired
    public AccountService(AccountRepository accountRepository,
                          PaymentMethodService paymentMethodService) {

        this.accountRepository = accountRepository;
        this.paymentMethodService = paymentMethodService;
    }

    public void createNew(RegisterOwnerRequest registerOwnerRequest, User owner, Plan plan) {

        Account account = initialize(registerOwnerRequest, owner, plan);
        accountRepository.save(account);
        log.info("Created new account with id [%s] for user with id [%s].".formatted(account.getId(), owner.getId()));
    }

    private Account initialize(RegisterOwnerRequest registerOwnerRequest, User owner, Plan plan) {

        LocalDateTime now = LocalDateTime.now();

        return Account.builder()
                .phoneNumber(registerOwnerRequest.getPhoneNumber())
                .plan(plan)
                .owner(owner)
                .createdOn(now)
                .updatedOn(now)
                .owner(owner)
                .build();
    }

    public Account getByOwner(User user) {

        return accountRepository.findByOwner(user);
    }

    public void allowAutoRenewal(PaymentRequest paymentRequest, Account account) {

        if (account.getPaymentMethods().isEmpty()) {
            paymentMethodService.createNew(paymentRequest, account);
        }

        account.setAutoRenewalEnabled(true); //add log what has been updated.
        account.setUpdatedOn(LocalDateTime.now());
        accountRepository.save(account);
        log.info("Auto-renewal enabled for account with id [%s]".formatted(account.getId()));
    }

    public void setToActive(Account account) {

        account.setActive(true);
        account.setUpdatedOn(LocalDateTime.now());
        accountRepository.save(account);
        log.info("Account with id [%s] has been activated.".formatted(account.getId()));
    }
}
