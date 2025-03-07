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
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {

    private static final String LOGIN_ERROR_MESSAGE = "Incorrect username or password.";

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
            modelAndView.addObject("errorMessage", LOGIN_ERROR_MESSAGE);
        }

        return modelAndView;
    }

    @GetMapping("/dashboard")
    public ModelAndView getDashboardPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        User user = userService.getById(authenticationMetadata.getUserId());
        Account account = accountService.getByOwner(user);

        if (!account.isActive()) {

            return new ModelAndView("redirect:/payments");
        }

        ModelAndView modelAndView = new ModelAndView("dashboard");
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage(@RequestParam(value = "plan", required = false)String planName) {

        if (planName == null || planName.isEmpty()) {
            return new ModelAndView("redirect:/plans");
        }

        Plan currentPlan = planService.getByType(Mapper.getPlanTypeFromString(planName));

        RegisterOwnerRequest registerOwnerRequest = new RegisterOwnerRequest();
        registerOwnerRequest.setPlanName(planName);

        ModelAndView modelAndView = new ModelAndView("register");
        modelAndView.addObject("registerOwnerRequest", registerOwnerRequest);
        modelAndView.addObject("currentPlan", currentPlan);

        return modelAndView;
    }

    @PostMapping("/register")
    public String processRegisterRequest(@Valid RegisterOwnerRequest registerOwnerRequest,
                                         BindingResult bindingResult,
                                         Model model) {

        Plan currentPlan = planService.getByType(Mapper.getPlanTypeFromString(registerOwnerRequest.getPlanName()));
        if (bindingResult.hasErrors()) {

            model.addAttribute("registerOwnerRequest", registerOwnerRequest);
            model.addAttribute("planName", registerOwnerRequest.getPlanName());
            model.addAttribute("currentPlan", currentPlan);
            return "register";
        }

        userService.registerAccountOwner(registerOwnerRequest, currentPlan);

        return "redirect:/login";
    }
}
