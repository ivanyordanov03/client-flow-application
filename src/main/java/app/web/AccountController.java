package app.web;

import app.account.model.Account;
import app.account.service.AccountService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.AccountRequest;
import app.web.mapper.Mapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;

    @Autowired
    public AccountController(AccountService accountService,
                             UserService userService) {

        this.accountService = accountService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getAccountPage(@AuthenticationPrincipal AuthenticationMetadata data) {

        User user = userService.getById(data.getUserId());
        Account account = accountService.getById(user.getAccountId());

        ModelAndView modelAndView = new ModelAndView("account");
        modelAndView.addObject("account", account);
        modelAndView.addObject("user", user);
        modelAndView.addObject("currentPlan", account.getPlan());

        return modelAndView;
    }

    @GetMapping("/settings")
    public ModelAndView getEditAccountPage(@AuthenticationPrincipal AuthenticationMetadata data) {

        Account account = accountService.getByOwnerId(data.getUserId());
        ModelAndView modelAndView = new ModelAndView("account-settings");
        modelAndView.addObject("account", Mapper.mapAccountToAccountRequest(account));

        return modelAndView;
    }

    @PostMapping
    public ModelAndView processAccountRequest(@AuthenticationPrincipal AuthenticationMetadata data,
                                              @Valid AccountRequest accountRequest,
                                              BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();
        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("account-settings");
            modelAndView.addObject("accountRequest", accountRequest);
        }

        accountService.edit(accountRequest, data.getUserId());
        modelAndView.setViewName("redirect:/account");

        return modelAndView;
    }
}
