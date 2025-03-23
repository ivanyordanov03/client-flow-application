package app.web.mapper;

import app.account.model.Account;
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

    public static PlanName getPlanTypeFromString(String planName) {

        return switch (planName) {
            case "PLUS" -> PlanName.PLUS;
            case "ESSENTIALS" -> PlanName.ESSENTIALS;
            case "SIMPLE_START" -> PlanName.SIMPLE_START;
            default -> throw new IllegalArgumentException("Unexpected plan value: " + planName);
        };
    }

    public static TaskPriority getTaskPriorityFromString(String priorityName) {

        return switch (priorityName) {
            case "LOW" -> TaskPriority.LOW;
            case "MEDIUM" -> TaskPriority.MEDIUM;
            case "HIGH" -> TaskPriority.HIGH;
            case "URGENT" -> TaskPriority.URGENT;
            default -> throw new IllegalArgumentException("Unexpected task value: " + priorityName);
        };
    }

    public static LocalDate getDateFromStringIsoFormat(String dateString) {

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
        taskRequest.setPriority(task.getPriority() != null ? task.getPriority().toString() : "");
        taskRequest.setAssignedTo(task.getAssignedToId().toString());

        return taskRequest;
    }

    public static UserRequest mapUserToUserRequest(User user) {

        UserRequest userRequest = new UserRequest();
        userRequest.setFirstName(user.getFirstName());
        userRequest.setLastName(user.getLastName());
        userRequest.setEmail(user.getEmail());
        userRequest.setUserRoleString(user.getUserRole().toString());

        return userRequest;
    }

    public static AccountRequest mapAccountToAccountRequest(Account account) {

        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setLogoURL(account.getCompanyLogo());
        accountRequest.setBusinessName(account.getBusinessName());
        accountRequest.setAddress(account.getAddress());
        accountRequest.setPhoneNumber(account.getPhoneNumber());

        return accountRequest;
    }

    public static PaymentSettingsRequest mapPaymentRequestToPaymentMethodRequest(PaymentRequest paymentRequest) {

        PaymentSettingsRequest paymentSettingsRequest = new PaymentSettingsRequest();
        paymentSettingsRequest.setCardholderName(paymentRequest.getCardholderName());
        paymentSettingsRequest.setCardNumber(paymentRequest.getCardNumber());
        paymentSettingsRequest.setExpirationDate(paymentRequest.getExpirationDate());
        paymentSettingsRequest.setCvv(paymentRequest.getCvv());

        return paymentSettingsRequest;
    }

    public static PaymentSettingsRequest mapPaymentMethodToPaymentSettingsRequest(PaymentMethod paymentMethod) {

        PaymentSettingsRequest paymentSettingsRequest = new PaymentSettingsRequest();
        paymentSettingsRequest.setCardholderName(paymentMethod.getCardHolderName());
        paymentSettingsRequest.setCardNumber(paymentMethod.getCreditCardNumber());
        paymentSettingsRequest.setExpirationDate(paymentMethod.getExpirationDate());
        paymentSettingsRequest.setCvv(paymentMethod.getCVV());
        paymentSettingsRequest.setDefaultMethod(paymentMethod.isDefaultMethod());

        return paymentSettingsRequest;
    }
}
