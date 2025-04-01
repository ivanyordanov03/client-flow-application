package app.web.dto;

import app.validation.ValidExpirationDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentMethodRequest {

    //    @CreditCardNumber - for real credit card validation, commented to make tests easier, using @Pattern instead
    @NotBlank
    @Pattern(regexp = "^\\d{15,16}$", message = "The card number must be 15 or 16 digits")
    private String cardNumber;

    @Size(min = 5, max = 50, message = "Cardholder's name length must be between 5 and 50 letters.")
    private String cardholderName;

    @ValidExpirationDate(message = "Expiration date must be a future date in MM/YY format (e.g., 12/25).")
    private String expirationDate;

    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "^\\d{3,4}$", message = "CVV code must be 3 or 4 digits")
    private String cvv;

    private boolean defaultMethod;
}
