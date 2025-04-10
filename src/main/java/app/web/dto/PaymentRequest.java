package app.web.dto;

import app.validation.ValidExpirationDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {

//    @CreditCardNumber - for real credit card validation, commented to make tests easier
    private String cardNumber;

    @Size(min = 5, max = 50, message = "The cardholder's name length must be between 5 and 50 letters.")
    private String cardholderName;

    @ValidExpirationDate(message = "Expiration date must be a future date in MM/YY format (e.g., 12/25).")
    private String expirationDate;

    @NotBlank(message = "CVV is required")
    private String cvv;

    private String planToPurchase;

    private String transactionType;

    private boolean savePaymentMethod;

    private boolean autoRenewal;
}
