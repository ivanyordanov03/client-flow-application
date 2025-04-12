package app.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class CreditCardExpirationValidatorUTest {
    private final CreditCardExpirationValidator validator = new CreditCardExpirationValidator();
    @Mock
    private ConstraintValidatorContext context;

    @Test void validFutureDate_returnsTrue() {
        assertTrue(validator.isValid("12/30", context));
    }

    @Test void currentMonth_returnsTrue() {
        String currentMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("MM/yy"));
        assertTrue(validator.isValid(currentMonth, context));
    }

    @Test void invalidFormat_returnsFalse() {
        assertFalse(validator.isValid("12-30", context));
        assertFalse(validator.isValid("13/30", context));
        assertFalse(validator.isValid("00/30", context));
        assertFalse(validator.isValid("12/2030", context));
    }

    @Test void expiredDate_returnsFalse() {
        assertFalse(validator.isValid("01/20", context));
    }

    @Test void nullValue_returnsFalse() {
        assertFalse(validator.isValid(null, context));
    }

    @Test void malformedDate_returnsFalse() {
        assertFalse(validator.isValid("not/a/date", context));
    }

    @Test void validFormatButInvalidDate_returnsFalse() {
        assertFalse(validator.isValid("02/00", context));
        assertFalse(validator.isValid("00/01", context));
    }
}