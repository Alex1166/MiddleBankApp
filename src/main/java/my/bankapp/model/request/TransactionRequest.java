package my.bankapp.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
public class TransactionRequest extends IdRequest {
    private Long senderAccountId;
    private Long recipientAccountId;
    private BigDecimal money;
}
