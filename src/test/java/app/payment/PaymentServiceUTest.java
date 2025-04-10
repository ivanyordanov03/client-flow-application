package app.payment;

import app.account.service.AccountService;
import app.payment.model.Payment;
import app.payment.repository.PaymentRepository;
import app.payment.service.PaymentService;
import app.paymentMethod.service.PaymentMethodService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceUTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private PaymentMethodService paymentMethodService;
    @InjectMocks
    private PaymentService paymentService;

    @Test
    void givenListOfPaymentsThenReturnTheLastThree() {}

    @Test
    void givenAccountThenReturnAllAccountPayments() {

        UUID accountId = UUID.randomUUID();
        Payment payment1 = createPayment(accountId, LocalDateTime.now().minusDays(2));
        Payment payment2 = createPayment(accountId, LocalDateTime.now().minusDays(1));
        Payment payment3 = createPayment(accountId, LocalDateTime.now());

        List<Payment> payments = List.of(payment1, payment2, payment3);
        when(paymentRepository.findAllByAccountIdOrderByPaymentDateDesc(accountId)).thenReturn(payments
                .stream()
                .sorted(Comparator.comparing(Payment::getPaymentDate).reversed())
                .toList());

        List<Payment> orderedPayments = paymentService.getAllAccountPayments(accountId);

        assertEquals(3,orderedPayments.size());
        assertTrue(orderedPayments.get(0).getPaymentDate().isAfter(orderedPayments.get(1).getPaymentDate()));
        assertTrue(orderedPayments.get(1).getPaymentDate().isAfter(orderedPayments.get(2).getPaymentDate()));
        verify(paymentRepository).findAllByAccountIdOrderByPaymentDateDesc(accountId);
    }

    @Test
    void givenAccountWithMoreThanThreePayments_whenGetLastThree_thenReturnsThreeMostRecent() {

        UUID accountId = UUID.randomUUID();
        Payment payment1 = createPayment(accountId, LocalDateTime.now().minusDays(3));
        Payment payment2 = createPayment(accountId, LocalDateTime.now().minusDays(2));
        Payment payment3 = createPayment(accountId, LocalDateTime.now().minusDays(1));
        Payment payment4 = createPayment(accountId, LocalDateTime.now());

        List<Payment> payments = List.of(payment1, payment2, payment3, payment4);
        when(paymentRepository.findAllByAccountIdOrderByPaymentDateDesc(accountId))
                .thenReturn(payments);

        List<Payment> result = paymentService.getLastThree(accountId);

        assertEquals(3, result.size());
        assertTrue(payments.contains(payment4));
        assertTrue(payments.contains(payment3));
        assertTrue(payments.contains(payment2));
    }

    @Test
    void givenAccountWithLessThanThreePayments_whenGetLastThree_thenReturnsAllAvailable() {

        UUID accountId = UUID.randomUUID();

        Payment payment1 = createPayment(accountId, LocalDateTime.now().minusDays(1));
        Payment payment2 = createPayment(accountId, LocalDateTime.now());

        List<Payment> payments = List.of(payment1, payment2);
        when(paymentRepository.findAllByAccountIdOrderByPaymentDateDesc(accountId))
                .thenReturn(payments);

        List<Payment> result = paymentService.getLastThree(accountId);

        assertEquals(result, payments);
    }

    private Payment createPayment(UUID accountId,LocalDateTime date) {
        return Payment.builder()
                .id(UUID.randomUUID())
                .accountId(accountId)
                .paymentDate(date)
                .build();
    }

}
