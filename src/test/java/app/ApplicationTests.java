package app;

import app.plan.TestPlanInitConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import({TestPlanInitConfig.class})
class ApplicationTests {

    @Test
    void contextLoads() {
    }

}
