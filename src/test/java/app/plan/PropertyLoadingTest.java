package app.plan;

import app.plan.properties.PlanProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class PropertyLoadingTest {

    @Autowired
    private PlanProperties planProperties;

    @Test
    void testPropertiesLoading() {
        assertEquals(BigDecimal.valueOf(9.99) , planProperties.getPriceSimpleStart());
        assertEquals(BigDecimal.valueOf(19.99) , planProperties.getPriceEssentials());
        assertEquals(BigDecimal.valueOf(29.99) , planProperties.getPricePlus());
    }
}