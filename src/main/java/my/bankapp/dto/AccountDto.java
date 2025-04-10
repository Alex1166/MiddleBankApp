package my.bankapp.dto;

import java.math.BigDecimal;

public class AccountDto {
    private BigDecimal balance;
    private int type;
    private long id;
    private long userId;
    private String title;
    private boolean isDefault;

    public AccountDto() {}

    public AccountDto(long id, long userId, int type, BigDecimal balance, String title, boolean isDefault) {
        this.balance = balance;
        this.type = type;
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.isDefault = isDefault;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void setType(int type) {
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

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public String toString() {
        return "Account:" + this.getId() + "\nWallet:" + this.getBalance() + "\n";
    }
}
