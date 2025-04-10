package app.account;

import app.TestBuilder;
import app.account.model.Account;
import app.account.repository.AccountRepository;
import app.account.service.AccountService;
import app.paymentMethod.model.PaymentMethod;
import app.paymentMethod.service.PaymentMethodService;
import app.plan.model.Plan;
import app.plan.model.PlanName;
import app.plan.service.PlanService;
import app.user.model.User;
import app.web.dto.AccountRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceUTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PlanService planService;
    @Mock
    private PaymentMethodService paymentMethodService;

    @InjectMocks
    private AccountService accountService;

    @Test
    void givenAccount_whenDeactivated_thenSetActiveIsFalse() {

        UUID id = UUID.randomUUID();
        Account account = Account.builder().id(id).build();
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        Account deactivated = accountService.deactivate(id);
        assertFalse(deactivated.isActive());
    }

    @Test
    void shouldReturnAllExpiredAccounts_happyPath() {

        assertNotNull(accountService.getAllExpiredAccounts());
    }

    @Test
    void shouldReturnAllForAutoRenewalAccounts_happyPath() {

        assertNotNull(accountService.getAllAccountsForAutoRenewal());
    }

    @Test
    void givenInvalidAccountId_shouldThrowIllegalArgumentException() {

        UUID uuid = UUID.randomUUID();
        when(accountRepository.findById(uuid)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> accountService.getById(uuid));

        assertEquals("Account with id [%s] does not exist.".formatted(uuid), exception.getMessage());
    }

    @Test
    void givenInvalidOwnerId_shouldThrowIllegalArgumentException() {

        UUID uuid = UUID.randomUUID();
        when(accountRepository.findByOwnerId(uuid)).thenReturn(Optional.empty());


        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> accountService.getByOwnerId(uuid));

        assertEquals("There is no account associated with user with id [%s].".formatted(uuid), exception.getMessage());
    }

    @Test
    void givenAccount_whenEdit_HappyPath() {

        UUID id = UUID.randomUUID();
        User user = TestBuilder.aRandomPrimaryAdmin();
        Account account = Account.builder()
                .id(id)
                .ownerId(user.getId())
                .build();

        when(accountRepository.findByOwnerId(user.getId())).thenReturn(Optional.of(account));

        AccountRequest accountRequest = AccountRequest.builder()
                .logoURL("Logo")
                .businessName("Edited")
                .address("")
                .phoneNumber("2023034455")
                .build();

        accountService.edit(accountRequest, account.getOwnerId());

        assertEquals("Logo", account.getCompanyLogo());
        assertEquals("Edited", account.getBusinessName());
        assertEquals("", account.getAddress());
        assertEquals("2023034455", account.getPhoneNumber());
    }

    @Test
    void givenAccountWithAutoRenewalTrue_whenDeleteNotTheOnlyPaymentMethod_AutoRenewalStaysTrue() {

        UUID id = UUID.randomUUID();
        User user = TestBuilder.aRandomPrimaryAdmin();
        Account account = Account.builder()
                .id(id)
                .ownerId(user.getId())
                .autoRenewalEnabled(true)
                .build();
        id = UUID.randomUUID();
        PaymentMethod method = PaymentMethod.builder()
                .id(id)
                .accountId(account.getId())
                .build();

        when(paymentMethodService.getAllByAccountId(account.getId())).thenReturn(List.of(method));

        accountService.deleteAccountPaymentMethod(method.getId(), user.getId(), account.getId());

        assertTrue(account.isAutoRenewalEnabled());
    }

    @Test
    void givenAccountWithAutoRenewalFalse_whenDeleteAccountPaymentMethod_AutoRenewalStaysFalse() {

        UUID id = UUID.randomUUID();
        User user = TestBuilder.aRandomPrimaryAdmin();
        Account account = Account.builder()
                .id(id)
                .ownerId(user.getId())
                .autoRenewalEnabled(false)
                .build();
        id = UUID.randomUUID();
        PaymentMethod method = PaymentMethod.builder()
                .id(id)
                .accountId(account.getId())
                .build();

        when(paymentMethodService.getAllByAccountId(account.getId())).thenReturn(List.of(method));

        accountService.deleteAccountPaymentMethod(method.getId(), user.getId(), account.getId());

        assertFalse(account.isAutoRenewalEnabled());
    }

    @Test
    void givenAccountWithAutoRenewalTrue_whenDeleteTheOnlyPaymentMethod_AutoRenewalTurnsFalse() {

        UUID id = UUID.randomUUID();
        User user = TestBuilder.aRandomPrimaryAdmin();
        Account account = Account.builder()
                .id(id)
                .ownerId(user.getId())
                .autoRenewalEnabled(true)
                .build();
        id = UUID.randomUUID();
        PaymentMethod method = PaymentMethod.builder()
                .id(id)
                .accountId(account.getId())
                .build();

        when(paymentMethodService.getAllByAccountId(account.getId())).thenReturn(Collections.emptyList());
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        accountService.deleteAccountPaymentMethod(method.getId(), user.getId(), account.getId());

        assertFalse(account.isAutoRenewalEnabled());
    }

    @Test
    void givenAccountWithAutoRenewalTrue_whenSwitchAutoRenewalEnabled_thenAutoRenewalTurnsFalse() {

        UUID id = UUID.randomUUID();
        User user = TestBuilder.aRandomPrimaryAdmin();
        Account account = Account.builder()
                .id(id)
                .ownerId(user.getId())
                .autoRenewalEnabled(true)
                .build();

        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        accountService.switchAutoRenewal(account.getId());

        assertFalse(account.isAutoRenewalEnabled());
    }

    @Test
    void givenAccountWithAutoRenewalFalseAndNoSavedPaymentMethod_whenSwitchAutoRenewalEnabled_thenThrowsIllegalStateException() {

        UUID id = UUID.randomUUID();
        User user = TestBuilder.aRandomPrimaryAdmin();
        Account account = Account.builder()
                .id(id)
                .ownerId(user.getId())
                .autoRenewalEnabled(false)
                .build();

        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(paymentMethodService.getAllByAccountId(account.getId())).thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> accountService.switchAutoRenewal(account.getId()));
        assertFalse(account.isAutoRenewalEnabled());
    }

    @Test
    void givenActiveAccountWithPlanSimpleStart_whenPaidToUpgradeToEssential_thenPlanIsUpgradedToEssentialAndNewExpirationDate() {

        UUID id = UUID.randomUUID();
        Plan accountPlan = Plan.builder()
                .id(id)
                .planName(PlanName.SIMPLE_START)
                .build();

        id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Account account = Account.builder()
                .id(id)
                .plan(accountPlan)
                .active(true)
                .dateExpiring(now)
                .build();

        id = UUID.randomUUID();
        Plan newPlan = Plan.builder()
                .id(id)
                .planName(PlanName.ESSENTIALS)
                .build();

        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(planService.getByName(any())).thenReturn(newPlan);

        accountService.setAccountAfterSubscriptionPayment(account.getId(), newPlan.getPlanName().toString());

        assertEquals(account.getDateExpiring(), now.plusMonths(1));
        assertEquals(PlanName.ESSENTIALS, account.getPlan().getPlanName());
    }
}
