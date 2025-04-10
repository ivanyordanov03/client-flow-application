package app;

import app.paymentMethod.model.PaymentMethod;
import app.paymentMethod.repository.PaymentMethodRepository;
import app.paymentMethod.service.PaymentMethodService;
import app.user.model.User;
import app.web.dto.PaymentMethodRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class DeleteEditedDefaultPaymentMethodServiceITest {

    @Autowired
    private PaymentMethodRepository planRepository;
    @Autowired
    private PaymentMethodService paymentMethodService;

    @Test
    public void deleteFirstEditedDefaultPaymentMethodByAuthorizedUser_happyPath() {

        PaymentMethodRequest paymentMethodRequest = PaymentMethodRequest.builder()
                .cardNumber("3883996644771221")
                .cvv("101")
                .expirationDate("11/28")
                .cardholderName("Card Holder Name")
                .defaultMethod(true)
                .build();

        User primaryAdmin = TestBuilder.aRandomPrimaryAdmin();

        paymentMethodService.createNew(paymentMethodRequest, primaryAdmin.getId());
        List<PaymentMethod> paymentMethods = paymentMethodService.getAllByAccountId(primaryAdmin.getId());

        assertFalse(paymentMethods.isEmpty());

        PaymentMethod paymentMethod = paymentMethods.get(0);

        Assertions.assertTrue(paymentMethod.isDefaultMethod());

        paymentMethodRequest.setCvv("202");
        paymentMethod = paymentMethodService.edit(paymentMethod.getId(), paymentMethodRequest);

        assertEquals("202", paymentMethod.getCVV());

        paymentMethodService.delete(paymentMethod.getId(), primaryAdmin.getId(), primaryAdmin.getAccountId());

        PaymentMethod finalPaymentMethod = paymentMethod;
        assertThrows(IllegalArgumentException.class, () -> paymentMethodService.getById(finalPaymentMethod.getId()));
    }

}
