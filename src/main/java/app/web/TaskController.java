package app.web;

import app.security.AuthenticationMetadata;
import app.task.model.Task;
import app.task.service.TaskService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.TaskRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    public TaskController(TaskService taskService, UserService userService) {

        this.taskService = taskService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getTasksPage(@AuthenticationPrincipal AuthenticationMetadata data, @RequestParam(value = "filter", required = false)String filter) {

        User user = userService.getById(data.getUserId());
        String userFirstNameAndLastNameInitial = ("%s %s.".formatted(user.getFirstName(), user.getLastName().charAt(0)));

        if (filter == null) {
            filter = user.getUserRole().name().equals("USER") ? "my-tasks" : "all-open";
        }

        ModelAndView modelAndView = new ModelAndView("tasks");
        if(user.getUserRole().name().equals("USER")) {
            modelAndView.addObject("userRoleTasks", taskService.getAllForUserRoleUserByAccountIdUserIdAndFilter(user.getId(), filter));
        } else {
            modelAndView.addObject("accountTasks", taskService.getAllByAccountIdUserIdAndFilter(user.getAccountId(), user.getId(), filter));
        }

        modelAndView.addObject("user", user);
        modelAndView.addObject("filter", filter);
        modelAndView.addObject("userFirstNameAndLastNameInitial", userFirstNameAndLastNameInitial);
        modelAndView.addObject("allAccountTasks", taskService.getAllByAccountId(user.getAccountId()));

        return modelAndView;
    }

    @GetMapping("/new-task")
    public ModelAndView getNewTasksPage(@AuthenticationPrincipal AuthenticationMetadata data) {

        User user = userService.getById(data.getUserId());
        ModelAndView modelAndView = new ModelAndView("new-task");
        modelAndView.addObject("user", user);
        modelAndView.addObject("taskRequest", new TaskRequest());
        modelAndView.addObject("accountUsers", userService.getAllByAccountId(user.getAccountId()));
        modelAndView.addObject("defaultDueDate", LocalDate.now().format(DateTimeFormatter.ofPattern("M/d/yyyy")));

        return modelAndView;
    }

    @PostMapping
    public ModelAndView processTaskRequest(@Valid @ModelAttribute("taskRequest") TaskRequest taskRequest,
                                           @AuthenticationPrincipal AuthenticationMetadata data,
                                           BindingResult bindingResult) {

        User user = userService.getById(data.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("user", user);

        if (bindingResult.hasErrors()) {

            modelAndView.addObject("defaultDueDate", LocalDate.now().format(DateTimeFormatter.ofPattern("M/d/yyyy")));
            modelAndView.addObject("accountUsers", userService.getAllByAccountId(user.getAccountId()));
            modelAndView.setViewName("new-task");
            return modelAndView; // add error messages in the html
        }

        taskService.createNew(taskRequest, data.getUserId());

        modelAndView.setViewName("redirect:/tasks");
        return modelAndView;
    }
}
