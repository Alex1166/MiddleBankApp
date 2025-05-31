package my.bankapp.service;

import my.bankapp.dao.UserDao;
import my.bankapp.dto.AccountDto;
import my.bankapp.dto.UserDto;
import my.bankapp.exception.AccountNotFoundException;
import my.bankapp.exception.UserNotFoundException;
import my.bankapp.factory.DaoFactory;
import my.bankapp.model.Account;
import my.bankapp.model.User;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.List;
import java.util.Optional;

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
        return new User(userDto.getId(), userDto.getLogin(), userDto.getName(), userDto.getPassword(), userDto.getIsDeleted());
    }

    public Optional<UserDto> getUserByLogin(String login) throws RuntimeException {
        if (userDao.findByLogin(login).isPresent()) {
            return Optional.ofNullable(toDto(userDao.findByLogin(login).get()));
        } else {
            return Optional.empty();
        }
    }

    public Optional<UserDto>  getUserById(long userId) throws RuntimeException {
        if (userDao.findById(userId).isPresent()) {
            return Optional.ofNullable(toDto(userDao.findById(userId).get()));
        } else {
            return Optional.empty();
        }
    }

    public void updateUser(UserDto userDto) {
        if (userDto.getPassword() != null) {
            userDto.setPassword(DigestUtils.md5Hex(userDto.getPassword()));
            userDao.setUserPassword(fromDto(userDto));
        }
        userDao.update(fromDto(userDto));
    }

    public boolean deleteUser(long userId) throws IllegalArgumentException {
        Optional<UserDto> userDto = getUserById(userId);
        if (userDto.isEmpty()) {
            return false;
        }
        userDto.get().setIsDeleted(true);
        userDao.update(fromDto(userDto.get()));
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

        Optional<UserDto> user = getUserByLogin(login);

        String hash = userDao.getUserPassword(fromDto(user.orElseThrow(() -> new UserNotFoundException("User with login %s not found".formatted(login)))));

        return hash.equals(DigestUtils.md5Hex(password));
    }
}
