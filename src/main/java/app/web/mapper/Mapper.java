package app.web.mapper;

import app.account.model.Account;
import app.contact.model.Contact;
import app.paymentMethod.model.PaymentMethod;
import app.plan.model.PlanName;
import app.task.model.Task;
import app.task.model.TaskPriority;
import app.user.model.User;
import app.web.dto.*;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@UtilityClass
public class Mapper {

    public static PlanName mapPlanNameAsStringToPlanTypeEnum(String planName) {

        return switch (planName) {
            case "PLUS" -> PlanName.PLUS;
            case "ESSENTIALS" -> PlanName.ESSENTIALS;
            case "SIMPLE_START" -> PlanName.SIMPLE_START;
            default -> throw new IllegalArgumentException("Unexpected plan value: " + planName);
        };
    }

    public static TaskPriority mapTaskPriorityAsStringToTaskPriorityEnum(String priorityName) {

        return switch (priorityName) {
            case "LOW" -> TaskPriority.LOW;
            case "MEDIUM" -> TaskPriority.MEDIUM;
            case "HIGH" -> TaskPriority.HIGH;
            case "URGENT" -> TaskPriority.URGENT;
            default -> throw new IllegalArgumentException("Unexpected task value: " + priorityName);
        };
    }

    public static LocalDate mapDateAsStringIsoFormatToLocalDateFormat(String dateString) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return LocalDate.parse(dateString, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected format: yyyy-MM-dd, got: " + dateString, e);
        }
    }

    public static TaskRequest mapTaskToTaskRequest(Task task) {

        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setName(task.getName());
        taskRequest.setDescription(task.getDescription());
        taskRequest.setDueDate(task.getDueDate().toString());
        TaskPriority priority = task.getPriority();
        taskRequest.setPriority(priority != null ? task.getPriority().toString() : "");
        taskRequest.setAssignedTo(task.getAssignedToId().toString());
        return taskRequest;
    }

    public static EditUserRequest mapUserToEditUserRequest(User user) {

        EditUserRequest editUserRequest = new EditUserRequest();
        editUserRequest.setFirstName(user.getFirstName());
        editUserRequest.setLastName(user.getLastName());
        editUserRequest.setEmail(user.getEmail());
        editUserRequest.setUserRoleString(user.getUserRole().toString());

        return editUserRequest;
    }

    public static AccountRequest mapAccountToAccountRequest(Account account) {

        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setLogoURL(account.getCompanyLogo());
        accountRequest.setBusinessName(account.getBusinessName());
        accountRequest.setAddress(account.getAddress());
        accountRequest.setPhoneNumber(account.getPhoneNumber());

        return accountRequest;
    }

    public static PaymentMethodRequest mapPaymentRequestToPaymentMethodRequest(PaymentRequest paymentRequest) {

        PaymentMethodRequest paymentMethodRequest = new PaymentMethodRequest();
        paymentMethodRequest.setCardholderName(paymentRequest.getCardholderName());
        paymentMethodRequest.setCardNumber(paymentRequest.getCardNumber());
        paymentMethodRequest.setExpirationDate(paymentRequest.getExpirationDate());
        paymentMethodRequest.setCvv(paymentRequest.getCvv());

        return paymentMethodRequest;
    }

    public static PaymentMethodRequest mapPaymentMethodToPaymentSettingsRequest(PaymentMethod paymentMethod) {

        PaymentMethodRequest paymentMethodRequest = new PaymentMethodRequest();
        paymentMethodRequest.setCardholderName(paymentMethod.getCardHolderName());
        paymentMethodRequest.setCardNumber(paymentMethod.getCreditCardNumber());
        paymentMethodRequest.setExpirationDate(paymentMethod.getExpirationDate());
        paymentMethodRequest.setCvv(paymentMethod.getCVV());
        paymentMethodRequest.setDefaultMethod(paymentMethod.isDefaultMethod());

        return paymentMethodRequest;
    }

    public static PaymentRequest mapPaymentMethodToPaymentRequest(PaymentMethod paymentMethod) {

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setCardholderName(paymentMethod.getCardHolderName());
        paymentRequest.setCardNumber(paymentMethod.getCreditCardNumber());
        paymentRequest.setExpirationDate(paymentMethod.getExpirationDate());
        paymentRequest.setCvv(paymentMethod.getCVV());

        return paymentRequest;
    }

    public static ContactRequest mapContactToContactRequest(Contact contact) {

        ContactRequest contactRequest = new ContactRequest();
        contactRequest.setFirstName(contact.getFirstName());
        contactRequest.setLastName(contact.getLastName());
        contactRequest.setEmail(contact.getEmail());
        contactRequest.setAddress(contact.getAddress());
        contactRequest.setPhoneNumber(contact.getPhoneNumber());
        contactRequest.setBusinessName(contact.getBusinessName());
        contactRequest.setAssignedToId(contact.getAssignedToId().toString());

        return contactRequest;
    }
}
