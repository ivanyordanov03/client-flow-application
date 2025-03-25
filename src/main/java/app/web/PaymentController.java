package app.web;

import app.account.model.Account;
import app.account.service.AccountService;
import app.payment.service.PaymentService;
import app.paymentMethod.service.PaymentMethodService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.PaymentRequest;
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
@RequestMapping("/payments")
public class PaymentController {

    private static final String UNSUCCESSFUL_TRANSACTION = "unsuccessful";
    private static final String UNSUCCESSFUL_TRANSACTION_ERROR_MESSAGE = "The payment was not successful. Please try again.";

    private final UserService userService;
    private final AccountService accountService;
    private final PaymentService paymentService;
    private final PaymentMethodService paymentMethodService;

    @Autowired
    public PaymentController(UserService userService,
                             AccountService accountService,
                             PaymentService paymentService,
                             PaymentMethodService paymentMethodService) {

        this.userService = userService;
        this.accountService = accountService;
        this.paymentService = paymentService;
        this.paymentMethodService = paymentMethodService;
    }

    @GetMapping
    public ModelAndView getPaymentsPage(@AuthenticationPrincipal AuthenticationMetadata data) {

        User user = userService.getById(data.getUserId());
        Account account = accountService.getByOwnerId(user.getId());

        return null;
    }

    @PostMapping
    public ModelAndView processNewPayment(@AuthenticationPrincipal AuthenticationMetadata data,
                                          @Valid PaymentRequest paymentRequest,
                                          BindingResult bindingResult) {

        User user = userService.getById(data.getUserId());
        Account account = accountService.getByOwnerId(user.getId());

        ModelAndView modelAndView = new ModelAndView("subscription-payment");
        modelAndView.addObject("user", user);
        modelAndView.addObject("account", account);
        modelAndView.addObject("paymentRequest", paymentRequest);

        if (bindingResult.hasErrors()) {
            return modelAndView;
        }

        if (paymentRequest.getTransactionType().equals(UNSUCCESSFUL_TRANSACTION)) {
            modelAndView.addObject("errorMessage", UNSUCCESSFUL_TRANSACTION_ERROR_MESSAGE);
            return modelAndView;
        }

        paymentService.insert(paymentRequest, account);

        return new ModelAndView("redirect:/dashboard");
    }

    @GetMapping("/new")
    public ModelAndView getSubscriptionPage(@AuthenticationPrincipal AuthenticationMetadata data) {

        User user = userService.getById(data.getUserId());
        Account account = accountService.getByOwnerId(user.getId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("subscription-payment");
        modelAndView.addObject("user", user);
        modelAndView.addObject("account", account);
        modelAndView.addObject("paymentRequest", new PaymentRequest());
        modelAndView.addObject("accountPaymentMethods", paymentMethodService.getAllByAccountId(account.getId()));

        return modelAndView;
    }
}
