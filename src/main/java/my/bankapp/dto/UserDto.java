package my.bankapp.dto;

public class UserDto {
    private String login;
    private String name;
    private String password;
    private long id;
//    private long defaultAccountNumber;

    public UserDto(long id,
                   String login,
                   String name,
                   String password
//            , long defaultAccountNumber
    ) {
        this.id = id;
        this.login = login;
        this.name = name;
        this.password = password;
//        this.defaultAccountNumber = defaultAccountNumber;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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

    @Override
    public String toString() {
        return "User:" + this.login + "\n";
    }
}
