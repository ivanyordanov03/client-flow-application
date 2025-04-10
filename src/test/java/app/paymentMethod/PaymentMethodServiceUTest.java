package app.paymentMethod;

import app.paymentMethod.model.PaymentMethod;
import app.paymentMethod.repository.PaymentMethodRepository;
import app.paymentMethod.service.PaymentMethodService;
import app.web.dto.PaymentMethodRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentMethodServiceUTest {

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @InjectMocks
    private PaymentMethodService paymentMethodService;

    @Test
    void givenPaymentMethodRequest_whenCreateNewAndGetPaymentMethodExists_throwsIllegalArgumentException() {

        PaymentMethod method = PaymentMethod.builder()
                .id(UUID.randomUUID())
                .creditCardNumber(UUID.randomUUID().toString())
                .expirationDate("02/28")
                .build();
        PaymentMethodRequest paymentMethodRequest = PaymentMethodRequest.builder()
                .cardNumber(method.getCreditCardNumber())
                .expirationDate(method.getExpirationDate())
                .build();
        when(paymentMethodRepository.getByCreditCardNumberAndExpirationDate(paymentMethodRequest.getCardNumber(), paymentMethodRequest.getExpirationDate()))
                .thenReturn(Optional.of(method));

        assertThrows(IllegalArgumentException.class, () -> paymentMethodService.createNew(paymentMethodRequest, UUID.randomUUID()));
    }

    @Test
    void givenPaymentMethod_whenSetAsDefaultMethodAndMoreThanOneAlreadyExist_thenLogErrorAndSetAsDefaultMethod() {

        PaymentMethod method = PaymentMethod.builder()
                .id(UUID.randomUUID())
                .accountId(UUID.randomUUID())
                .defaultMethod(false)
                .build();
        PaymentMethod defaultMethod1 = PaymentMethod.builder()
                .id(UUID.randomUUID())
                .defaultMethod(true)
                .build();
        PaymentMethod defaultMethod2 = PaymentMethod.builder()
                .id(UUID.randomUUID())
                .defaultMethod(true)
                .build();

        when(paymentMethodRepository.findByAccountIdAndDefaultMethodIsTrue(method.getAccountId())).thenReturn(List.of(defaultMethod1, defaultMethod2));

        paymentMethodService.setAsDefaultMethod(method);

        assertTrue(method.isDefaultMethod());
        assertFalse(defaultMethod1.isDefaultMethod());
        assertFalse(defaultMethod2.isDefaultMethod());
    }

    @Test
    void givenDefaultPaymentMethod_whenDeleteAndThereAreOtherPaymentMethods_thenSetOtherDefaultMethodAndDeleteGiven() {

        PaymentMethod defaultMethodToDelete = PaymentMethod.builder()
                .id(UUID.randomUUID())
                .accountId(UUID.randomUUID())
                .defaultMethod(true)
                .build();
        PaymentMethod newDefaultMethod = PaymentMethod.builder()
                .id(UUID.randomUUID())
                .defaultMethod(false)
                .build();

        when(paymentMethodRepository.findById(any())).thenReturn(Optional.of(defaultMethodToDelete));
        when(paymentMethodRepository.findAllByAccountId(any())).thenReturn(List.of(defaultMethodToDelete, newDefaultMethod));

        paymentMethodService.delete(defaultMethodToDelete.getId(), UUID.randomUUID(), defaultMethodToDelete.getAccountId());

        assertTrue(newDefaultMethod.isDefaultMethod());
    }

    @Test
    void givenNoExistingPaymentMethods_whenCreateNew_thenPaymentMethodIsSetAsDefault() {

        UUID accountId = UUID.randomUUID();
        PaymentMethodRequest request = PaymentMethodRequest.builder()
                .cardNumber(UUID.randomUUID().toString())
                .expirationDate("12/25")
                .defaultMethod(false)
                .build();

        when(paymentMethodRepository.findAllByAccountId(accountId))
                .thenReturn(List.of());
        when(paymentMethodRepository.getByCreditCardNumberAndExpirationDate(any(), any()))
                .thenReturn(Optional.empty());
        when(paymentMethodRepository.save(any(PaymentMethod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        paymentMethodService.createNew(request, accountId);

        ArgumentCaptor<PaymentMethod> paymentMethodCaptor = ArgumentCaptor.forClass(PaymentMethod.class);
        verify(paymentMethodRepository).save(paymentMethodCaptor.capture());

        PaymentMethod capturedPaymentMethod = paymentMethodCaptor.getValue();
        assertTrue(capturedPaymentMethod.isDefaultMethod());
    }
}
