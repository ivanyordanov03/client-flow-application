package app;

import app.contact.model.Contact;
import app.contact.service.ContactService;
import app.plan.service.PlanInit;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.ContactRequest;
import app.web.dto.UserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class DeleteContactWhenArchivedTest {

    @Autowired
    private UserService userService;
    @Autowired
    private ContactService contactService;
    @Autowired
    private PlanInit planInit;


    @Test
    void deleteArchivedContact_happyPath() {

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
        UUID primaryAdminId = primaryAdmin.getId();

        ContactRequest contactRequest = ContactRequest.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john@smith.com")
                .assignedToId(primaryAdminId.toString())
                .build();
        contactService.create(contactRequest, primaryAdminId);
        List<Contact> adminContacts = contactService
                .getAllByUserRoleFilterAndSortOrderedByName(primaryAdminId,
                        primaryAdmin.getAccountId(),
                        UserRole.PRIMARY_ADMIN.toString(),
                        "my-contacts",
                        null);
        Contact contact = adminContacts.get(0);
        UUID contactId = contact.getId();

        assertEquals("john@smith.com", contact.getEmail());
        contactService.switchArchiveStatus(contactId, primaryAdminId);

        assertTrue(contactService.getById(contactId).isArchived());

        contactService.delete(contactId, primaryAdminId);

        adminContacts = contactService
                .getAllByUserRoleFilterAndSortOrderedByName(primaryAdminId,
                        primaryAdmin.getAccountId(),
                        UserRole.PRIMARY_ADMIN.toString(),
                        "my-contacts",
                        null);
        assertTrue(adminContacts.isEmpty());
    }
}
