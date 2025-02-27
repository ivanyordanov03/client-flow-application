package app.plan.service;

import app.plan.model.Plan;
import app.plan.model.PlanName;
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

    public void create(PlanName name) {

        planRepository.save(initializePlan(name));
    }

    private Plan initializePlan(PlanName name) {

        BigDecimal price;
        switch (name) {
            case BASIC -> price = planProperties.getSimpleStartPrice();
            case ESSENTIALS -> price = planProperties.getEssentialsPrice();
            case PLUS -> price = planProperties.getPlusPrice();
            default -> throw new IllegalStateException("Unexpected value: " + name);
        }

        return Plan.builder()
                .name(name)
                .pricePerMonth(price)
                .build();
    }

    public Plan getByName(PlanName planName) {

        return planRepository.getByName(planName).orElseThrow(() -> new IllegalArgumentException("Plan with name [%s] not found".formatted(planName)));
    }
}
