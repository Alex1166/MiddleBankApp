package my.bankapp.model.request;

import lombok.Data;

import java.util.List;

@Data
public class RequestOperation {
    private boolean isOrOperation;
    private List<RequestCondition> conditionList;
}
