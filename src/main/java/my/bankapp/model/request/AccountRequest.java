package my.bankapp.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AccountRequest extends IdRequest {
    private Integer type;
    private String title;
    private Boolean isDefault;
}
