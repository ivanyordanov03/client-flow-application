package app.scheduler;

import app.account.model.Account;
import app.account.service.AccountService;
import app.payment.service.PaymentService;
import app.paymentMethod.model.PaymentMethod;
import app.paymentMethod.service.PaymentMethodService;
import app.web.dto.PaymentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionSchedulerUTest {

    @Mock
    private AccountService accountService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private PaymentMethodService paymentMethodService;
    @InjectMocks
    private SubscriptionScheduler subscriptionScheduler;

    private final UUID accountId = UUID.randomUUID();
    private final UUID paymentMethodId = UUID.randomUUID();


    @Test
    void runAutoPay_whenNoAccountsForAutoRenewal_logsMessage() {

        when(accountService.getAllAccountsForAutoRenewal()).thenReturn(List.of());

        subscriptionScheduler.runAutoPay();

        verify(accountService, times(1)).getAllAccountsForAutoRenewal();
        verify(paymentService, never()).insert(any(), any());
        verify(paymentMethodService, never()).getById(any());
    }

    @Test
    void runAutoPay_whenAccountsExist_processesEachAccount() {

        Account account = Account.builder()
                .id(accountId)
                .defaultPaymentMethodId(paymentMethodId)
                .build();

        PaymentMethod paymentMethod = PaymentMethod.builder().build();

        when(accountService.getAllAccountsForAutoRenewal()).thenReturn(List.of(account));
        when(paymentMethodService.getById(paymentMethodId)).thenReturn(paymentMethod);

        subscriptionScheduler.runAutoPay();

        verify(paymentService, times(1)).insert(any(PaymentRequest.class), eq(accountId));
    }

    @Test
    void deactivateExpiredAccounts_whenNoExpiredAccounts_logsMessage() {

        when(accountService.getAllExpiredAccounts()).thenReturn(List.of());

        subscriptionScheduler.deactivateExpiredAccounts();

        verify(accountService, times(1)).getAllExpiredAccounts();
        verify(accountService, never()).deactivate(any());
    }

    @Test
    void deactivateExpiredAccounts_whenExpiredAccountsExist_deactivatesEach() {

        Account expiredAccount = Account.builder()
                .id(accountId)
                .build();

        when(accountService.getAllExpiredAccounts()).thenReturn(List.of(expiredAccount));

        subscriptionScheduler.deactivateExpiredAccounts();

        verify(accountService, times(1)).getAllExpiredAccounts();
        verify(accountService, times(1)).deactivate(accountId);
    }
}
