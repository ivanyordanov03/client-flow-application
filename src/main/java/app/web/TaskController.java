package app.web;

import app.security.AuthenticationMetadata;
import app.task.service.TaskService;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.TaskRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
    public ModelAndView getTasksPage(@AuthenticationPrincipal AuthenticationMetadata data) {

        User user = userService.getById(data.getUserId());
        ModelAndView modelAndView = new ModelAndView("tasks");
        modelAndView.addObject("user", user);
        modelAndView.addObject("userTasks", taskService.getAllByAccountIdAndAssignedTo(user.getAccountId(), user.getId()));
        modelAndView.addObject("accountTasks", taskService.getAllByAccountId(user.getAccountId()));

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

        ModelAndView modelAndView = new ModelAndView();

        if (bindingResult.hasErrors()) {

            User user = userService.getById(data.getUserId());
            modelAndView.addObject("user", user);
            modelAndView.addObject("accountUsers", userService.getAllByAccountId(user.getAccountId()));
            modelAndView.addObject("defaultDueDate", LocalDate.now().format(DateTimeFormatter.ofPattern("M/d/yyyy")));
            modelAndView.setViewName("new-task");
            return modelAndView; // add error messages in the html
        }

        taskService.createNew(taskRequest, data.getUserId());

        modelAndView.setViewName("redirect:/tasks");
        return modelAndView;
    }
}
