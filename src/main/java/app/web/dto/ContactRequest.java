package app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContactRequest {

    @NotEmpty
    @Pattern(regexp = "^[A-Za-zÀ-ÿ]+(?:'[A-Za-zÀ-ÿ]+)?(?:[ -][A-Za-zÀ-ÿ]+)?$",
            message = "First name must contain letters, at most one apostrophe not at start or end, and at most one space or hyphen")
    @Size(min = 2, max = 30, message = "First name must be 2-30 characters")
    private String firstName;

    @NotEmpty
    @Pattern(regexp = "^[A-Za-zÀ-ÿ]+(?:'[A-Za-zÀ-ÿ]+)?(?:[ -][A-Za-zÀ-ÿ]+)?$",
            message = "Last name must contain letters, at most one apostrophe not at start or end, and at most one space or hyphen")
    @Size(min = 2, max = 30, message = "Last name must be 2-30 characters")
    private String lastName;

    @Email(message = "Enter valid email address")
    private String email;

    @Size(max = 50, message = "Business name can not be longer that 50 characters.")
    private String businessName;

    @Size(max = 100, message = "Business address can not be longer that 100 characters.")
    private String address;

    @Pattern(regexp = "^(?:[2-9]\\d{9})?$", message = "Phone number must be a valid USA format (e.g. 2025550123)")
    private String phoneNumber;

    private String assignedToId;
}
