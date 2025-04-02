package app.scheduler;

import app.account.model.Account;
import app.account.service.AccountService;
import app.payment.service.PaymentService;
import app.paymentMethod.service.PaymentMethodService;
import app.web.mapper.Mapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SubscriptionScheduler {

    private static final String ACCOUNT_ID_HAS_EXPIRED = "Account with id [%s] has expired.";
    private static final String NO_EXPIRED_ACCOUNTS_FOUND = "No expired accounts were found.";
    private static final String NO_ACCOUNTS_FOUND_FOR_AUTO_RENEWAL = "No accounts found for autoRenewal";
    private static final String AUTO_RENEWAL_COMPLETE_ACCOUNT_ID_DEFAULT_METHOD_ID = "AutoRenewal for account with id [%s] and default payment method id [%s] completed";

    private final AccountService accountService;
    private final PaymentService paymentService;
    private final PaymentMethodService paymentMethodService;

    public SubscriptionScheduler(AccountService accountService,
                                PaymentService paymentService,
                                PaymentMethodService paymentMethodService) {

        this.accountService = accountService;
        this.paymentService = paymentService;
        this.paymentMethodService = paymentMethodService;
    }

    @Scheduled(cron = "0 0,30 * * * *")
    public void runAutoPay() {

        List<Account> allAccountsForAutoRenewal = accountService.getAllAccountsForAutoRenewal();

        if (allAccountsForAutoRenewal.isEmpty()) {
            log.info(NO_ACCOUNTS_FOUND_FOR_AUTO_RENEWAL);
            return;
        }
        allAccountsForAutoRenewal.forEach(account -> {
            paymentService.insert(Mapper.mapPaymentMethodToPaymentRequest(paymentMethodService.getById(account.getDefaultPaymentMethodId())), account.getId());
            log.info(AUTO_RENEWAL_COMPLETE_ACCOUNT_ID_DEFAULT_METHOD_ID.formatted(account.getId(), account.getDefaultPaymentMethodId()));                                                    });
    }

    @Scheduled(cron = "0 0 * * * *")
    public void deactivateExpiredAccounts() {

        List<Account> allExpiredAccounts = accountService.getAllExpiredAccounts();

        if (allExpiredAccounts.isEmpty()) {
            log.info(NO_EXPIRED_ACCOUNTS_FOUND);
            return;
        }

        allExpiredAccounts.forEach(account -> {
            log.info(ACCOUNT_ID_HAS_EXPIRED.formatted(account.getId()));
            accountService.deactivate(account.getId());
        });
    }
}
