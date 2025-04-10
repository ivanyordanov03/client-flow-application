package app;

import app.contact.model.Contact;
import app.task.model.Task;
import app.task.model.TaskPriority;
import app.user.model.User;
import app.user.model.UserRole;
import lombok.experimental.UtilityClass;

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
                .accountId(UUID.randomUUID())
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
                .accountId(UUID.randomUUID())
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
                .accountId(UUID.randomUUID())
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
                .accountId(UUID.randomUUID())
                .assignedToId(UUID.randomUUID())
                .assignedToName(UUID.randomUUID().toString())
                .archived(false)
                .dateCreated(LocalDateTime.now())
                .dateUpdated(LocalDateTime.now())
                .build();
    }
}
