package app.account.service;

import app.account.model.Account;
import app.account.repository.AccountRepository;
import app.paymentMethod.model.PaymentMethod;
import app.paymentMethod.service.PaymentMethodService;
import app.plan.model.Plan;
import app.plan.service.PlanService;
import app.web.dto.PaymentRequest;
import app.web.dto.RegisterUserRequest;
import app.web.mapper.Mapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AccountService {

    private static final String AUTO_RENEWAL_ENABLED_FOR_ACCOUNT_WITH_ID_S = "Auto-renewal enabled for account with id [%s]";
    private static final String ACCOUNT_WITH_ID_ACTIVATED = "Account with id [%s] has been activated.";

    private final AccountRepository accountRepository;
    private final PlanService planService;
    private final PaymentMethodService paymentMethodService;

    @Autowired
    public AccountService(AccountRepository accountRepository,
                          PlanService planService,
                          PaymentMethodService paymentMethodService) {

        this.accountRepository = accountRepository;
        this.planService = planService;
        this.paymentMethodService = paymentMethodService;
    }

    public Account createNew(RegisterUserRequest registerUserRequest) {

        Plan plan = planService.getByType(Mapper.getPlanTypeFromString(registerUserRequest.getPlanName()));

        Account account = initialize(plan);
        accountRepository.save(account);

        return account;
    }

    private Account initialize(Plan plan) {

        LocalDateTime now = LocalDateTime.now();

        return Account.builder()
                .plan(plan)
                .createdOn(now)
                .updatedOn(now)
                .build();
    }

    public Account getByOwnerId(UUID ownerId) {

        return accountRepository.findByOwnerId(ownerId);
    }

    public void allowAutoRenewal(PaymentRequest paymentRequest, Account account) {

        List<PaymentMethod> paymentMethods = paymentMethodService.getAllByAccountId(account.getId());
        if (paymentMethods.isEmpty()) {
            paymentMethodService.createNew(paymentRequest, account.getId());
        }

        account.setAutoRenewalEnabled(true);
        account.setUpdatedOn(LocalDateTime.now());
        accountRepository.save(account);
        log.info(AUTO_RENEWAL_ENABLED_FOR_ACCOUNT_WITH_ID_S.formatted(account.getId()));

    }

    public void setToActive(Account account) {

        account.setActive(true);
        account.setUpdatedOn(LocalDateTime.now());
        accountRepository.save(account);
        log.info(ACCOUNT_WITH_ID_ACTIVATED.formatted(account.getId()));
    }
}
