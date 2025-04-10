package app;

import app.account.model.Account;
import app.account.service.AccountService;
import app.payment.service.PaymentService;
import app.plan.service.PlanInit;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.PaymentRequest;
import app.web.dto.UserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class DeleteArchivedUserTest {

    @Autowired
    private AccountService accountService;
    @Autowired
    private UserService userService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private PlanInit planInit;


    @Test
    void registerUser_happyPath() {

        planInit.run();
        UserRequest userRequest = UserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@doe.com")
                .password("password")
                .planName("ESSENTIALS")
                .userRoleString("USER")
                .build();

        userService.registerAccountOwner(userRequest);
        User primaryAdmin = userService.getByEmail("john@doe.com");
        Account account = accountService.getById(primaryAdmin.getAccountId());
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .cardholderName("John Doe")
                .cardNumber("4444010123234545")
                .cvv("201")
                .expirationDate("12/26")
                .planToPurchase(account.getPlan().getPlanName().toString())
                .autoRenewal(true)
                .transactionType("successful")
                .build();
        paymentService.insert(paymentRequest, primaryAdmin.getAccountId());


        UserRequest userRequest1 = UserRequest.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john@smith.com")
                .password("password")
                .userRoleString("USER")
                .planName("ESSENTIALS")
                .build();

        userService.register(userRequest1, primaryAdmin.getAccountId());

        List<User> team = userService.getAllByAccountId(primaryAdmin.getAccountId());
        User accountUser = userService.getByEmail("john@smith.com");
        account = accountService.getById(accountUser.getAccountId());
        UUID primaryAdminId = primaryAdmin.getId();
        UUID accountUserId = accountUser.getId();

        assertTrue(account.isActive());
        assertEquals(2, team.size());
        assertTrue(accountUser.isActive());
        assertTrue(primaryAdmin.isActive());
        assertThat(accountUser.getUserRole(), is(UserRole.USER));
        assertThat(primaryAdmin.getUserRole(), is(UserRole.PRIMARY_ADMIN));
        assertThat(accountUser.getEmail(), is("john@smith.com"));
        assertThat(primaryAdmin.getEmail(), is("john@doe.com"));

        userService.switchStatus(accountUserId, primaryAdminId);
        assertFalse(userService.getById(accountUserId).isActive());

        userService.switchStatus(accountUserId, primaryAdminId);
        assertTrue(accountUser.isActive());

        userService.switchArchveStatus(accountUserId, primaryAdminId);
        accountUser = userService.getById(accountUserId);

        assertFalse(accountUser.isActive());
        assertTrue(accountUser.isArchived());

        userService.delete(accountUserId, primaryAdminId);
        List<User> accountUsers = userService.getAllByAccountId(primaryAdmin.getAccountId());
        assertThat(accountUsers.size(), is(1));
    }
}
