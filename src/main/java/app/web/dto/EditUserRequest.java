package app.web.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditUserRequest {

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

    @NotEmpty
    @Email(message = "Enter valid email address")
    private String email;

    @Pattern(regexp = "^$|^.{3,20}$", message = "Password must be empty or between 3 and 20 characters") // min = 3 and no mandatory special characters for easier testing
    private String password;

    @NotEmpty
    private String userRoleString;
}
