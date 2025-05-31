package my.bankapp.model.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GetRequest {
    private Long userId;
    private Integer page;
    private Integer size;
//    private Map<String, List<String>> filterBy;

    private List<RequestOperation> filterBy;
    private Map<String, Boolean> sortBy;

    public static final String PAGE_PARAM = "page";
    public static final String SIZE_PARAM = "size";
    public static final String SORT_PARAM = "sort";
}
