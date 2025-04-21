package my.bankapp.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(of = {"title", "userId", "balance", "type", "isDefault"})
@EqualsAndHashCode(of = {"id", "userId"})
public class Account {
    private long id;
    private long userId;
    private int type;
    private String title;
    private BigDecimal balance;
    private boolean isDefault;

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

    public void addValue(BigDecimal value) {
        this.setBalance(this.balance.add(value));
    }

    public void subtractValue(BigDecimal value) {
        if (value.compareTo(this.balance) > 0) {
            throw new IllegalArgumentException("Not enough money on the account");
        }
        this.setBalance(this.balance.subtract(value));
    }
}
