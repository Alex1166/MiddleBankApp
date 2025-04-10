package my.bankapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.Objects;

public class MoneyDto implements Comparable<MoneyDto> {
    BigDecimal value;

    public MoneyDto() {}

    public MoneyDto(String value) throws NumberFormatException {
        this(new BigDecimal(value));
    }

    public MoneyDto(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(String.format("Amount of money must not be less than zero. %s provided", value));
        }

        this.value = value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getValue() {
        return value;
    }

    public MoneyDto addValue(MoneyDto value) {
        return new MoneyDto(this.value.add(value.getValue()));
    }

    public MoneyDto subtractValue(MoneyDto value) {
        if (value.compareTo(this) > 0) {
            throw new IllegalArgumentException("Not enough money on the account");
        }
        return new MoneyDto(this.value.subtract(value.getValue()));
    }

    @JsonIgnore
    public boolean isZeroOrLess() {
        return this.getValue().compareTo(BigDecimal.ZERO) <= 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MoneyDto money = (MoneyDto) o;
        return Objects.equals(value, money.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public int compareTo(MoneyDto o) {
        return this.getValue().compareTo(o.getValue());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
