package my.bankapp.dto;

import java.math.BigDecimal;

public class TransactionDto {
    private long id;
    private long senderAccountId;
    private long recipientAccountId;
    private MoneyDto money;

    public TransactionDto() {
    }

    public TransactionDto(long id, long senderAccountId, long recipientAccountId, BigDecimal money) {
        this.id = id;
        this.senderAccountId = senderAccountId;
        this.recipientAccountId = recipientAccountId;
        this.money = new MoneyDto(money);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSenderAccountId() {
        return senderAccountId;
    }

    public void setSenderAccountId(long senderAccountId) {
        this.senderAccountId = senderAccountId;
    }

    public long getRecipientAccountId() {
        return recipientAccountId;
    }

    public void setRecipientAccountId(long recipientAccountId) {
        this.recipientAccountId = recipientAccountId;
    }

    public MoneyDto getMoney() {
        return money;
    }

    public void setMoney(MoneyDto money) {
        this.money = money;
    }

    @Override
    public String toString() {
        return "Transaction:" + this.getId() + "\nSenderAccountId:" + this.getSenderAccountId() + "\nRecipientAccountId:" +
               this.getRecipientAccountId() + "\nMoney:" + this.getMoney() + "\n";
    }
}
