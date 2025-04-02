package app.web;

import app.contact.model.Contact;
import app.contact.service.ContactService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.service.UserService;
import app.web.dto.ContactRequest;
import app.web.mapper.Mapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/contacts")
public class ContactController {

    private final ContactService contactService;
    private final UserService userService;

    @Autowired
    public ContactController(ContactService contactService, UserService userService) {

        this.contactService = contactService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getContactsPage(@RequestParam(value = "filter", required = false) String filter,
                                        @RequestParam(value = "sort", required = false) String sort,
                                        @AuthenticationPrincipal AuthenticationMetadata data) {

        if (filter == null) {
            filter = "my-contacts";
        }

        List<Contact> allByUserRoleFilterAndSortOrderedByName = contactService
                        .getAllByUserRoleFilterAndSortOrderedByName(data.getUserId(), data.getAccountId(), data.getUserRole().toString(), filter, sort);
        ModelAndView modelAndView = new ModelAndView("contacts");
        modelAndView.addObject("filter", filter);
        modelAndView.addObject("userRole", data.getUserRole().toString());
        modelAndView.addObject("user", userService.getById(data.getUserId()));
        modelAndView.addObject("contacts", allByUserRoleFilterAndSortOrderedByName);

        return modelAndView;
    }

    @GetMapping("/new")
    public ModelAndView getNewContactPage(@RequestParam("filter") String filter,
                                          @AuthenticationPrincipal AuthenticationMetadata data) {

        User user = userService.getById(data.getUserId());

        ModelAndView modelAndView = new ModelAndView("new-contact");
        modelAndView.addObject("user", user);
        modelAndView.addObject("filter", filter);
        modelAndView.addObject("contactRequest", new ContactRequest());
        modelAndView.addObject("userRole", data.getUserRole().toString());
        modelAndView.addObject("accountUsers", userService.getAllByAccountIdNotArchivedOrdered(user.getAccountId()));
        return modelAndView;
    }

    @PostMapping
    public ModelAndView processContactRequestNew(@RequestParam("filter") String filter,
                                                 @Valid @ModelAttribute("contactRequest") ContactRequest contactRequest,
                                                 BindingResult bindingResult,
                                                 @AuthenticationPrincipal AuthenticationMetadata data) {

        ModelAndView modelAndView = new ModelAndView("new-contact");
        modelAndView.addObject("filter", filter);

        if (bindingResult.hasErrors()) {
            modelAndView.addObject("accountUsers", userService.getAllByAccountIdNotArchivedOrdered(data.getAccountId()));
            return modelAndView;
        }

        contactService.create(contactRequest, data.getUserId());
        modelAndView.setViewName("redirect:/contacts");
        return modelAndView;
    }

    @GetMapping("/{id}/details")
    public ModelAndView getContactDetailsPage(@PathVariable("id") UUID id,
                                              @RequestParam("filter") String filter,
                                              @AuthenticationPrincipal AuthenticationMetadata data) {

        User user = userService.getById(data.getUserId());
        Contact contact = contactService.getById(id);

        ModelAndView modelAndView = new ModelAndView("contact-details");
        modelAndView.addObject("user", user);
        modelAndView.addObject("filter", filter);
        modelAndView.addObject("contact", contact);
        modelAndView.addObject("userRole", data.getUserRole().toString());
        return modelAndView;
    }

    @GetMapping("/{id}")
    public ModelAndView getEditContactPage(@PathVariable("id") UUID id,
                                           @RequestParam("filter") String filter,
                                           @AuthenticationPrincipal AuthenticationMetadata data) {

        User user = userService.getById(data.getUserId());
        Contact contact = contactService.getById(id);

        ModelAndView modelAndView = new ModelAndView("new-contact");
        modelAndView.addObject("contactId", id);
        modelAndView.addObject("user", user);
        modelAndView.addObject("filter", filter);
        modelAndView.addObject("userRole", data.getUserRole().toString());
        modelAndView.addObject("contactRequest", Mapper.mapContactToContactRequest(contact));
        modelAndView.addObject("accountUsers", userService.getAllByAccountIdNotArchivedOrdered(user.getAccountId()));
        return modelAndView;
    }

    @PutMapping("/{id}")
    public ModelAndView processContactRequestEdit(@PathVariable("id") UUID id,
                                                  @RequestParam("filter") String filter,
                                                  @Valid @ModelAttribute("contactRequest") ContactRequest contactRequest,
                                                  BindingResult bindingResult,
                                                  @AuthenticationPrincipal AuthenticationMetadata data) {

        ModelAndView modelAndView = new ModelAndView("new-contact");
        modelAndView.addObject("filter", filter);

        if (bindingResult.hasErrors()) {
            modelAndView.addObject("contactId", id);
            modelAndView.addObject("accountUsers", userService.getAllByAccountIdNotArchivedOrdered(data.getAccountId()));
            return modelAndView;
        }

        contactService.edit(id, contactRequest, data.getUserId());
        modelAndView.setViewName("redirect:/contacts");
        return modelAndView;
    }

    @PutMapping("/{id}/archive-status")
    public ModelAndView processSwitchArchiveStatusRequest(@PathVariable("id") UUID id,
                                                          @RequestParam("filter") String filter,
                                                          @AuthenticationPrincipal AuthenticationMetadata data) {

        ModelAndView modelAndView = new ModelAndView("redirect:/contacts");
        modelAndView.addObject("filter", filter);
        contactService.switchArchiveStatus(id, data.getUserId());
        return modelAndView;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PRIMARY_ADMIN', 'ADMINISTRATOR')")
    public ModelAndView processDeleteRequest(@PathVariable("id") UUID id,
                                             @RequestParam("filter") String filter,
                                             @AuthenticationPrincipal AuthenticationMetadata data) {

        contactService.delete(id, data.getUserId());
        ModelAndView modelAndView = new ModelAndView("redirect:/contacts");
        modelAndView.addObject("filter", filter);

        return modelAndView;
    }
}
