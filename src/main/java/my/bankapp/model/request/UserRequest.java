package my.bankapp.model.request;

import lombok.Data;

@Data
public class UserRequest {
    private String username;
    private String login;
    private String password;
}
