package app.plan.service;

import app.plan.model.Plan;
import app.plan.model.PlanType;
import app.plan.properties.PlanProperties;
import app.plan.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PlanService {

    private final PlanRepository planRepository;
    private final PlanProperties planProperties;

    @Autowired
    public PlanService(PlanRepository planRepository, PlanProperties planProperties) {
        this.planRepository = planRepository;
        this.planProperties = planProperties;
    }

    public List<Plan> getPlans() {

        return planRepository.findAll();
    }

    public void create(PlanType planType) {

        planRepository.save(initializePlan(planType));
    }

    private Plan initializePlan(PlanType planType) {

        BigDecimal price;
        switch (planType) {
            case SIMPLE_START -> price = planProperties.getSimpleStartPrice();
            case ESSENTIALS -> price = planProperties.getEssentialsPrice();
            case PLUS -> price = planProperties.getPlusPrice();
            default -> throw new IllegalStateException("Unexpected value: " + planType);
        }

        return Plan.builder()
                .planType(planType)
                .pricePerMonth(price)
                .build();
    }

    public Plan getByType(PlanType planType) {

        return planRepository.getByPlanType(planType).orElseThrow(() -> new IllegalArgumentException("Plan with name [%s] not found".formatted(planType)));
    }
}
