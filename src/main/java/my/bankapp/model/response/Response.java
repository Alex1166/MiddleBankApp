package my.bankapp.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class Response {
    private final boolean success;
    private final Object result;
}
