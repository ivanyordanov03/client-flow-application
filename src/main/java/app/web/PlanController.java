package app.web;

import app.account.model.Account;
import app.account.service.AccountService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.PaymentRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/plans")
public class PlanController {

    private final UserService userService;
    private final AccountService accountService;

    public PlanController(UserService userService,
                          AccountService accountService) {

        this.userService = userService;
        this.accountService = accountService;
    }

    @GetMapping
    public String getPlansPage() {

        return "plans";
    }

    @GetMapping("/upgrade")
    public ModelAndView getUpgradePage(@AuthenticationPrincipal AuthenticationMetadata data) {

        User user = userService.getById(data.getUserId());
        Account account = accountService.getByOwnerId(user.getId());

        ModelAndView modelAndView = new ModelAndView("plans");
        modelAndView.addObject("user", user);
        modelAndView.addObject("account", account);
        modelAndView.addObject("totalAccountUsers", userService.getAllByAccountId(account.getId()).size());

        return modelAndView;
    }
}
