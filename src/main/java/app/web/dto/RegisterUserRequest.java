package app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserRequest {

    @NotEmpty
    @Pattern(regexp = "^[A-Za-zÀ-ÿ]+(?:'[A-Za-zÀ-ÿ]+)?(?:[ -][A-Za-zÀ-ÿ]+)?$",
            message = "First name must contain letters, at most one apostrophe not at start or end, and at most one space or hyphen")
    @Size(min = 2, max = 50, message = "First name must be 2-50 characters")
    private String firstName;

    @NotEmpty
    @Pattern(regexp = "^[A-Za-zÀ-ÿ]+(?:'[A-Za-zÀ-ÿ]+)?(?:[ -][A-Za-zÀ-ÿ]+)?$",
            message = "Last name must contain letters, at most one apostrophe not at start or end, and at most one space or hyphen")
    @Size(min = 2, max = 50, message = "Last name must be 2-50 characters")
    private String lastName;

    @NotEmpty
    @Email(message = "Enter valid email address")
    private String email;

    @NotEmpty
    @Size(min = 3, max = 20, message = "Password must be between 3 and 20 characters long")
    private String password;

//    @Pattern(regexp = "^(?:[2-9]\\d{9})?$", message = "Phone number must be a valid USA format (e.g. 2025550123)")
//    private String phoneNumber;
//
//    @Pattern(regexp = "^(?:[A-Za-z0-9 .-]{3,30})?$", message = "Company name must be 3-30 characters")
//    private String businessName;

    private String planName;
}
