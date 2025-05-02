package my.bankapp.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString(of = {"login", "name", "isDeleted"})
@EqualsAndHashCode(of = {"id", "login"})
public class User {
    private long id;
    private String login;
    private String name;
    private String password;
    private boolean isDeleted;
}
