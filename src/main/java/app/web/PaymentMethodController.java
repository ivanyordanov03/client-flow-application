package app.web;

import app.account.model.Account;
import app.account.service.AccountService;
import app.paymentMethod.service.PaymentMethodService;
import app.security.AuthenticationMetadata;
import app.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/payment-methods")
public class PaymentMethodController {

    private final UserService userService;
    private final AccountService accountService;
    private final PaymentMethodService paymentMethodService;

    @Autowired
    public PaymentMethodController(UserService userService,
                                   AccountService accountService,
                                   PaymentMethodService paymentMethodService) {

        this.userService = userService;
        this.accountService = accountService;
        this.paymentMethodService = paymentMethodService;
    }

    @GetMapping()
    public ModelAndView getPaymentSettingsPage(@AuthenticationPrincipal AuthenticationMetadata data) {

        Account account = accountService.getByOwnerId(data.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("payment-settings");
        modelAndView.addObject("user", userService.getById(data.getUserId()));
        modelAndView.addObject("paymentMethods", paymentMethodService.getAllByAccountId(account.getId()));
        modelAndView.addObject("account", account);
        return modelAndView;

    }

    @GetMapping("/payment-method")
    public ModelAndView getPaymentMethodPage(@AuthenticationPrincipal AuthenticationMetadata data) {

        Account account = accountService.getByOwnerId(data.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("payment-method");
        modelAndView.addObject("user", userService.getById(data.getUserId()));
        modelAndView.addObject("paymentMethods", paymentMethodService.getAllByAccountId(account.getId()));
        modelAndView.addObject("account", account);
        return modelAndView;

    }
}
