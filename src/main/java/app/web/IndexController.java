package app.web;

import app.plan.model.Plan;
import app.plan.model.PlanName;
import app.plan.service.PlanService;
import app.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {

    private final PlanService planService;

    @Autowired
    public IndexController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public String getIndexPage() {

        return "index";
    }

    @GetMapping("/plans")
    public String getPlansPage() {

        return "plans";
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage(@RequestParam ("plan")String planName) {

        ModelAndView modelAndView = new ModelAndView("register");

        Plan plan = planService.getByName(PlanName.valueOf(planName.toUpperCase()));
        modelAndView.addObject("plan", plan);
        modelAndView.addObject("registerRequest", new RegisterRequest());

        return modelAndView;
    }
}
