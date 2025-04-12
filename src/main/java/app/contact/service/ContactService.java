package app.contact.service;

import app.contact.model.Contact;
import app.contact.repository.ContactRepository;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.ContactRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ContactService {

    private static final String FILTER_ARCHIVED = "archived";
    private static final String FILTER_MY_CONTACTS = "my-contacts";
    private static final String FILTER_ALL_CONTACTS = "all-contacts";
    private static final String FIRST_NAME_LAST_NAME_INITIAL = "%s %s.";
    private static final String SORT_LAST_UPDATED_ASC = "lastUpdatedAsc";
    private static final String SORT_LAST_UPDATED_DESC = "lastUpdatedDesc";
    private static final String UNEXPECTED_FILTER_VALUE = "Unexpected filter value: %s.";
    private static final String CONTACT_WITH_ID_NOT_FOUND = "Contact with id [%s] not found";
    private static final String CONTACT_ID_EDITED_BY_USER_ID = "Contact with id [%s] was edited by user with id [%s]";
    private static final String CONTACT_ID_DELETED_BY_USER_ID = "Contact with id [%s] was permanently deleted by user with id [%s]";
    private static final String CONTACT_ID_SET_BY_USER_ID_TO_ARCHIVE_STATUS = "Contact with id [%s] was set by user with id [%s] to status %s";
    private static final String CONTACT_ID_CREATED_BY_USER_ID_ASSIGNED_TO_USER_ID = "Contact with id [%s] was created by user with id [%s] and assigned to user with id [%s]";

    private final ContactRepository contactRepository;
    private final UserService userService;

    @Autowired
    public ContactService(ContactRepository contactRepository,
                          UserService userService) {

        this.contactRepository = contactRepository;
        this.userService = userService;
    }

    public List<Contact> getAllByUserRoleFilterAndSortOrderedByName(UUID userId, UUID accountId, String userRole, String filter, String sort) {

        if (UserRole.USER.toString().equals(userRole)) {
            return getAllByAssignedToIdFilterAndSortOrderedByName(userId, filter, sort);
        } else {
            return getAllByAccountIdFilterAndSortOrderedByName(userId, accountId, filter, sort);
        }
    }

    private List<Contact> getAllByAccountIdFilterAndSortOrderedByName(UUID assignedToId, UUID accountId, String filter, String sort) {

        if (FILTER_ARCHIVED.equals(filter)) {
            return contactRepository.findAllByAccountIdAndArchivedIsTrueOrderByFirstNameAscLastNameAsc(accountId);
        }
        else if (SORT_LAST_UPDATED_DESC.equals(sort)) {
            return switch (filter) {
                case FILTER_ALL_CONTACTS -> contactRepository.findAllByAccountIdAndArchivedIsFalseOrderByDateUpdatedDescFirstNameAscLastNameAsc(accountId);
                case FILTER_MY_CONTACTS -> getAllByAssignedToIdFilterAndSortOrderedByName(assignedToId, FILTER_MY_CONTACTS, SORT_LAST_UPDATED_DESC);
                default -> throw new IllegalArgumentException(UNEXPECTED_FILTER_VALUE.formatted(filter));
            };
        } else if (SORT_LAST_UPDATED_ASC.equals(sort)) {
            return switch (filter) {
                case FILTER_ALL_CONTACTS -> contactRepository.findAllByAccountIdAndArchivedIsFalseOrderByDateUpdatedAscFirstNameAscLastNameAsc(accountId);
                case FILTER_MY_CONTACTS -> getAllByAssignedToIdFilterAndSortOrderedByName(assignedToId, FILTER_MY_CONTACTS, SORT_LAST_UPDATED_ASC);
                default -> throw new IllegalArgumentException(UNEXPECTED_FILTER_VALUE.formatted(filter));
            };

        } else {
            return switch (filter) {
                case FILTER_ALL_CONTACTS -> contactRepository.findAllByAccountIdAndArchivedIsFalseOrderByFirstNameAscLastNameAsc(accountId);
                case FILTER_MY_CONTACTS -> getAllByAssignedToIdFilterAndSortOrderedByName(assignedToId, FILTER_MY_CONTACTS, null);
                default -> throw new IllegalArgumentException(UNEXPECTED_FILTER_VALUE.formatted(filter));
            };
        }
    }

    private List<Contact> getAllByAssignedToIdFilterAndSortOrderedByName(UUID assignedToId, String filter, String sort) {

        if (FILTER_ARCHIVED.equals(filter)) {
            return contactRepository.findAllByAssignedToIdAndArchivedIsTrueOrderByFirstNameAscLastNameAsc(assignedToId);
        } else if (FILTER_ALL_CONTACTS.equals(filter)) {
            throw new IllegalArgumentException(UNEXPECTED_FILTER_VALUE.formatted(filter));
        } else if (SORT_LAST_UPDATED_DESC.equals(sort)) {
            if (FILTER_MY_CONTACTS.equals(filter)) {
                return contactRepository.findAllByAssignedToIdAndArchivedIsFalseOrderByDateUpdatedDescFirstNameAscLastNameAsc(assignedToId);
            } else {
                throw new IllegalArgumentException(UNEXPECTED_FILTER_VALUE.formatted(filter));
            }
        } else if (SORT_LAST_UPDATED_ASC.equals(sort)) {
            if (FILTER_MY_CONTACTS.equals(filter)) {
                return contactRepository.findAllByAssignedToIdAndArchivedIsFalseOrderByDateUpdatedAscFirstNameAscLastNameAsc(assignedToId);
            } else {
                throw new IllegalArgumentException(UNEXPECTED_FILTER_VALUE.formatted(filter));
            }
        } else {
            if (FILTER_MY_CONTACTS.equals(filter)) {
                return contactRepository.findAllByAssignedToIdAndArchivedIsFalseOrderByFirstNameAscLastNameAsc(assignedToId);
            } else {
                throw new IllegalArgumentException(UNEXPECTED_FILTER_VALUE.formatted(filter));
            }
        }
    }

    public Contact getById(UUID id) {

        return contactRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(CONTACT_WITH_ID_NOT_FOUND.formatted(id)));
    }

    public void edit(UUID id, ContactRequest contactRequest, UUID userId) {

        Contact contact = getById(id);
        User assignee = userService.getById(UUID.fromString(contactRequest.getAssignedToId()));

        contact.setFirstName(contactRequest.getFirstName());
        contact.setLastName(contactRequest.getLastName());
        contact.setEmail(contactRequest.getEmail());
        contact.setAddress(contactRequest.getAddress());
        contact.setPhoneNumber(contactRequest.getPhoneNumber());
        contact.setBusinessName(contactRequest.getBusinessName());
        contact.setAssignedToId(UUID.fromString(contactRequest.getAssignedToId()));
        contact.setAssignedToName(FIRST_NAME_LAST_NAME_INITIAL.formatted(assignee.getFirstName(), assignee.getLastName().charAt(0)));
        contact.setDateUpdated(LocalDateTime.now());

        contactRepository.save(contact);
        log.info(CONTACT_ID_EDITED_BY_USER_ID.formatted(id, userId));
    }

    public void create(ContactRequest contactRequest, UUID userId) {

        Contact contact = initializeNew(contactRequest);
        User user = userService.getById(userId);

        if (!UserRole.USER.equals(user.getUserRole()) && contactRequest.getAssignedToId() != null) {
            User assignee = userService.getById(UUID.fromString(contactRequest.getAssignedToId()));
            contact.setAssignedToId(UUID.fromString(contactRequest.getAssignedToId()));
            contact.setAssignedToName(FIRST_NAME_LAST_NAME_INITIAL.formatted(assignee.getFirstName(), assignee.getLastName().charAt(0)));
        } else {
            contact.setAssignedToId(userId);
            contact.setAssignedToName(FIRST_NAME_LAST_NAME_INITIAL.formatted(user.getFirstName(), user.getLastName().charAt(0)));
        }


        contact.setAccountId(user.getAccountId());
        contactRepository.save(contact);
        log.info(CONTACT_ID_CREATED_BY_USER_ID_ASSIGNED_TO_USER_ID.formatted(contact.getId(), userId, contact.getAssignedToId()));
    }

    private Contact initializeNew(ContactRequest contactRequest) {

        LocalDateTime now = LocalDateTime.now();
        return Contact.builder()
                .firstName(contactRequest.getFirstName())
                .lastName(contactRequest.getLastName())
                .email(contactRequest.getEmail())
                .address(contactRequest.getAddress())
                .phoneNumber(contactRequest.getPhoneNumber())
                .businessName(contactRequest.getBusinessName())
                .dateCreated(now)
                .dateUpdated(now)
                .build();
    }

    public void switchArchiveStatus(UUID id, UUID userId) {

        Contact contact = getById(id);
        contact.setArchived(!contact.isArchived());
        contact.setDateUpdated(LocalDateTime.now());
        contactRepository.save(contact);
        String status = (contact.isArchived()) ? "ARCHIVED" : "RESTORED";
        log.info(CONTACT_ID_SET_BY_USER_ID_TO_ARCHIVE_STATUS.formatted(id, userId, status));
    }

    public void delete(UUID id, UUID userId) {

        if (userService.getById(userId).getUserRole().equals(UserRole.USER)) {
            return;
        }

        contactRepository.delete(getById(id));
        log.info(CONTACT_ID_DELETED_BY_USER_ID.formatted(id, userId));
    }

    public List<Contact> getAllUserContacts(UUID userId) {

        return contactRepository.findAllByAssignedToIdAndArchivedIsFalseOrderByFirstNameAscLastNameAsc(userId);
    }
}
