package my.bankapp.dto;

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
@ToString(of = {"login", "name"})
@EqualsAndHashCode(of = {"id", "login"})
public class UserDto {
    private String login;
    private String name;
    private String password;
    private long id;
//    private long defaultAccountNumber;

    public UserDto(long id,
                   String login,
                   String name
//                , String password
//            , long defaultAccountNumber
    ) {
        this.id = id;
        this.login = login;
        this.name = name;
//        this.password = password;
//        this.defaultAccountNumber = defaultAccountNumber;
    }

    //    public String getPassword() {
//        return password;
//    }

//    public void setPassword(String password) {
//        this.password = password;
//    }

//    public void setDefaultAccountNumber(long defaultAccountNumber) {
//        this.defaultAccountNumber = defaultAccountNumber;
//    }
//
//    public long getDefaultAccountNumber() throws IllegalArgumentException {
//        if (this.defaultAccountNumber == -1) {
//            throw new IllegalArgumentException(String.format("User %s does not have a default account", login));
//        }
//        return this.defaultAccountNumber;
//    }
}
