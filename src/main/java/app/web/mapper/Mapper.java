package app.web.mapper;

import app.plan.model.PlanType;
import app.task.model.TaskPriority;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@UtilityClass
public class Mapper {

    public static PlanType getPlanTypeFromString(String planName) {

        return switch (planName) {
            case "PLUS" -> PlanType.PLUS;
            case "ESSENTIALS" -> PlanType.ESSENTIALS;
            case "SIMPLE_START" -> PlanType.SIMPLE_START;
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
            throw new IllegalArgumentException("Invalid date format. Expected format: M/d/yyyy, got: " + dateString, e);
        }
    }
}
