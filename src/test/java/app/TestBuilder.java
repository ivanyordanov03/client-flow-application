package app;

import app.account.model.Account;
import app.contact.model.Contact;
import app.paymentMethod.model.PaymentMethod;
import app.plan.model.Plan;
import app.plan.model.PlanName;
import app.security.AuthenticationMetadata;
import app.task.model.Task;
import app.task.model.TaskPriority;
import app.user.model.User;
import app.user.model.UserRole;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@UtilityClass
public class TestBuilder {

    public static User aRandomUser() {

        return User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivo")
                .lastName("Kolev")
                .userRole(UserRole.USER)
                .email("ivo@gmail.com")
                .password("password")
                .accountId(aAccount().getId())
                .dateCreated(LocalDateTime.now())
                .dateUpdated(LocalDateTime.now())
                .build();
    }

    public static User aRandomAdmin() {

        return User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Ivanov")
                .userRole(UserRole.ADMINISTRATOR)
                .email("ivan@gmail.com")
                .password("password")
                .accountId(aAccount().getId())
                .dateCreated(LocalDateTime.now())
                .dateUpdated(LocalDateTime.now())
                .build();
    }

    public static User aRandomPrimaryAdmin() {

        return User.builder()
                .id(UUID.randomUUID())
                .firstName("Boyan")
                .lastName("Kirov")
                .userRole(UserRole.PRIMARY_ADMIN)
                .email("boyan@gmail.com")
                .password("password")
                .accountId(aAccount().getId())
                .dateCreated(LocalDateTime.now())
                .dateUpdated(LocalDateTime.now())
                .build();
    }

    public static Task aTask() {
        return Task.builder()
                .id(UUID.randomUUID())
                .name("Test Task")
                .description("Test description")
                .assignedToId(UUID.randomUUID())
                .priority(TaskPriority.LOW)
                .dueDate(LocalDate.now())
                .dateCreated(LocalDateTime.now())
                .dateUpdated(LocalDateTime.now())
                .build();
    }

    public static Contact aRandomContact() {

        return Contact.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john@doe.com")
                .accountId(aAccount().getId())
                .assignedToId(UUID.randomUUID())
                .assignedToName(UUID.randomUUID().toString())
                .archived(false)
                .dateCreated(LocalDateTime.now())
                .dateUpdated(LocalDateTime.now())
                .build();
    }

    public static PaymentMethod aPaymentMethod() {

        return PaymentMethod.builder()
                .cardHolderName("John Doe")
                .creditCardNumber("4111111111111111")
                .expirationDate("12/25")
                .CVV("123")
                .defaultMethod(true)
                .build();
    }

    public static Account aAccount() {

        LocalDateTime now = LocalDateTime.now();
        return Account.builder()
                .id(UUID.randomUUID())
                .plan(aPlan())
                .address(UUID.randomUUID().toString())
                .phoneNumber(UUID.randomUUID().toString())
                .businessName(UUID.randomUUID().toString())
                .companyLogo(UUID.randomUUID().toString())
                .ownerId(UUID.randomUUID())
                .active(true)
                .autoRenewalEnabled(false)
                .dateExpiring(now.plusDays(15))
                .dateCreated(now.minusMonths(3))
                .dateUpdated(now)
                .build();
    }

    public static Plan aPlan() {

        return Plan.builder()
                .id(UUID.randomUUID())
                .planName(PlanName.ESSENTIALS)
                .maxUsers(5)
                .pricePerMonth(BigDecimal.valueOf(19.99))
                .build();
    }

    public static AuthenticationMetadata aMetadataUser() {

        return new AuthenticationMetadata(aRandomUser().getId(),
                aRandomUser().getEmail(),
                aRandomUser().getPassword(),
                aRandomUser().getUserRole(),
                aRandomUser().getAccountId(),
                true);

    }

    public static AuthenticationMetadata aMetadataPrimaryAdmin() {

        return new AuthenticationMetadata(aRandomPrimaryAdmin().getId(),
                aRandomPrimaryAdmin().getEmail(),
                aRandomPrimaryAdmin().getPassword(),
                aRandomPrimaryAdmin().getUserRole(),
                aRandomPrimaryAdmin().getAccountId(),
                true);

    }
}
