package app.contact;

import app.TestBuilder;
import app.contact.model.Contact;
import app.contact.repository.ContactRepository;
import app.contact.service.ContactService;
import app.event.InAppNotificationEventPublisher;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.ContactRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ContactServiceUTest {

    private static final String FILTER_ARCHIVED = "archived";
    private static final String FILTER_MY_CONTACTS = "my-contacts";
    private static final String FILTER_ALL_CONTACTS = "all-contacts";
    private static final String SORT_LAST_UPDATED_ASC = "lastUpdatedAsc";
    private static final String SORT_LAST_UPDATED_DESC = "lastUpdatedDesc";

    private static final UUID userId = UUID.randomUUID();
    private static final UUID accountId = UUID.randomUUID();

    @Mock
    private ContactRepository contactRepository;
    @Mock
    private UserService userService;
    @Mock
    private InAppNotificationEventPublisher inAppNotificationEventPublisher;

    @InjectMocks
    private ContactService contactService;

    @Test
    void givenContact_whenEdited_happyPath() {

        Contact contact = TestBuilder.aRandomContact();
        User user = TestBuilder.aRandomAdmin();
        String assignedToIdAsString = user.getId().toString();

        ContactRequest contactRequest = ContactRequest.builder()
                .firstName("Edited")
                .lastName("Edited description")
                .email("edited@email.com")
                .assignedToId(assignedToIdAsString)
                .build();

        when(contactRepository.findById(contact.getId())).thenReturn(Optional.of(contact));
        when(userService.getById(user.getId())).thenReturn(user);
        doNothing().when(inAppNotificationEventPublisher).send(any());

        contactService.edit(contact.getId(), contactRequest, user.getId());

        assertEquals("Edited", contact.getFirstName());
        assertEquals("Edited description", contact.getLastName());
        assertEquals("edited@email.com", contact.getEmail());
        assertEquals(user.getId(), contact.getAssignedToId());
    }

    @Test
    void userRoleAdmin_withArchivedFilter_returnsArchivedContacts() {

        Contact archivedContact = Contact.builder()
                .archived(true)
                .build();

        when(contactRepository.findAllByAccountIdAndArchivedIsTrueOrderByFirstNameAscLastNameAsc(accountId))
                .thenReturn(List.of(archivedContact));

        List<Contact> result = contactService.getAllByUserRoleFilterAndSortOrderedByName(
                userId, accountId, UserRole.ADMINISTRATOR.toString(), FILTER_ARCHIVED, null);

        assertThat(result).containsExactly(archivedContact);
    }

    @Test
    void userRoleAdmin_withAllContactsFilterUnsorted_returnsNonArchived() {

        Contact activeContact = Contact.builder()
                .archived(false)
                .build();

        when(contactRepository.findAllByAccountIdAndArchivedIsFalseOrderByFirstNameAscLastNameAsc(accountId))
                .thenReturn(List.of(activeContact));

        List<Contact> result = contactService.getAllByUserRoleFilterAndSortOrderedByName(
                userId, accountId, UserRole.ADMINISTRATOR.toString(), FILTER_ALL_CONTACTS, null);

        assertThat(result).containsExactly(activeContact);
    }

    @Test
    void userRoleAdmin_whenUnsortedAndInvalidFilter_throwsException() {

        assertThrows(IllegalArgumentException.class, () -> contactService.getAllByUserRoleFilterAndSortOrderedByName(
                        userId, accountId, UserRole.ADMINISTRATOR.toString(), "INVALID_FILTER", null));
    }

    @Test
    void userRoleAdmin_withAllContactsFilterSortedLastUpdatedDesc_returnsAllContactsSortedLastUpdatedDesc() {

        Contact lastUpdatedContact = Contact.builder()
                .dateUpdated(LocalDateTime.now())
                .build();
        Contact secondToLastUpdatedContact = Contact.builder()
                .dateUpdated(LocalDateTime.now().minusHours(1))
                .build();
        Contact thirdToLastUpdatedContact = Contact.builder()
                .dateUpdated(LocalDateTime.now().minusHours(2))
                .build();

        List<Contact> contacts = List.of(lastUpdatedContact, secondToLastUpdatedContact, thirdToLastUpdatedContact);
        when(contactRepository.findAllByAccountIdAndArchivedIsFalseOrderByDateUpdatedDescFirstNameAscLastNameAsc(accountId))
                .thenReturn(contacts);

        List<Contact> result = contactService.getAllByUserRoleFilterAndSortOrderedByName(
                userId, accountId, UserRole.ADMINISTRATOR.toString(), FILTER_ALL_CONTACTS, SORT_LAST_UPDATED_DESC);

        assertEquals(contacts, result);
    }

    @Test
    void userRoleAdmin_withAllContactsFilterSortedLastUpdatedAsc_returnsAllContactsSortedLastUpdatedAsc() {

        Contact lastUpdatedContact = Contact.builder()
                .dateUpdated(LocalDateTime.now())
                .build();
        Contact secondToLastUpdatedContact = Contact.builder()
                .dateUpdated(LocalDateTime.now().minusHours(1))
                .build();
        Contact thirdToLastUpdatedContact = Contact.builder()
                .dateUpdated(LocalDateTime.now().minusHours(2))
                .build();

        List<Contact> contacts = List.of(thirdToLastUpdatedContact, secondToLastUpdatedContact, lastUpdatedContact);
        when(contactRepository.findAllByAccountIdAndArchivedIsFalseOrderByDateUpdatedAscFirstNameAscLastNameAsc(accountId))
                .thenReturn(contacts);

        List<Contact> result = contactService.getAllByUserRoleFilterAndSortOrderedByName(
                userId, accountId, UserRole.ADMINISTRATOR.toString(), FILTER_ALL_CONTACTS, SORT_LAST_UPDATED_ASC);

        assertEquals(contacts, result);
    }

    @Test
    void userRoleAdmin_withMyContactsFilterSortedLastUpdatedDesc_returnsAllContactsSortedLastUpdatedDesc() {

        Contact lastUpdatedContact = Contact.builder()
                .dateUpdated(LocalDateTime.now())
                .build();
        Contact secondToLastUpdatedContact = Contact.builder()
                .dateUpdated(LocalDateTime.now().minusHours(1))
                .build();
        Contact thirdToLastUpdatedContact = Contact.builder()
                .dateUpdated(LocalDateTime.now().minusHours(2))
                .build();

        List<Contact> contacts = List.of(lastUpdatedContact, secondToLastUpdatedContact, thirdToLastUpdatedContact);
        when(contactRepository.findAllByAssignedToIdAndArchivedIsFalseOrderByDateUpdatedDescFirstNameAscLastNameAsc(userId))
                .thenReturn(contacts);

        List<Contact> result = contactService.getAllByUserRoleFilterAndSortOrderedByName(
                userId, accountId, UserRole.ADMINISTRATOR.toString(), FILTER_MY_CONTACTS, SORT_LAST_UPDATED_DESC);

        assertEquals(contacts, result);
    }

    @Test
    void userRoleAdmin_withMyContactsFilterSortedLastUpdatedAsc_returnsAllContactsSortedLastUpdatedAsc() {

        Contact lastUpdatedContact = Contact.builder()
                .dateUpdated(LocalDateTime.now())
                .build();
        Contact secondToLastUpdatedContact = Contact.builder()
                .dateUpdated(LocalDateTime.now().minusHours(1))
                .build();
        Contact thirdToLastUpdatedContact = Contact.builder()
                .dateUpdated(LocalDateTime.now().minusHours(2))
                .build();

        List<Contact> contacts = List.of(thirdToLastUpdatedContact, secondToLastUpdatedContact, lastUpdatedContact);
        when(contactRepository.findAllByAssignedToIdAndArchivedIsFalseOrderByDateUpdatedAscFirstNameAscLastNameAsc(userId))
                .thenReturn(contacts);

        List<Contact> result = contactService.getAllByUserRoleFilterAndSortOrderedByName(
                userId, accountId, UserRole.ADMINISTRATOR.toString(), FILTER_MY_CONTACTS, SORT_LAST_UPDATED_ASC);

        assertEquals(contacts, result);
    }

    @Test
    void userRoleAdmin_whenSortedLastUpdatedAscInvalidFilter_throwsException() {

        assertThrows(IllegalArgumentException.class, () -> contactService.getAllByUserRoleFilterAndSortOrderedByName(
                userId, accountId, UserRole.ADMINISTRATOR.toString(), "INVALID_FILTER", SORT_LAST_UPDATED_ASC));
    }

    @Test
    void userRoleAdmin_whenSortedLastUpdatedDescInvalidFilter_throwsException() {

        assertThrows(IllegalArgumentException.class, () -> contactService.getAllByUserRoleFilterAndSortOrderedByName(
                userId, accountId, UserRole.ADMINISTRATOR.toString(), "INVALID_FILTER", SORT_LAST_UPDATED_DESC));
    }

    @Test
    void userRoleUser_withArchivedFilter_returnsArchivedContacts() {

        Contact archivedContact = Contact.builder()
                .archived(true)
                .build();

        when(contactRepository.findAllByAccountIdAndArchivedIsTrueOrderByFirstNameAscLastNameAsc(accountId))
                .thenReturn(List.of(archivedContact));

        List<Contact> result = contactService.getAllByUserRoleFilterAndSortOrderedByName(
                userId, accountId, UserRole.ADMINISTRATOR.toString(), FILTER_ARCHIVED, null);

        assertThat(result).containsExactly(archivedContact);
    }

    @Test
    void userRoleUser_delegatesToArchivedContacts() {

        Contact assignedArchivedContact = Contact.builder()
                .archived(true)
                .build();

        when(contactRepository.findAllByAssignedToIdAndArchivedIsTrueOrderByFirstNameAscLastNameAsc(userId))
                .thenReturn(List.of(assignedArchivedContact));

        List<Contact> result = contactService.getAllByUserRoleFilterAndSortOrderedByName(
                userId, accountId, UserRole.USER.toString(), FILTER_ARCHIVED, null);

        assertThat(result).containsExactly(assignedArchivedContact);
    }

    @Test
    void userRoleUser_delegatesToAssignedContacts() {

        Contact assignedContact = Contact.builder()
                .assignedToId(userId)
                .build();

        when(contactRepository.findAllByAssignedToIdAndArchivedIsFalseOrderByFirstNameAscLastNameAsc(userId))
                .thenReturn(List.of(assignedContact));

        List<Contact> result = contactService.getAllByUserRoleFilterAndSortOrderedByName(
                userId, accountId, UserRole.USER.toString(), FILTER_MY_CONTACTS, null);

        assertThat(result).containsExactly(assignedContact);
    }

    @Test
    void userRoleUser_whenUnsortedAndInvalidFilterAllContacts_throwsException() {

        assertThrows(IllegalArgumentException.class, () -> contactService.getAllByUserRoleFilterAndSortOrderedByName(
                userId, accountId, UserRole.USER.toString(), FILTER_ALL_CONTACTS, null));
    }

    @Test
    void userRoleUser_whenSortedLastUpdatedDescAndInvalidFilterAllContacts_throwsException() {

        assertThrows(IllegalArgumentException.class, () -> contactService.getAllByUserRoleFilterAndSortOrderedByName(
                userId, accountId, UserRole.USER.toString(), "INVALID_FILTER", SORT_LAST_UPDATED_DESC));
    }

    @Test
    void userRoleUser_whenSortedLastUpdatedAscAndInvalidFilterAllContacts_throwsException() {

        assertThrows(IllegalArgumentException.class, () -> contactService.getAllByUserRoleFilterAndSortOrderedByName(
                userId, accountId, UserRole.USER.toString(), "INVALID_FILTER", SORT_LAST_UPDATED_ASC));
    }

    @Test
    void userRoleUser_whenUnsortedAndInvalidFilter_throwsException() {

        assertThrows(IllegalArgumentException.class, () -> contactService.getAllByUserRoleFilterAndSortOrderedByName(
                userId, accountId, UserRole.USER.toString(), "INVALID_FILTER", null));
    }
}
