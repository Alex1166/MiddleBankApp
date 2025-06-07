package my.bankapp.service;

import my.bankapp.dao.UserDao;
import my.bankapp.dto.UserDto;
import my.bankapp.dto.UserReadDto;
import my.bankapp.exception.UserNotFoundException;
import my.bankapp.factory.DaoFactory;
import my.bankapp.model.Account;
import my.bankapp.model.User;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.List;
import java.util.Optional;

public class UserService {

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

    public Optional<UserReadDto> getUserByLogin(String login) throws RuntimeException {
        Optional<User> userByLogin = userDao.findByLogin(login);

        if (userByLogin.isPresent()) {
            User user = userByLogin.get();

            return Optional.of(new UserReadDto(user.getId(), user.getLogin(), user.getName(), user.isDeleted()));
        } else {
            return Optional.empty();
        }
    }

    public Optional<UserReadDto> getUserById(long userId) throws RuntimeException {
        Optional<User> userById = userDao.findById(userId);

        if (userById.isPresent()) {
            User user = userById.get();

            return Optional.of(new UserReadDto(user.getId(), user.getLogin(), user.getName(), user.isDeleted()));
        } else {
            return Optional.empty();
        }
    }

    public void updateUser(UserDto userDto) {
        if (userDto.getPassword() != null) {
            userDto.setPassword(DigestUtils.md5Hex(userDto.getPassword()));
            userDao.setUserPassword(fromDto(userDto));
        }
        System.out.println("updateUser(UserDto userDto) : " + userDto);
        userDao.update(fromDto(userDto));
    }

    public boolean deleteUser(long userId) throws IllegalArgumentException {
        Optional<UserReadDto> userReadDtoOpt = getUserById(userId);
        if (userReadDtoOpt.isEmpty()) {
            return false;
        }

        UserReadDto userReadDto = userReadDtoOpt.get();

        User user = new User(userReadDto.getId(), userReadDto.getLogin(), userReadDto.getName(), null, userReadDto.getIsDeleted());

        userReadDto.setIsDeleted(true);
        userDao.update(user);
        List<Account> accountList = daoFactory.getAccountDao().findAllByUserId(userId).toList();
        if (!accountList.isEmpty()) {
            for (Account account : accountList) {
                account.setDeleted(true);
                daoFactory.getAccountDao().update(account);
            }
        }
        return true;
    }

    public UserReadDto createNewUser(UserDto userDto) {

        String hash = DigestUtils.md5Hex(userDto.getPassword());

        User user = new User(-1, userDto.getLogin(), userDto.getName(), hash, false);
        user = userDao.insert(user);

        return new UserReadDto(user.getId(), user.getLogin(), user.getName(), user.isDeleted());
    }

    public UserReadDto createNewUser(String login, String name, String password) {

        String hash = DigestUtils.md5Hex(password);

        User user = new User(-1, login, name, hash, false);
        user = userDao.insert(user);

        return new UserReadDto(user.getId(), user.getLogin(), user.getName(), user.isDeleted());
    }

    public boolean isPasswordCorrect(String login, String password) throws RuntimeException {

        Optional<User> userByLogin = userDao.findByLogin(login);

        String hash = userDao.getUserPassword(
                userByLogin.orElseThrow(() -> new UserNotFoundException("User with login %s not found".formatted(login))));

        return hash.equals(DigestUtils.md5Hex(password));
    }
}
