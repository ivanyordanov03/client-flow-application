package app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class CreditCardExpirationValidator implements ConstraintValidator<ValidExpirationDate, String> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/yy");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || !value.matches("^((0[1-9])|(1[0-2]))/[0-9]{2}$")) {
            return false;
        }

        try {
            YearMonth expiration = YearMonth.parse(value, FORMATTER);
            return expiration.plusMonths(1).isAfter(YearMonth.now());
        } catch (Exception e) {
            return false;
        }
    }
}