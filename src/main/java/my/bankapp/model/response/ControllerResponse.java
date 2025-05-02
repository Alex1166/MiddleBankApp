package my.bankapp.model.response;

import lombok.Data;
import lombok.Value;

@Data
public class ControllerResponse<DTO> {
    private final boolean success;
    private final int status;
    private final String type;
    private final DTO result;
}
