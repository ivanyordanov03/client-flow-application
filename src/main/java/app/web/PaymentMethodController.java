package app.web;

import app.account.model.Account;
import app.account.service.AccountService;
import app.payment.service.PaymentService;
import app.paymentMethod.model.PaymentMethod;
import app.paymentMethod.service.PaymentMethodService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.PaymentMethodRequest;
import app.web.mapper.Mapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/payment-settings")
@PreAuthorize("hasRole('PRIMARY_ADMIN')")
public class PaymentMethodController {

    private final UserService userService;
    private final AccountService accountService;
    private final PaymentService paymentService;
    private final PaymentMethodService paymentMethodService;

    @Autowired
    public PaymentMethodController(UserService userService,
                                   AccountService accountService,
                                   PaymentService paymentService,
                                   PaymentMethodService paymentMethodService) {

        this.userService = userService;
        this.accountService = accountService;
        this.paymentService = paymentService;
        this.paymentMethodService = paymentMethodService;
    }

    @GetMapping()
    public ModelAndView getPaymentSettingsPage(@AuthenticationPrincipal AuthenticationMetadata data) {

        User user = userService.getById(data.getUserId());
        Account account = accountService.getById(data.getAccountId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("payment-settings");
        modelAndView.addObject("user", user);
        modelAndView.addObject("account", account);
        modelAndView.addObject("lastThreePayments", paymentService.getLastThree(data.getAccountId()));
        modelAndView.addObject("accountPaymentMethods", paymentMethodService.getAllByAccountId(account.getId()));
        return modelAndView;

    }

    @GetMapping("/new")
    public ModelAndView getPaymentMethodPageToCreateNew() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("payment-method");
        modelAndView.addObject("paymentMethodRequest", new PaymentMethodRequest());

        return modelAndView;
    }

    @PostMapping
    public ModelAndView processPaymentMethodRequest(@Valid PaymentMethodRequest paymentMethodRequest,
                                                    BindingResult bindingResult,
                                                    @AuthenticationPrincipal AuthenticationMetadata data) {

        ModelAndView modelAndView = new ModelAndView();

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("payment-method");
            modelAndView.addObject("paymentMethodRequest", paymentMethodRequest);
            return modelAndView;
        }

        User user = userService.getById(data.getUserId());
        paymentMethodService.createNew(paymentMethodRequest, user.getAccountId());

        return new ModelAndView("redirect:/payment-settings");
    }

    @GetMapping("/{id}")
    public ModelAndView getPaymentMethodPageToEdit(@PathVariable UUID id) {

        PaymentMethod paymentMethod = paymentMethodService.getById(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("payment-method");
        modelAndView.addObject("paymentMethodRequest", Mapper.mapPaymentMethodToPaymentMethodRequest(paymentMethod));
        modelAndView.addObject("paymentMethodId", id);

        return modelAndView;
    }

    @PutMapping("/{id}")
    public ModelAndView updatePaymentMethod(@PathVariable UUID id,
                                            @Valid PaymentMethodRequest paymentMethodRequest,
                                            BindingResult bindingResult) {

        ModelAndView modelAndView = new ModelAndView();

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("payment-method");
            modelAndView.addObject("paymentMethodRequest", paymentMethodRequest);
            return modelAndView;
        }

        paymentMethodService.edit(id, paymentMethodRequest);

        return new ModelAndView("redirect:/payment-settings");
    }

    @PutMapping("/{id}/default")
    public ModelAndView setDefaultPaymentMethod(@PathVariable("id") UUID id) {

        paymentMethodService.prepareToSetAsDefaultMethod(id);

        return new ModelAndView("redirect:/payment-settings");
    }

    @DeleteMapping("/{id}")
    public String deletePaymentMethod(@PathVariable UUID id,
                                      @AuthenticationPrincipal AuthenticationMetadata data) {

        User user = userService.getById(data.getUserId());
        Account account = accountService.getById(user.getAccountId());
        accountService.deleteAccountPaymentMethod(id, user.getId(), account.getId());


        return "redirect:/payment-settings";
    }
}
