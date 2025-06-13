package my.bankapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionCreateDto {
    private Long senderAccountId;
    private Long recipientUserId;
    private BigDecimal money;
}
