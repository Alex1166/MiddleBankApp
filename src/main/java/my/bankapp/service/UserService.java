package my.bankapp.service;

import my.bankapp.dao.UserDao;
import my.bankapp.dto.UserDto;
import my.bankapp.model.User;
import org.apache.commons.codec.digest.DigestUtils;

public class UserService {

//    private DaoBank userDao;
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public UserDto toDto(User user) throws IllegalArgumentException {
        return new UserDto(user.getId(), user.getLogin(), user.getName(), user.getPassword());
    }

    public User fromDto(UserDto userDto) throws IllegalArgumentException {
        return new User(userDto.getId(), userDto.getLogin(), userDto.getName(), userDto.getPassword());
    }

    public UserDto getUserByLogin(String login) throws RuntimeException {
        return toDto(userDao.findByLogin(login));
    }

    public UserDto getUserById(long userId) throws RuntimeException {
        return toDto(userDao.findById(userId));
    }

    public UserDto createNewUser(String login, String name, String password) throws RuntimeException {

        String hash = DigestUtils.md5Hex(password);

        User user = new User(-1, login, name, hash);

        return toDto(userDao.insert(user));
    }

    public boolean isPasswordCorrect(String login, String password) throws RuntimeException {

        UserDto user = getUserByLogin(login);

        String hash = userDao.getUserPassword(fromDto(user));

        return hash.equals(DigestUtils.md5Hex(password));
    }
}
