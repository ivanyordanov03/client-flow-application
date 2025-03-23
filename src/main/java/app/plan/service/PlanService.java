package app.plan.service;

import app.plan.model.Plan;
import app.plan.model.PlanName;
import app.account.properties.PlanProperties;
import app.plan.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PlanService {

    private final static String PLAN_NAME_NOT_FOUND = "Plan with name [%s] not found";
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

    public void create(PlanName planName) {

        planRepository.save(initializePlan(planName));
    }

    private Plan initializePlan(PlanName planName) {

        return Plan.builder()
                .planName(planName)
                .pricePerMonth(getPlanPriceFromByPlanName(planName))
                .maxUsers(getMaxActiveUsersByPlanName(planName))
                .build();
    }

    public Plan getByName(PlanName planName) {

        return planRepository.findByPlanName(planName).orElseThrow(() -> new IllegalArgumentException(PLAN_NAME_NOT_FOUND.formatted(planName)));
    }

    public void updateCurrentPrices() {

        planRepository.findAll().forEach(plan -> {plan.setPricePerMonth(getPlanPriceFromByPlanName(plan.getPlanName()));
                                                       planRepository.save(plan);});
    }

    public void updateCurrentPlanMaxUsers() {

        planRepository.findAll().forEach(plan -> {plan.setMaxUsers(getMaxActiveUsersByPlanName(plan.getPlanName()));
                                                       planRepository.save(plan);});
    }

    public BigDecimal getPlanPriceFromByPlanName(PlanName planName) {

        return switch (planName) {
            case SIMPLE_START -> planProperties.getPriceSimpleStart();
            case ESSENTIALS -> planProperties.getPriceEssentials();
            case PLUS -> planProperties.getPricePlus();
        };
    }

    public int getMaxActiveUsersByPlanName(PlanName planName) {

        return switch (planName) {
            case SIMPLE_START -> planProperties.getMaxUsersSimpleStart();
            case ESSENTIALS -> planProperties.getMaxUsersEssentials();
            case PLUS -> planProperties.getMaxUsersPlus();
        };
    }
}
