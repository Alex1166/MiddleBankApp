package my.bankapp.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString(of = {"title", "userId", "balance", "type", "isDefault"})
@EqualsAndHashCode(of = {"id", "userId"})
public class AccountDto {
    private long id;
    private long userId;
    private int type;
    private String title;
    private BigDecimal balance;
    private boolean isDefault;
}
