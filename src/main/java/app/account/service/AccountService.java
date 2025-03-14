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
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class AccountService {

    private static final String AUTO_RENEWAL_ENABLED_FOR_ACCOUNT_WITH_ID_S = "Auto-renewal enabled for account with id [%s]";
    private static final String ACCOUNT_WITH_ID_ACTIVATED = "Account with id [%s] has been activated.";
    private static final String ACCOUNT_WITH_ID_DOES_NOT_EXIST = "Account with id [%s] does not exist.";
    private static final String NO_ACCOUNT_ASSOCIATED_WITH_USER_WITH_ID = "There is no account associated with user with id [%s].";
    private static final String ACCOUNT_WITH_ID_NOT_FOUND = "Account with id [%s] was not found.";

    private final AccountRepository accountRepository;
    private final PlanService planService;
    private final PaymentMethodService paymentMethodService;

    @Autowired
    public AccountService(AccountRepository accountRepository,          // create account page visible only to Primary admin with upgrade button
                          PlanService planService,                      // check if owner can add team members account limits
                          PaymentMethodService paymentMethodService) {  // upgrade account page

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

    public Account getId(UUID accountId) {

        Optional<Account> optional = accountRepository.findById(accountId);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException(ACCOUNT_WITH_ID_DOES_NOT_EXIST.formatted(accountId));
        }

        return optional.get();
    }

    public Account getByOwnerId(UUID ownerId) {

        Optional<Account> optional = accountRepository.findByOwnerId(ownerId);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException(NO_ACCOUNT_ASSOCIATED_WITH_USER_WITH_ID.formatted(ownerId));
        }

        return optional.get();
    }

    public Account getById(UUID id) {

        Optional<Account> optional = accountRepository.findById(id);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException(ACCOUNT_WITH_ID_NOT_FOUND.formatted(id));
        }

        return optional.get();
    }
}
