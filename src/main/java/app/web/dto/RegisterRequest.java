package app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class RegisterRequest {

    @NotEmpty(message = "First name is required")
    @Size(min = 2, max = 20, message = "First name must be between 2 and 20 letters long")
    private String firstName;

    @NotEmpty(message = "Last name is required")
    @Size(min = 2, max = 40, message = "First name must be between 2 and 40 letters long")
    private String lastName;

    @NotEmpty(message = "Email is required")
    @Email(message = "Enter valid email address")
    private String email;

    @NotEmpty(message = "Phone number is required")
    @Pattern(regexp = "^[2-9]\\d{9}$", message = "Phone number must be a valid USA format (e.g., 2025550123)")
    private String phoneNumber;

    private String businessName;

    @URL(message = "Enter valid web address")
    private String domain;
}
