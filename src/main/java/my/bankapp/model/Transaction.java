package my.bankapp.model;

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
@ToString(of = {"senderAccountId", "recipientAccountId", "money"})
@EqualsAndHashCode(of = {"id", "senderAccountId", "recipientAccountId"})
public class Transaction {
    private long id;
    private long senderAccountId;
    private long recipientAccountId;
    private BigDecimal money;
}
