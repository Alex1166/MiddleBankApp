package my.bankapp.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LoginRequest extends IdRequest {
    private String login;
    private String password;
}
