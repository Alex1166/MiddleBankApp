package my.bankapp.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserRequest extends IdRequest {
    private String name;
    private String login;
    private String password;
}
