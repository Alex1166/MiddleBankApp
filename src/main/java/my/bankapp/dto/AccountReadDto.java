package my.bankapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AccountReadDto {
    private long id;
    private long userId;
    private Integer type;
    private String title;
    private BigDecimal balance;
    private Boolean isDefault;
    private Boolean isDeleted;
}
