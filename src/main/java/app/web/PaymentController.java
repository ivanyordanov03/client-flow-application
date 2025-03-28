package app.web;

import app.account.model.Account;
import app.account.service.AccountService;
import app.payment.service.PaymentService;
import app.paymentMethod.model.PaymentMethod;
import app.paymentMethod.service.PaymentMethodService;
import app.plan.model.Plan;
import app.plan.service.PlanService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.PaymentRequest;
import app.web.mapper.Mapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    private static final String UNSUCCESSFUL_TRANSACTION = "unsuccessful";
    private static final String UNSUCCESSFUL_TRANSACTION_ERROR_MESSAGE = "The payment was not successful. Please try again.";

    private final UserService userService;
    private final PlanService planService;
    private final AccountService accountService;
    private final PaymentService paymentService;
    private final PaymentMethodService paymentMethodService;

    @Autowired
    public PaymentController(UserService userService,
                             PlanService planService,
                             AccountService accountService,
                             PaymentService paymentService,
                             PaymentMethodService paymentMethodService) {

        this.userService = userService;
        this.planService = planService;
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

    @GetMapping("/new")
    public ModelAndView getSubscriptionPage(@RequestParam(value = "plan", required = false) String planName, @AuthenticationPrincipal AuthenticationMetadata data) {

        User user = userService.getById(data.getUserId());
        Account account = accountService.getByOwnerId(user.getId());

        if (planName == null) {
            planName = account.getPlan().getPlanName().toString();
        }

        Plan planToPurchase = planService.getByName(Mapper.getPlanTypeFromString(planName));

        ModelAndView modelAndView = new ModelAndView("subscription-payment");
        modelAndView.addObject("user", user);
        modelAndView.addObject("account", account);
        modelAndView.addObject("planToPurchase", planToPurchase);
        modelAndView.addObject("paymentRequest", new PaymentRequest());
        modelAndView.addObject("accountPaymentMethods", paymentMethodService.getAllByAccountId(account.getId()));

        return modelAndView;
    }

    @PostMapping
    public ModelAndView processNewPayment(@RequestParam(value = "savedPaymentMethod", required = false) UUID savedPaymentMethodId,
                                          @RequestParam(value = "useSavedMethod", required = false) boolean useSavedMethod,
                                          @RequestParam("planName") String planName,
                                          @AuthenticationPrincipal AuthenticationMetadata data,
                                          @Valid PaymentRequest paymentRequest,
                                          BindingResult bindingResult) {

        User user = userService.getById(data.getUserId());
        Account account = accountService.getByOwnerId(user.getId());
        Plan planToPurchase = planService.getByName(Mapper.getPlanTypeFromString(planName));

        if (useSavedMethod) {

            PaymentMethod paymentMethod = paymentMethodService.getById(savedPaymentMethodId);
            paymentRequest = Mapper.mapPaymentMethodToPaymentRequest(paymentMethod);

        } else {

            ModelAndView modelAndView = new ModelAndView("subscription-payment");
            modelAndView.addObject("user", user);
            modelAndView.addObject("account", account);
            modelAndView.addObject("planToPurchase", planToPurchase);
            modelAndView.addObject("paymentRequest", paymentRequest);
            modelAndView.addObject("accountPaymentMethods", paymentMethodService.getAllByAccountId(account.getId()));

            if (bindingResult.hasErrors()) {
                return modelAndView;
            }

            if (paymentRequest.getTransactionType().equals(UNSUCCESSFUL_TRANSACTION)) {
                modelAndView.addObject("errorMessage", UNSUCCESSFUL_TRANSACTION_ERROR_MESSAGE);
                return modelAndView;
            }
        }
        paymentService.insert(paymentRequest, account.getId());

        return new ModelAndView("redirect:/dashboard");
    }
}
