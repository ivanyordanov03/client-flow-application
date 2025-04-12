package app.web;

import app.TestBuilder;
import app.configuration.WebMvcConfiguration;
import app.contact.model.Contact;
import app.contact.service.ContactService;
import app.security.AuthenticationMetadata;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContactController.class)
@Import(WebMvcConfiguration.class)
public class ContactControllerApiTest {

    @MockitoBean
    private ContactService contactService;
    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAuthorizedRequestToContactsEndpoint_shouldReturnContactsView() throws Exception {

        AuthenticationMetadata data = TestBuilder.aMetadataUser();
        when(contactService.getAllByUserRoleFilterAndSortOrderedByName(any(), any(), any(), any(), any()))
                .thenReturn(List.of(TestBuilder.aRandomContact()));
        when(userService.getById(data.getUserId())).thenReturn(TestBuilder.aRandomUser());

        mockMvc.perform(get("/contacts").with(user(data)))
                .andExpect(status().isOk())
                .andExpect(view().name("contacts"))
                .andExpect(model().attributeExists("filter", "userRole", "user", "contacts"));
    }

    @Test
    void getAuthorizedRequestToContactsEndpointWithFilter_shouldReturnContactsView() throws Exception {

        AuthenticationMetadata data = TestBuilder.aMetadataPrimaryAdmin();
        when(contactService.getAllByUserRoleFilterAndSortOrderedByName(any(), any(), any(), any(), any()))
                .thenReturn(List.of(TestBuilder.aRandomContact()));
        when(userService.getById(data.getUserId())).thenReturn(TestBuilder.aRandomUser());

        mockMvc.perform(get("/contacts")
                        .param("filter", "archived")
                        .with(user(data)))
                .andExpect(status().isOk())
                .andExpect(view().name("contacts"))
                .andExpect(model().attributeExists("filter", "userRole", "user", "contacts"));
    }

    @Test
    void getAuthorizedRequestToNewContactEndpoint_shouldReturnContactFormView() throws Exception {

        AuthenticationMetadata data = TestBuilder.aMetadataUser();
        when(userService.getById(data.getUserId())).thenReturn(TestBuilder.aRandomUser());
        when(userService.getAllByAccountIdNotArchivedOrdered(data.getAccountId()))
                .thenReturn(List.of(TestBuilder.aRandomUser()));

        mockMvc.perform(get("/contacts/new")
                        .param("filter", "my-contacts")
                        .with(user(data)))
                .andExpect(status().isOk())
                .andExpect(view().name("new-contact"))
                .andExpect(model().attributeExists("filter", "userRole", "user", "contactRequest", "accountUsers"));
    }

    @Test
    void postAuthorizedRequestToNewContactEndpointWithCorrectData_redirectToContacts() throws Exception {

        AuthenticationMetadata data = TestBuilder.aMetadataPrimaryAdmin();

        MockHttpServletRequestBuilder request = post("/contacts")
                .with(user(data))
                .param("filter", "my-contacts")
                .formField("firstName", "Ivan")
                .formField("lastName", "Yo")
                .formField("email", "ivan@mail.com")
                .formField("businessName", "Company name")
                .formField("address", "1212 Address, City, State ZipCode")
                .formField("phoneNumber", "2023034455")
                .formField("assignedToId", data.getUserId().toString())
                .with(csrf());

        doNothing().when(contactService).create(any(), any());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts?filter=my-contacts"));
        verify(contactService, times(1)).create(any(), any());
    }

    @Test
    void postAuthorizedRequestToNewContactEndpointWithIncorrectData_shouldReturnContactFormView() throws Exception {

        AuthenticationMetadata data = TestBuilder.aMetadataPrimaryAdmin();

        MockHttpServletRequestBuilder request = post("/contacts")
                .with(user(data))
                .param("filter", "my-contacts")
                .formField("firstName", "Ivan")
                .formField("lastName", "Yo")
                .formField("email", "ivan@mail")
                .formField("businessName", "Company name")
                .formField("address", "1212 Address, City, State ZipCode")
                .formField("phoneNumber", "202303445")
                .formField("assignedToId", data.getUserId().toString())
                .with(csrf());

        when(userService.getAllByAccountIdNotArchivedOrdered(data.getAccountId()))
                .thenReturn(List.of(TestBuilder.aRandomUser()));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("new-contact"));
        verify(contactService, never()).create(any(), any());
    }

    @Test
    void getAuthorizedRequestToGivenContactDetailsEndpoint_shouldReturnContactDetailsView() throws Exception {

        AuthenticationMetadata data = TestBuilder.aMetadataUser();
        Contact contact = TestBuilder.aRandomContact();
        when(userService.getById(data.getUserId())).thenReturn(TestBuilder.aRandomUser());
        when(contactService.getById(any())).thenReturn(contact);

        mockMvc.perform(get("/contacts/{id}/details", contact.getId())
                        .param("filter", "my-contacts")
                        .with(user(data)))
                .andExpect(status().isOk())
                .andExpect(view().name("contact-details"))
                .andExpect(model().attributeExists("filter", "userRole", "user", "contact"));
    }

    @Test
    void getAuthorizedRequestToEditContactEndpoint_shouldReturnContactFormView() throws Exception {

        AuthenticationMetadata data = TestBuilder.aMetadataUser();
        Contact contact = TestBuilder.aRandomContact();
        when(userService.getById(data.getUserId())).thenReturn(TestBuilder.aRandomUser());
        when(contactService.getById(any())).thenReturn(contact);
        when(userService.getAllByAccountIdNotArchivedOrdered(data.getAccountId()))
                .thenReturn(List.of(TestBuilder.aRandomUser()));

        mockMvc.perform(get("/contacts/{id}", contact.getId())
                        .param("filter", "my-contacts")
                        .with(user(data)))
                .andExpect(status().isOk())
                .andExpect(view().name("new-contact"))
                .andExpect(model().attributeExists("filter", "userRole", "user", "contactId", "contactRequest", "accountUsers"));
    }

    @Test
    void putAuthorizedRequestToEditContactEndpointWithCorrectData_redirectToContacts() throws Exception {

        AuthenticationMetadata data = TestBuilder.aMetadataPrimaryAdmin();
        Contact contact = TestBuilder.aRandomContact();
        MockHttpServletRequestBuilder request = put("/contacts/{id}", contact.getId())
                .with(user(data))
                .param("filter", "my-contacts")
                .formField("firstName", "Ivan")
                .formField("lastName", "Yo")
                .formField("email", "ivan@mail.com")
                .formField("businessName", "Company name")
                .formField("address", "1212 Address, City, State ZipCode")
                .formField("phoneNumber", "2023034455")
                .formField("assignedToId", data.getUserId().toString())
                .with(csrf());

        doNothing().when(contactService).edit(any(), any(), any());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts?filter=my-contacts"));
        verify(contactService, times(1)).edit(any(), any(), any());
    }

    @Test
    void putAuthorizedRequestToEditContactEndpointWithIncorrectData_shouldReturnContactFormView() throws Exception {

        AuthenticationMetadata data = TestBuilder.aMetadataPrimaryAdmin();
        Contact contact = TestBuilder.aRandomContact();
        MockHttpServletRequestBuilder request = put("/contacts/{id}", contact.getId())
                .with(user(data))
                .param("filter", "my-contacts")
                .formField("firstName", "Ivan")
                .formField("lastName", "Yo")
                .formField("email", "ivan@mail")
                .formField("businessName", "Company name")
                .formField("address", "1212 Address, City, State ZipCode")
                .formField("phoneNumber", "202303445")
                .formField("assignedToId", data.getUserId().toString())
                .with(csrf());

        when(userService.getAllByAccountIdNotArchivedOrdered(data.getAccountId()))
                .thenReturn(List.of(TestBuilder.aRandomUser()));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("new-contact"));
        verify(contactService, never()).create(any(), any());
    }

    @Test
    void putAuthorizedRequestToChangeArchiveStatusOfAContactEndpoint_redirectToContacts() throws Exception {

        AuthenticationMetadata data = TestBuilder.aMetadataPrimaryAdmin();
        Contact contact = TestBuilder.aRandomContact();
        MockHttpServletRequestBuilder request = put("/contacts/{id}/archive-status", contact.getId())
                .with(user(data))
                .param("filter", "my-contacts")
                .with(csrf());

        doNothing().when(contactService).switchArchiveStatus(any(), any());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts?filter=my-contacts"));
        verify(contactService, times(1)).switchArchiveStatus(any(), any());
    }

    @Test
    void deleteAuthorizedRequestDeleteContactEndpoint_redirectToContacts() throws Exception {

        AuthenticationMetadata data = TestBuilder.aMetadataPrimaryAdmin();
        Contact contact = TestBuilder.aRandomContact();
        MockHttpServletRequestBuilder request = delete("/contacts/{id}", contact.getId())
                .with(user(data))
                .param("filter", "my-contacts")
                .with(csrf());

        doNothing().when(contactService).delete(any(), any());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts?filter=my-contacts"));
        verify(contactService, times(1)).delete(any(), any());
    }
}
