package app.web;

import app.account.model.Account;
import app.account.service.AccountService;
import app.plan.model.Plan;
import app.plan.service.PlanService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.RegisterOwnerRequest;
import app.web.mapper.Mapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {

    private final PlanService planService;
    private final UserService userService;
    private final AccountService accountService;

    @Autowired
    public IndexController(PlanService planService,
                           UserService userService,
                           AccountService accountService) {

        this.planService = planService;
        this.userService = userService;
        this.accountService = accountService;
    }

    @GetMapping
    public String getIndexPage() {

        return "index";
    }

    @GetMapping("/login")
    public ModelAndView getLoginPage(@RequestParam(value = "error", required = false) String errorParameter) {

        ModelAndView modelAndView = new ModelAndView("login");
        if (errorParameter != null) {
            modelAndView.addObject("errorMessage", "Incorrect username or password.");
        }

        return modelAndView;
    }

    @GetMapping("/dashboard")
    public ModelAndView getDashboardPage(@AuthenticationPrincipal AuthenticationMetadata data) {

        User user = userService.getById(data.getUserId());
        Account account = accountService.getByOwner(user);

        ModelAndView modelAndView = new ModelAndView("dashboard");
        modelAndView.addObject("user", user);

        if (account != null) {
            if (!account.isActive()) {
                modelAndView.setViewName("payment");
            }
        }

        return modelAndView;
    }

    @GetMapping("/plans")
    public String getPlansPage() {

        return "plans";
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage(@RequestParam(value = "plan", required = false)String planName) {

        if (planName == null || planName.isEmpty()) {
            return new ModelAndView("redirect:/plans");
        }

        Plan plan = planService.getByType(Mapper.getPlanTypeFromString(planName));

        RegisterOwnerRequest registerOwnerRequest = new RegisterOwnerRequest();
        registerOwnerRequest.setPlan(plan);

        ModelAndView modelAndView = new ModelAndView("register");
        modelAndView.addObject("registerOwnerRequest", registerOwnerRequest);

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView processRegisterRequest(@RequestParam(value = "plan")String planName, @Valid RegisterOwnerRequest registerOwnerRequest, BindingResult bindingResult) {

        Plan plan = planService.getByType(Mapper.getPlanTypeFromString(planName));
        registerOwnerRequest.setPlan(plan);

        ModelAndView modelAndView = new ModelAndView("register");
        if (bindingResult.hasErrors()) {
            modelAndView.addObject("registerOwnerRequest", registerOwnerRequest);
            return modelAndView;
        }

        User owner = userService.registerAccountOwner(registerOwnerRequest);
        modelAndView.setViewName("redirect:/login");

        return modelAndView;
    }
}
