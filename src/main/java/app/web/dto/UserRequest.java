package app.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

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

    @NotEmpty
    @Size(min = 3, max = 20, message = "Password must be between 3 and 20 characters long")
    private String password;

    @NotEmpty
    private String userRoleString;

    private String statusString;

    private String planName;
}
