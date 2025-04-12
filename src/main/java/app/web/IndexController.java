package app.web;

import app.account.model.Account;
import app.account.service.AccountService;
import app.contact.service.ContactService;
import app.plan.model.Plan;
import app.plan.service.PlanService;
import app.security.AuthenticationMetadata;
import app.task.service.TaskService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.UserRequest;
import app.web.mapper.Mapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;

@Controller
@RequestMapping
public class IndexController {

    private static final String LOGIN_ERROR_MESSAGE = "Incorrect email or password.";

    private final PlanService planService;
    private final UserService userService;
    private final TaskService taskService;
    private final AccountService accountService;
    private final ContactService contactService;

    @Autowired
    public IndexController(PlanService planService,
                           UserService userService,
                           TaskService taskService,
                           AccountService accountService,
                           ContactService contactService) {

        this.planService = planService;
        this.userService = userService;
        this.taskService = taskService;
        this.accountService = accountService;
        this.contactService = contactService;
    }

    @GetMapping("/")
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
    public ModelAndView getDashboardPage(@AuthenticationPrincipal AuthenticationMetadata data) {

        User user = userService.getById(data.getUserId());
        Account account = accountService.getById(user.getAccountId());

        if (!account.isActive()) {

            if (user.getUserRole().toString().equals("PRIMARY_ADMIN")) {
                return new ModelAndView("redirect:/payments/new");
            } else {
                return new ModelAndView("redirect:/login");
            }

        }

        ModelAndView modelAndView = new ModelAndView("dashboard");
        modelAndView.addObject("user", user);
        modelAndView.addObject("contactsCount", contactService.getAllUserContacts(data.getUserId()).size());
        modelAndView.addObject("tasksDueTodayCount", taskService.getAllDueToday(account.getId(), user.getId(), user.getUserRole().toString()).size());
        modelAndView.addObject("notificationsCounts", new ArrayList<>().size());

        return modelAndView;
    }

    @GetMapping("/register")
    public ModelAndView getRegisterPage(@RequestParam(value = "plan", required = false)String planName) {

        if (planName == null || planName.isEmpty()) {
            return new ModelAndView("redirect:/plans");
        }

        Plan currentPlan = planService.getByName(Mapper.mapPlanNameAsStringToPlanTypeEnum(planName));

        UserRequest userRequest = new UserRequest();
        userRequest.setPlanName(planName);
        userRequest.setUserRoleString("USER");

        ModelAndView modelAndView = new ModelAndView("register");
        modelAndView.addObject("ownerRequest", userRequest);
        modelAndView.addObject("currentPlan", currentPlan);

        return modelAndView;
    }

    @PostMapping("/register")
    public String processRegisterRequest(@Valid UserRequest userRequest,
                                         BindingResult bindingResult,
                                         Model model) {

        Plan currentPlan = planService.getByName(Mapper.mapPlanNameAsStringToPlanTypeEnum(userRequest.getPlanName()));
        if (bindingResult.hasErrors()) {

            model.addAttribute("ownerRequest", userRequest);
            model.addAttribute("planName", userRequest.getPlanName());
            model.addAttribute("currentPlan", currentPlan);
            return "register";
        }

        userService.registerAccountOwner(userRequest);

        return "redirect:/login";
    }

    @GetMapping("/coming-soon")
    public ModelAndView getComingSoonPage(@AuthenticationPrincipal AuthenticationMetadata data) {

        ModelAndView modelAndView = new ModelAndView("coming-soon");
        modelAndView.addObject("user", userService.getById(data.getUserId()));

        return modelAndView;
    }
}
