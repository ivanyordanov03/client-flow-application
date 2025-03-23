package app.web.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class AccountRequest {

    @URL
    private String logoURL;

    @Size(max = 50, message = "Business name can not be longer that 50 characters.")
    private String businessName;

    @Size(max = 100, message = "Business address can not be longer that 100 characters.")
    private String address;

    @Pattern(regexp = "^(?:[2-9]\\d{9})?$", message = "Phone number must be a valid USA format (e.g. 2025550123)")
    private String phoneNumber;

}
