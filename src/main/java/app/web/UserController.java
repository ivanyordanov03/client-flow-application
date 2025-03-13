package app.web;

import app.account.model.Account;
import app.account.service.AccountService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.RegisterUserRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AccountService accountService;

    @Autowired
    public UserController(UserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @GetMapping
    public ModelAndView getTeamPage(@AuthenticationPrincipal AuthenticationMetadata data) {

        User user = userService.getById(data.getUserId());
        Account account = accountService.getByOwnerId(user.getId());
        List<User> users = userService.getAllByAccountId(account.getId());

        ModelAndView modelAndView = new ModelAndView("users");
        modelAndView.addObject("user", user);
        modelAndView.addObject("users", users);

        return modelAndView;
    }

    @GetMapping("/new-user")
    public ModelAndView getNewUserPage(@AuthenticationPrincipal AuthenticationMetadata data) {

        User user = userService.getById(data.getUserId());

        ModelAndView modelAndView = new ModelAndView("new-user");
        modelAndView.addObject("registerUserRequest", new RegisterUserRequest());
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PostMapping
    public ModelAndView processNewUserRequest(@Valid @ModelAttribute("registerUserRequest") RegisterUserRequest registerUserRequest,
                                              @AuthenticationPrincipal AuthenticationMetadata data,
                                              BindingResult bindingResult) {

        User user = userService.getById(data.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("new-user");
        }

        UUID accountId = user.getAccountId();
        userService.register(registerUserRequest, accountId);

        return new ModelAndView("redirect:/users");
    }
}
