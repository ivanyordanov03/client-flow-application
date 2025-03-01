package app.web.mapper;

import app.plan.model.PlanType;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Mapper {

    public static PlanType getPlanTypeFromString(String planName) {

        return switch (planName) {
            case "PLUS" -> PlanType.PLUS;
            case "ESSENTIALS" -> PlanType.ESSENTIALS;
            case "SIMPLE_START" -> PlanType.SIMPLE_START;
            default -> throw new IllegalStateException("Unexpected value: " + planName);
        };
    }
}
