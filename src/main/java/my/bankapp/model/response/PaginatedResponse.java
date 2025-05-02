package my.bankapp.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaginatedResponse<DTO> extends ControllerResponse<DTO>{
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public PaginatedResponse(boolean success, int status, String type, DTO result, int page, int size, long totalElements, int totalPages) {
        super(success, status, type, result);
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }
}
