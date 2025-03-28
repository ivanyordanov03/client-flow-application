package app.web;

import app.account.service.AccountService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import app.web.dto.EditUserRequest;
import app.web.dto.UserRequest;
import app.web.mapper.Mapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private static final String ONLY_PRIMARY_ADMIN_CAN_EDIT_PRIMARY_ADMIN = "Access Denied!";

    private final UserService userService;
    private final AccountService accountService;

    @Autowired
    public UserController(UserService userService,
                          AccountService accountService) {

        this.userService = userService;
        this.accountService = accountService;
    }

    @GetMapping
    public ModelAndView getTeamPage(@RequestParam(value = "filter", required = false) String filter,
                                    @ModelAttribute("errorMessage") String errorMessage,
                                    @AuthenticationPrincipal AuthenticationMetadata data) {

        if (filter == null) {
            filter = "current";
        }

        User user = userService.getById(data.getUserId());
        List<User> users = userService.getAllByAccountIdAndFilterOrdered(user.getAccountId(), filter, user.getUserRole().toString());

        ModelAndView modelAndView = new ModelAndView("users");
        modelAndView.addObject("user", user);
        modelAndView.addObject("users", users);
        modelAndView.addObject("filter", filter);
        modelAndView.addObject("errorMessage", errorMessage);

        return modelAndView;
    }

    @GetMapping("/new-user")
    public ModelAndView getNewUserPage(@RequestParam("filter") String filter,
                                       @AuthenticationPrincipal AuthenticationMetadata data) {

        User user = userService.getById(data.getUserId());
        String planName = accountService.getById(user.getAccountId()).getPlan().getPlanName().toString();

        userService.validateUserLimit(user.getAccountId(), planName);

        ModelAndView modelAndView = new ModelAndView("new-user");
        modelAndView.addObject("userRequest", new UserRequest());
        modelAndView.addObject("filter", filter);

        return modelAndView;
    }

    @PostMapping
    public ModelAndView processNewUserRequest(@Valid @ModelAttribute("userRequest") UserRequest userRequest,
                                              BindingResult bindingResult,
                                              @RequestParam(value = "filter", required = false) String filter,
                                              @AuthenticationPrincipal AuthenticationMetadata data) {

        User user = userService.getById(data.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);
        modelAndView.addObject("filter", filter);

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("new-user");
            modelAndView.addObject("userRequest", userRequest);
            return modelAndView;
        }

        UUID accountId = user.getAccountId();
        userService.register(userRequest, accountId);

        return new ModelAndView("redirect:/users");
    }

    @GetMapping("/{id}")
    public ModelAndView getEditUserPage(@PathVariable UUID id,
                                        @RequestParam(value = "filter", required = false) String filter,
                                        @AuthenticationPrincipal AuthenticationMetadata data) {

        User user = userService.getById(id);
        User loggedUser = userService.getById(data.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("filter", filter);

        if (user.getUserRole().equals(UserRole.PRIMARY_ADMIN) && !loggedUser.getUserRole().equals(UserRole.PRIMARY_ADMIN)) {
            modelAndView.setViewName("redirect:/users");
            modelAndView.addObject("errorMessage", ONLY_PRIMARY_ADMIN_CAN_EDIT_PRIMARY_ADMIN);
            return modelAndView;
        }

        modelAndView.setViewName("new-user");
        modelAndView.addObject("userId", id);
        modelAndView.addObject("editUserRequest", Mapper.mapUserToEditUserRequest(user));

        return modelAndView;
    }

    @PutMapping("/{id}")
    public ModelAndView processEditUserRequest(@PathVariable("id") UUID id,
                                               @RequestParam("filter") String filter,
                                               @Valid @ModelAttribute("editUserRequest") EditUserRequest editUserRequest,
                                               BindingResult bindingResult,
                                               @AuthenticationPrincipal AuthenticationMetadata data) {

        User loggedUser = userService.getById(data.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("filter", filter);

        if (editUserRequest.getPassword().isEmpty()) {
            editUserRequest.setPassword(null);
        }

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("new-user");
            modelAndView.addObject("userId", id);
            modelAndView.addObject("editUserRequest", editUserRequest);
            return modelAndView;
        }

        userService.edit(id, editUserRequest, loggedUser.getId());
        return new ModelAndView("redirect:/users");
    }

    @PutMapping("/{id}/status")
    public ModelAndView changeUserStatus(@PathVariable("id") UUID id,
                                         @RequestParam("filter") String filter,
                                         @AuthenticationPrincipal AuthenticationMetadata data) {

        User loggedUser = userService.getById(data.getUserId());
        userService.changeStatus(id, loggedUser.getId());

        ModelAndView modelAndView = new ModelAndView("redirect:/users");
        modelAndView.addObject("filter", filter);

        return modelAndView;
    }

    @PutMapping("/{id}/archive")
    public ModelAndView archiveUser(@PathVariable("id") UUID id,
                                         @RequestParam("filter") String filter,
                                         @AuthenticationPrincipal AuthenticationMetadata data) {

        User loggedUser = userService.getById(data.getUserId());
        userService.archve(id, loggedUser.getId());

        ModelAndView modelAndView = new ModelAndView("redirect:/users");
        modelAndView.addObject("filter", filter);

        return modelAndView;
    }

    @DeleteMapping("/{id}")
    public ModelAndView deleteUser(@PathVariable("id") UUID id,
                                   @RequestParam("filter") String filter,
                                   @AuthenticationPrincipal AuthenticationMetadata data) {

        userService.delete(id, data.getUserId());
        ModelAndView modelAndView = new ModelAndView("redirect:/users");
        modelAndView.addObject("filter", filter);

        return modelAndView;
    }
}
