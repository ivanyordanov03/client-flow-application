package app.plan;

import app.plan.model.PlanName;
import app.plan.properties.PlanProperties;
import app.plan.service.PlanInit;
import app.plan.service.PlanService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.math.BigDecimal;

@TestConfiguration
public class TestPlanInitConfig {

    @Bean
    @Primary
    public PlanInit testPlanInit(PlanService planService) {
        return new PlanInit(planService) {
            @Override
            public void run(String... args) {}
        };
    }

    @Bean
    @Primary
    public PlanProperties testPlanProperties() {
        PlanProperties props = new PlanProperties();
        props.setPriceSimpleStart(new BigDecimal("9.99"));
        props.setPriceEssentials(new BigDecimal("19.99"));
        props.setPricePlus(new BigDecimal("29.99"));
        props.setMaxUsersSimpleStart(1);
        props.setMaxUsersEssentials(5);
        props.setMaxUsersPlus(15);
        return props;
    }
}