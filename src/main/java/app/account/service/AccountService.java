package app.account.service;

import app.account.model.Account;
import app.account.repository.AccountRepository;
import app.paymentMethod.model.PaymentMethod;
import app.paymentMethod.service.PaymentMethodService;
import app.plan.model.Plan;
import app.plan.service.PlanService;
import app.web.dto.AccountRequest;
import app.web.dto.PaymentRequest;
import app.web.dto.UserRequest;
import app.web.mapper.Mapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class AccountService {

    private static final String ACCOUNT_WITH_ID_DEACTIVATED = "Account with id [%s] was deactivated.";
    private static final String ACCOUNT_WITH_ID_ACTIVATED = "Account with id [%s] has been activated.";
    private static final String ACCOUNT_WITH_ID_DOES_NOT_EXIST = "Account with id [%s] does not exist.";
    private static final String ACCOUNT_WITH_ID_IS_SET_TO_EXPIRE_ON = "Account with id [%s] is set to expire on [%s].";
    private static final String AUTO_RENEWAL_ENABLED_FOR_ACCOUNT_WITH_ID = "Auto-renewal is enabled for account with id [%s]";
    private static final String AUTO_RENEWAL_DISABLED_FOR_ACCOUNT_WITH_ID = "Auto-renewal is disabled for account with id [%s]";
    private static final String ACCOUNT_WITH_ID_HAS_BEEN_EDITED_BY = "Account with id [%s] has been edited by user with id [%s].";
    private static final String NO_ACCOUNT_ASSOCIATED_WITH_USER_WITH_ID = "There is no account associated with user with id [%s].";
    private static final String NEW_OWNER_ID_ASSIGNED_TO_ACCOUNT_WITH_ID = "New owner with id [%s] has been assigned to account with id [%s].";
    private static final String NO_SAVED_PAYMENT_METHOD_FOR_AUTO_PAY = "There is no saved payment method associated with your account. Add one and try again.";

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

    public Account createNew(UserRequest registerUserRequest) {

        Plan plan = planService.getByName(Mapper.mapPlanNameAsStringToPlanTypeEnum(registerUserRequest.getPlanName()));

        Account account = initialize(plan);
        accountRepository.save(account);
        return account;
    }

    private Account initialize(Plan plan) {

        LocalDateTime now = LocalDateTime.now();

        return Account.builder()
                .plan(plan)
                .dateCreated(now)
                .dateUpdated(now)
                .build();
    }

    @Transactional
    public void enableAutoRenewalForNewSubscription(PaymentRequest paymentRequest, UUID id) {

        List<PaymentMethod> paymentMethods = paymentMethodService.getAllByAccountId(id);
        Account account = getById(id);
        if (paymentMethods.isEmpty()) {
            account.setDefaultPaymentMethodId(paymentMethodService.createNew(Mapper.mapPaymentRequestToPaymentMethodRequest(paymentRequest), id));
        }

        account.setAutoRenewalEnabled(true);
        account.setDateUpdated(LocalDateTime.now());
        accountRepository.save(account);

        log.info(AUTO_RENEWAL_ENABLED_FOR_ACCOUNT_WITH_ID.formatted(account.getId()));
    }

    public void setToActive(UUID id) {

        Account account = getById(id);
        account.setActive(true);
        account.setDateUpdated(LocalDateTime.now());
        accountRepository.save(account);
        log.info(ACCOUNT_WITH_ID_ACTIVATED.formatted(id));
    }

    public void setAccountAfterSubscriptionPayment(UUID id, String planName) {

        Account account = getById(id);

        if (account.isActive()) {
            account.setDateExpiring(account.getDateExpiring().plusMonths(1));
        } else {
            account.setDateExpiring(LocalDateTime.now().plusMonths(1));
        }

        if (!account.getPlan().getPlanName().toString().equals(planName)) {
            Plan plan = planService.getByName(Mapper.mapPlanNameAsStringToPlanTypeEnum(planName));
            account.setPlan(plan);
        }
        account.setDateUpdated(LocalDateTime.now());
        accountRepository.save(account);

        log.info(ACCOUNT_WITH_ID_IS_SET_TO_EXPIRE_ON.formatted(id, account.getDateExpiring()));
    }

    public void edit(AccountRequest accountRequest, UUID userId) {

        Account account = getByOwnerId(userId);
        account.setCompanyLogo(accountRequest.getLogoURL());
        account.setBusinessName(accountRequest.getBusinessName());
        account.setAddress(accountRequest.getAddress());
        account.setPhoneNumber(accountRequest.getPhoneNumber());
        account.setDateUpdated(LocalDateTime.now());

        accountRepository.save(account);
        log.info(ACCOUNT_WITH_ID_HAS_BEEN_EDITED_BY.formatted(account.getId(), userId));
    }

    @Transactional
    public void switchAutoRenewal(UUID id) {

        Account account = getById(id);

        if (!account.isAutoRenewalEnabled()) {
            List<PaymentMethod> savedPaymentMethods = paymentMethodService.getAllByAccountId(id);

            if (savedPaymentMethods.isEmpty()) {
                throw new IllegalStateException(NO_SAVED_PAYMENT_METHOD_FOR_AUTO_PAY);
            }

            if (savedPaymentMethods.stream().filter(PaymentMethod::isDefaultMethod).toList().isEmpty()) {
                paymentMethodService.setAsDefaultMethod(savedPaymentMethods.get(0));
            }
        }
        account.setAutoRenewalEnabled(!account.isAutoRenewalEnabled());
        account.setDateUpdated(LocalDateTime.now());
        accountRepository.save(account);

        if (account.isAutoRenewalEnabled()) {
            log.info(AUTO_RENEWAL_ENABLED_FOR_ACCOUNT_WITH_ID.formatted(id));
        } else {
            log.info(AUTO_RENEWAL_DISABLED_FOR_ACCOUNT_WITH_ID.formatted(id));
        }
    }

    public void insertOwnerId(UUID id, UUID ownerId) {

        Account account = getById(id);
        account.setOwnerId(ownerId);
        account.setDateUpdated(LocalDateTime.now());
        accountRepository.save(account);

        log.info(NEW_OWNER_ID_ASSIGNED_TO_ACCOUNT_WITH_ID.formatted(ownerId, id));
    }

    @Transactional
    public void deleteAccountPaymentMethod(UUID paymentMethodId, UUID userId, UUID id) {

        paymentMethodService.delete(paymentMethodId, userId, id);
        List<PaymentMethod> remainingMethods = paymentMethodService.getAllByAccountId(id);

        if (remainingMethods.isEmpty() && getById(id).isAutoRenewalEnabled()) {
            switchAutoRenewal(id);
        }
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
            throw new IllegalArgumentException(ACCOUNT_WITH_ID_DOES_NOT_EXIST.formatted(id));
        }

        return optional.get();
    }

    public List<Account> getAllAccountsForAutoRenewal() {

        return accountRepository.findAllByDateExpiringIsLessThanEqualAndAutoRenewalEnabledIsTrue(LocalDateTime.now().plusMinutes(30));
    }

    public List<Account> getAllExpiredAccounts() {

        return accountRepository.findAllByDateExpiringIsLessThanEqualAndActiveIsFalse(LocalDateTime.now());
    }

    public Account deactivate(UUID id) {

        Account account = getById(id);

        account.setActive(false);
        accountRepository.save(account);
        log.info(ACCOUNT_WITH_ID_DEACTIVATED.formatted(id));

        return account;
    }
}
