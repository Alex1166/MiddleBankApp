package my.bankapp.model;

import java.math.BigDecimal;

public class Account {
    private BigDecimal balance;
    private int type;
    private long id;
    private long userId;
    private String title;
    private boolean isDefault;

    public Account() {}

    public Account(long id, long userId, int type, BigDecimal balance, String title, boolean isDefault) {

        this.setBalance(balance);
        this.setType(type);
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.isDefault = isDefault;
    }

    public void setBalance(BigDecimal balance) {
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(String.format("Amount of money must not be less than zero. %s provided", balance));
        }
        this.balance = balance;
    }

    public void setType(int type) {
        if (type > 3) {
            throw new IllegalArgumentException("Wrong account type: " + type);
        }
        this.type = type;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public boolean getIsDefault() {
        return isDefault;
    }

    public String getTitle() {
        return this.title;
    }

    public long getId() {
        return this.id;
    }

    public BigDecimal getBalance() {
        return this.balance;
    }

    public int getType() {
        return this.type;
    }

    public long getUserId() {
        return userId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public void addValue(BigDecimal value) {
        this.setBalance(this.balance.add(value));
    }

    public void subtractValue(BigDecimal value) {
        if (value.compareTo(this.balance) > 0) {
            throw new IllegalArgumentException("Not enough money on the account");
        }
        this.setBalance(this.balance.subtract(value));
    }

    @Override
    public String toString() {
        return "Account:" + this.getId() + "\nBalance:" + this.getBalance() + "\n";
    }
}
