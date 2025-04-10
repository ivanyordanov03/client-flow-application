package app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequest {

    @Size(min = 2, max = 80)
    private String name;

    @Size(max = 1500)
    private String description;

    private String dueDate;

    private String priority;

    @NotBlank
    private String assignedTo;
}
