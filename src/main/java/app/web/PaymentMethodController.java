package app.web;

import app.account.model.Account;
import app.account.service.AccountService;
import app.paymentMethod.model.PaymentMethod;
import app.paymentMethod.service.PaymentMethodDefaultService;
import app.paymentMethod.service.PaymentMethodService;
import app.security.AuthenticationMetadata;
import app.user.service.UserService;
import app.web.dto.PaymentSettingsRequest;
import app.web.mapper.Mapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Controller
@RequestMapping("/payment-settings")
public class PaymentMethodController {

    private final UserService userService;
    private final AccountService accountService;
    private final PaymentMethodService paymentMethodService;
    private final PaymentMethodDefaultService paymentMethodDefaultService;

    @Autowired
    public PaymentMethodController(UserService userService,
                                   AccountService accountService,
                                   PaymentMethodService paymentMethodService,
                                   PaymentMethodDefaultService paymentMethodDefaultService) {

        this.userService = userService;
        this.accountService = accountService;
        this.paymentMethodService = paymentMethodService;
        this.paymentMethodDefaultService = paymentMethodDefaultService;
    }

    @GetMapping()
    public ModelAndView getPaymentSettingsPage(@AuthenticationPrincipal AuthenticationMetadata data) {

        Account account = accountService.getByOwnerId(data.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("payment-settings");
        modelAndView.addObject("user", userService.getById(data.getUserId()));
        modelAndView.addObject("accountPaymentMethods", paymentMethodService.getAllByAccountId(account.getId()));
        modelAndView.addObject("account", account);
        return modelAndView;

    }

    @GetMapping("/new")
    public ModelAndView getPaymentMethodPageToCreateNew() {

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("payment-method");
        modelAndView.addObject("paymentSettingsRequest", new PaymentSettingsRequest());

        return modelAndView;
    }

    @PostMapping
    public ModelAndView processPaymentMethodRequest(@Valid PaymentSettingsRequest paymentSettingsRequest,
                                           BindingResult bindingResult,
                                           @AuthenticationPrincipal AuthenticationMetadata data) {

        ModelAndView modelAndView = new ModelAndView();

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("payment-method");
            modelAndView.addObject("paymentSettingsRequest", paymentSettingsRequest);
        }

        Account account = accountService.getByOwnerId(data.getUserId());
        paymentMethodService.createNew(paymentSettingsRequest, account.getId());

        return new ModelAndView("redirect:/payment-settings");
    }

    @GetMapping("/{id}")
    public ModelAndView getPaymentMethodPageToEdit(@PathVariable UUID id) {

        PaymentMethod paymentMethod = paymentMethodService.getById(id);

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("payment-method");
        modelAndView.addObject("paymentSettingsRequest", Mapper.mapPaymentMethodToPaymentSettingsRequest(paymentMethod));
        modelAndView.addObject("paymentMethodId", id);

        return modelAndView;
    }

    @PutMapping("/{id}")
    public ModelAndView updatePaymentMethod(@PathVariable UUID id,
                                            @Valid PaymentSettingsRequest paymentSettingsRequest,
                                            BindingResult bindingResult) {

        ModelAndView modelAndView = new ModelAndView();

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("payment-method");
            modelAndView.addObject("paymentMethodRequest", paymentSettingsRequest);
            return modelAndView;
        }

        paymentMethodService.edit(id, paymentSettingsRequest);

        return new ModelAndView("redirect:/payment-settings");
    }

    @PutMapping("/{id}/default")
    public ModelAndView setDefaultPaymentMethod(@PathVariable("id") UUID id) {

        paymentMethodDefaultService.setAsDefaultMethod(paymentMethodService.getById(id));

        return new ModelAndView("redirect:/payment-settings");
    }
}
