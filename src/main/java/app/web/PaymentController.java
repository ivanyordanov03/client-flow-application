package app.web;

import app.account.model.Account;
import app.account.service.AccountService;
import app.payment.service.PaymentService;
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

    @Autowired
    public PaymentController(UserService userService,
                             AccountService accountService,
                             PaymentService paymentService) {

        this.userService = userService;
        this.accountService = accountService;
        this.paymentService = paymentService;
    }

    @GetMapping
    public ModelAndView getPaymentsPage(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata) {

        User user = userService.getById(authenticationMetadata.getUserId());
        Account account = accountService.getByOwner(user);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("payment");
        modelAndView.addObject("user", user);
        modelAndView.addObject("currentPlan", account.getPlan());
        modelAndView.addObject("paymentRequest", new PaymentRequest());

        return modelAndView;
    }

    @PostMapping
    public ModelAndView processNewPayment(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                          @Valid PaymentRequest paymentRequest,
                                          BindingResult bindingResult) {
        User user = userService.getById(authenticationMetadata.getUserId());
        Account account = accountService.getByOwner(user);

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("payment");
            modelAndView.addObject("user", user);
            modelAndView.addObject("currentPlan", account.getPlan());
            modelAndView.addObject("paymentRequest", paymentRequest);
            return modelAndView;
        }

        if (paymentRequest.getTransactionType().equals(UNSUCCESSFUL_TRANSACTION)) {
            ModelAndView modelAndView = new ModelAndView("payment");
            modelAndView.addObject("user", user);
            modelAndView.addObject("currentPlan", account.getPlan());
            modelAndView.addObject("paymentRequest", paymentRequest);
            modelAndView.addObject("errorMessage", UNSUCCESSFUL_TRANSACTION_ERROR_MESSAGE);

            return modelAndView;
        }

        paymentService.insert(paymentRequest, account);


        return new ModelAndView("redirect:/dashboard");
    }

}
