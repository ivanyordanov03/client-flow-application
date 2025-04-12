package app.web.mapper;

import app.TestBuilder;
import app.account.model.Account;
import app.contact.model.Contact;
import app.paymentMethod.model.PaymentMethod;
import app.plan.model.PlanName;
import app.task.model.Task;
import app.task.model.TaskPriority;
import app.web.dto.AccountRequest;
import app.web.dto.ContactRequest;
import app.web.dto.PaymentMethodRequest;
import app.web.dto.TaskRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskMapperTest {

    @Test
    void mapStringPlanNameSimpleStartToPlanNameEnum_ReturnsPlanNameSimpleStartAsEnum() {

        String planNameString = "SIMPLE_START";

        PlanName planName = Mapper.mapPlanNameAsStringToPlanTypeEnum(planNameString);
        assertEquals(planName, PlanName.valueOf(planNameString));
        assertEquals(PlanName.SIMPLE_START, planName);
    }

    @Test
    void mapInvalidStringPlanNameSimpleStartToPlanNameEnum_throwsIllegalArgumentException() {

        String invalidPlanNameString = "invalid_name";

        assertThrows(IllegalArgumentException.class, () -> Mapper.mapPlanNameAsStringToPlanTypeEnum(invalidPlanNameString));
    }

    @Test
    void mapTaskPriorityNameLowAsStringToTaskPriorityEnum_returnsTaskPriorityLowAsEnum() {

        String taskPriorityString = "LOW";
        TaskPriority taskPriority = Mapper.mapTaskPriorityAsStringToTaskPriorityEnum(taskPriorityString);

        assertEquals(taskPriority, TaskPriority.valueOf(taskPriorityString));
        assertEquals(TaskPriority.LOW, taskPriority);
    }

    @Test
    void mapTaskPriorityNameUrgentAsStringToTaskPriorityEnum_returnsTaskPriorityUrgentAsEnum() {

        String taskPriorityString = "URGENT";
        TaskPriority taskPriority = Mapper.mapTaskPriorityAsStringToTaskPriorityEnum(taskPriorityString);

        assertEquals(taskPriority, TaskPriority.valueOf(taskPriorityString));
        assertEquals(TaskPriority.URGENT, taskPriority);
    }

    @Test
    void mapInvalidTaskPriorityNameAsStringToTaskPriorityEnum_ThrowsException() {

        String invalidTaskPriorityNameString = "invalid_priority";

        assertThrows(IllegalArgumentException.class, () -> Mapper.mapTaskPriorityAsStringToTaskPriorityEnum(invalidTaskPriorityNameString));
    }

    @Test
    void mapDateAsStringIsoFormatToLocalDateFormat_whenInvalidDateFormat_throwsIllegalArgumentException() {

        String invalidDateFormatString = "invalid_date";

        assertThrows(IllegalArgumentException.class, () -> Mapper.mapDateAsStringIsoFormatToLocalDateFormat(invalidDateFormatString));
    }

    @Test
    void mapTaskToTaskRequest_WithAllFields_ReturnsProperlyMappedRequest() {

        Task task = TestBuilder.aTask();

        TaskRequest result = Mapper.mapTaskToTaskRequest(task);

        assertEquals(task.getName(), result.getName());
        assertEquals(task.getDescription(), result.getDescription());
        assertEquals(task.getDueDate().toString(), result.getDueDate());
        assertEquals(task.getPriority().toString(), result.getPriority());
        assertEquals(task.getAssignedToId().toString(), result.getAssignedTo());
    }

    @Test
    void mapTaskToTaskRequest_WithNullPriority_ReturnsEmptyPriorityString() {

        Task task = TestBuilder.aTask();
        task.setPriority(null);

        TaskRequest result = Mapper.mapTaskToTaskRequest(task);

        assertEquals("", result.getPriority());
    }

    @Test
    void mapTaskToTaskRequest_WithNullDueDate_ThrowsException() {

        Task task = TestBuilder.aTask();
        task.setDueDate(null);

        assertThrows(NullPointerException.class, () -> Mapper.mapTaskToTaskRequest(task));
    }

    @Test
    void mapTaskToTaskRequest_WithNullAssignedTo_ThrowsException() {

        Task task = TestBuilder.aTask();
        task.setAssignedToId(null);

        assertThrows(NullPointerException.class, () -> Mapper.mapTaskToTaskRequest(task));
    }

    @Test
    void mapAccountToAccountRequest_WithAllFields_ReturnsProperlyMappedAccountRequest() {

        Account account = TestBuilder.aAccount();
        AccountRequest result = Mapper.mapAccountToAccountRequest(account);

        assertEquals(account.getCompanyLogo(), result.getLogoURL());
        assertEquals(account.getBusinessName(), result.getBusinessName());
        assertEquals(account.getAddress(), result.getAddress());
        assertEquals(account.getPhoneNumber(), result.getPhoneNumber());
    }

    @Test
    void mapPaymentMethodToPaymentSettingsRequest_WithCompletePaymentMethod_ReturnsProperlyMappedRequest() {

        PaymentMethod paymentMethod = TestBuilder.aPaymentMethod();

        PaymentMethodRequest result = Mapper.mapPaymentMethodToPaymentMethodRequest(paymentMethod);

        assertEquals(paymentMethod.getCardHolderName(), result.getCardholderName());
        assertEquals(paymentMethod.getCreditCardNumber(), result.getCardNumber());
        assertEquals(paymentMethod.getExpirationDate(), result.getExpirationDate());
        assertEquals(paymentMethod.getCVV(), result.getCvv());
        assertEquals(paymentMethod.isDefaultMethod(), result.isDefaultMethod());
    }

    @Test
    void mapContactToContactRequest_WithCompleteContact_ReturnsProperlyMappedRequest() {

        Contact contact = TestBuilder.aRandomContact();

        ContactRequest result = Mapper.mapContactToContactRequest(contact);

        assertEquals(contact.getFirstName(), result.getFirstName());
        assertEquals(contact.getLastName(), result.getLastName());
        assertEquals(contact.getEmail(), result.getEmail());
        assertEquals(contact.getAddress(), result.getAddress());
        assertEquals(contact.getPhoneNumber(), result.getPhoneNumber());
        assertEquals(contact.getBusinessName(), result.getBusinessName());
        assertEquals(contact.getAssignedToId().toString(), result.getAssignedToId());
    }
}
