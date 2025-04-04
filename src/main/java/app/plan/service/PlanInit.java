package app.plan.service;

import app.plan.model.PlanName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PlanInit implements CommandLineRunner {

    private final PlanService planService;

    @Autowired
    public PlanInit(PlanService planService) {

        this.planService = planService;
    }

    @Override
    public void run(String... args) {

        if (planService.getPlans().isEmpty()) {
            for (PlanName planName : PlanName.values()) {
                planService.create(planName);
            }
        } else {
            planService.updateCurrentPrices();
            planService.updateCurrentPlanMaxUsers();
        }
    }
}
