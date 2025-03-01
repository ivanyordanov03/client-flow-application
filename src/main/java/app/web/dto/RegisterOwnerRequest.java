package app.web.dto;

import app.plan.model.Plan;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterOwnerRequest {

    @NotEmpty
    @Size(min = 2, max = 20, message = "First name must be between 2 and 20 letters long")
    private String firstName;

    @NotEmpty
    @Size(min = 2, max = 30, message = "First name must be between 2 and 40 letters long")
    private String lastName;

    @NotEmpty
    @Email(message = "Enter valid email address")
    private String email;

    @NotEmpty
    @Pattern(regexp = "^[2-9]\\d{9}$", message = "Phone number must be a valid USA format (e.g., 2025550123)")
    private String phoneNumber;

    @NotEmpty
    @Size(min = 3, max = 20, message = "Password must be between 3 and 20 characters long")
    private String password;

    private String businessName;

    private Plan plan;
}
