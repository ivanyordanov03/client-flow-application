package app.web;

import app.exception.EmailAlreadyInUseException;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@Slf4j
public class ExceptionAdvice {

    private static final String FILTER_CURRENT = "current";
    private static final String EMAIL_ALREADY_IN_USE = "Email %s is already in use.";

    private final UserService userService;

    @Autowired
    public ExceptionAdvice(UserService userService) {
        this.userService = userService;
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public String handleEmailAlreadyInUse(EmailAlreadyInUseException e,
                                                HttpServletRequest request,
                                                RedirectAttributes redirectAttributes) {

        String viewName = extractViewName(request.getHeader("referer"), "plans");
        viewName = viewName.equals("plans") ? "/plans" : ("/users/" + viewName);

        String url = "redirect:" + viewName;
        String email = request.getParameter("email");

        redirectAttributes.addFlashAttribute("emailInUseExceptionMessage", EMAIL_ALREADY_IN_USE.formatted(email));
        redirectAttributes.addFlashAttribute("filter", FILTER_CURRENT);

        return url;
    }

//    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalStateException.class,
                       IllegalArgumentException.class})
    public String handleBadRequest(Exception e,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   RedirectAttributes redirectAttributes) {

        response.setStatus(HttpStatus.BAD_REQUEST.value());

        String referer = request.getHeader("Referer");
        String viewName = extractViewName(referer, "dashboard");
        redirectAttributes.addFlashAttribute("badRequestExceptionMessage", e.getMessage());

        String filter = request.getParameter("filter");
        if (filter != null) {
            redirectAttributes.addAttribute("filter", filter);
        }

        return "redirect:/" + viewName;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({AccessDeniedException.class,
                       NoResourceFoundException.class,
                       MethodArgumentTypeMismatchException.class,
                       MissingRequestValueException.class})
    public ModelAndView handleNotFoundExceptions(Exception exception,
                                                 @AuthenticationPrincipal AuthenticationMetadata data) {

        ModelAndView modelAndView = new ModelAndView("not-found");
        addUserToModel(modelAndView, data);

        return modelAndView;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAnyException(Exception exception,
                                           @AuthenticationPrincipal AuthenticationMetadata data) {

        ModelAndView modelAndView = new ModelAndView("internal-server-error");
        modelAndView.addObject("errorMessage", exception.getClass().getSimpleName());
        addUserToModel(modelAndView, data);

        return modelAndView;
    }

    private void addUserToModel(ModelAndView modelAndView, AuthenticationMetadata data) {

        if (data != null) {
            User user = userService.getById(data.getUserId());
            modelAndView.addObject("user", user);
        } else {
            modelAndView.addObject("user", null);
        }
    }

    private String extractViewName(String referer, String defaultViewName) {
        if (referer == null) {
            return defaultViewName;
        }

        int lastSlash = referer.lastIndexOf('/');
        if (lastSlash < 0) {
            return defaultViewName;
        }

        String path = referer.substring(lastSlash + 1);
        int queryIndex = path.indexOf('?');
        return queryIndex > 0 ? path.substring(0, queryIndex) : path;
    }
}