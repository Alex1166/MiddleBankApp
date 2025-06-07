package my.bankapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionReadDto {
    private long id;
    private Long senderAccountId;
    private Long recipientAccountId;
    private BigDecimal money;
    private Timestamp time;
}
