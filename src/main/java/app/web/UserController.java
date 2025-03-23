package app.web;

import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
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

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {

        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getTeamPage(@RequestParam(value = "filter", required = false) String filter,
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

        return modelAndView;
    }

    @GetMapping("/new-user")
    public ModelAndView getNewUserPage() {

        ModelAndView modelAndView = new ModelAndView("new-user");
        modelAndView.addObject("userRequest", new UserRequest());

        return modelAndView;
    }

    @PostMapping
    public ModelAndView processNewUserRequest(@Valid @ModelAttribute("userRequest") UserRequest userRequest,
                                              @AuthenticationPrincipal AuthenticationMetadata data,
                                              BindingResult bindingResult) {

        User user = userService.getById(data.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);

        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("new-user");
        }

        UUID accountId = user.getAccountId();
        userService.register(userRequest, accountId);

        return new ModelAndView("redirect:/users");
    }

    @GetMapping("/{id}")
    public ModelAndView getEditUserPage(@PathVariable UUID id) {

        User user = userService.getById(id);

        ModelAndView modelAndView = new ModelAndView("new-user");
        modelAndView.addObject("userId", id);
        modelAndView.addObject("userRequest", Mapper.mapUserToUserRequest(user));

        return modelAndView;
    }

    @PutMapping("/{id}")
    public ModelAndView processEditUserRequest(@PathVariable("id") UUID id,
                                               @RequestParam("filter") String filter,
                                               @Valid UserRequest userRequest,
                                               @AuthenticationPrincipal AuthenticationMetadata data,
                                               BindingResult bindingResult) {

        User loggedUser = userService.getById(data.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        if (bindingResult.hasErrors()) {
            modelAndView.setViewName("new-user");
            modelAndView.addObject("filter", filter);
            modelAndView.addObject("userRequest", userRequest);
        }

        userService.edit(id, userRequest, loggedUser.getId());
        return modelAndView;
    }
}
