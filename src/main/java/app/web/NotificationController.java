package app.web;

import app.notification.service.NotificationService;
import app.security.AuthenticationMetadata;
import app.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService,
                                  UserService userService) {

        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getNotificationsPage (@AuthenticationPrincipal AuthenticationMetadata data) {

        ModelAndView modelAndView = new ModelAndView("notifications");
        modelAndView.addObject("user", userService.getById(data.getUserId()));
        modelAndView.addObject("notifications", notificationService.getNotifications(data.getUserId()));

        return modelAndView;
    }

    @PutMapping
    public ModelAndView processArchiveAllRequests(@AuthenticationPrincipal AuthenticationMetadata data) {

        notificationService.archiveAll(data.getUserId());
        ModelAndView modelAndView = new ModelAndView("redirect:/notifications");
        modelAndView.addObject("user", userService.getById(data.getUserId()));
        modelAndView.addObject("notifications", notificationService.getNotifications(data.getUserId()));

        return modelAndView;
    }
}
