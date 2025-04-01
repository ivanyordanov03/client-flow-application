package app.contact.repository;

import app.contact.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {

    List<Contact> findAllByAccountIdAndArchivedIsTrueOrderByFirstNameAscLastNameAsc(UUID accountId);

    List<Contact> findAllByAccountIdAndArchivedIsFalseOrderByFirstNameAscLastNameAsc(UUID accountId);

    List<Contact> findAllByAccountIdAndArchivedIsFalseOrderByDateUpdatedAscFirstNameAscLastNameAsc(UUID accountId);

    List<Contact> findAllByAccountIdAndArchivedIsFalseOrderByDateUpdatedDescFirstNameAscLastNameAsc(UUID accountId);

    List<Contact> findAllByAssignedToIdAndArchivedIsTrueOrderByFirstNameAscLastNameAsc(UUID assignedToId);

    List<Contact> findAllByAssignedToIdAndArchivedIsFalseOrderByDateUpdatedDescFirstNameAscLastNameAsc(UUID assignedToId);

    List<Contact> findAllByAssignedToIdAndArchivedIsFalseOrderByDateUpdatedAscFirstNameAscLastNameAsc(UUID assignedToId);

    List<Contact> findAllByAssignedToIdAndArchivedIsFalseOrderByFirstNameAscLastNameAsc(UUID assignedToId);
}
