package app.plan.repository;

import app.plan.model.Plan;
import app.plan.model.PlanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {
    Optional<Plan> getByPlanType(PlanType planType);
}
