package my.bankapp.service;

import my.bankapp.dao.UserDao;
import my.bankapp.dto.AccountDto;
import my.bankapp.dto.UserDto;
import my.bankapp.factory.DaoFactory;
import my.bankapp.model.Account;
import my.bankapp.model.User;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.List;

public class UserService {

//    private DaoBank userDao;
    private final UserDao userDao;
    private final DaoFactory daoFactory;

    public UserService(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
        this.userDao = daoFactory.getUserDao();
    }

    public UserDto toDto(User user) throws IllegalArgumentException {
        return new UserDto(user.getId(), user.getLogin(), user.getName(), user.getPassword(), user.isDeleted());
    }

    public User fromDto(UserDto userDto) throws IllegalArgumentException {
        return new User(userDto.getId(), userDto.getLogin(), userDto.getName(), userDto.getPassword(), userDto.isDeleted());
    }

    public UserDto getUserByLogin(String login) throws RuntimeException {
        return toDto(userDao.findByLogin(login));
    }

    public UserDto getUserById(long userId) throws RuntimeException {
        return toDto(userDao.findById(userId));
    }

    public UserDto updateUser(UserDto userDto) {
        if (userDto.getPassword() != null) {
            userDto.setPassword(DigestUtils.md5Hex(userDto.getPassword()));
            userDao.setUserPassword(fromDto(userDto));
        }
        return toDto(userDao.update(fromDto(userDto)));
    }

    public boolean deleteUser(long userId) throws IllegalArgumentException {
        UserDto userDto = getUserById(userId);
        userDto.setDeleted(true);
        userDao.update(fromDto(userDto));
        List<Account> accountList = daoFactory.getAccountDao().findAllByUserId(userId).toList();
        if (!accountList.isEmpty()) {
            for (Account account : accountList) {
                account.setDeleted(true);
                daoFactory.getAccountDao().update(account);
            }
        }
        return true;
    }

    public UserDto createNewUser(UserDto userDto) throws RuntimeException {

        String hash = DigestUtils.md5Hex(userDto.getPassword());

        User user = new User(-1, userDto.getLogin(), userDto.getName(), hash, false);

        return toDto(userDao.insert(user));
    }

    public UserDto createNewUser(String login, String name, String password) throws RuntimeException {

        String hash = DigestUtils.md5Hex(password);

        User user = new User(-1, login, name, hash, false);

        return toDto(userDao.insert(user));
    }

    public boolean isPasswordCorrect(String login, String password) throws RuntimeException {

        UserDto user = getUserByLogin(login);

        String hash = userDao.getUserPassword(fromDto(user));

        return hash.equals(DigestUtils.md5Hex(password));
    }
}
