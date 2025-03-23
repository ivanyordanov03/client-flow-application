package app.account.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@Data
@ConfigurationProperties(prefix = "plans")
public class PlanProperties {

    private BigDecimal priceSimpleStart;
    private BigDecimal priceEssentials;
    private BigDecimal pricePlus;

    private int maxUsersSimpleStart;
    private int maxUsersEssentials;
    private int maxUsersPlus;
}
