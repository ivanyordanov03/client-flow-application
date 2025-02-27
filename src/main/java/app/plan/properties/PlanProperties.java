package app.plan.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@Data
@ConfigurationProperties(prefix = "plans")
public class PlanProperties {

    private BigDecimal simpleStartPrice;
    private BigDecimal essentialsPrice;
    private BigDecimal plusPrice;
}
