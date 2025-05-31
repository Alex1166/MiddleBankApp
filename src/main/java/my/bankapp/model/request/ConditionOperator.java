package my.bankapp.model.request;

import lombok.Getter;

@Getter
public enum ConditionOperator {
    EQ("eq"),              // Equals
    GT("gt"),              // Greater than
    LT("lt"),              // Less than
    GTE("gte"),            // Greater than or equal
    LTE("lte"),            // Less than or equal
    CONTAINS("contains");  // Case-insensitive LIKE

    private final String keyword;

    ConditionOperator(String keyword) {
        this.keyword = keyword;
    }

    public static ConditionOperator fromString(String keyword) {
        for (ConditionOperator op : values()) {
            if (op.keyword.equalsIgnoreCase(keyword)) {
                return op;
            }
        }
        return EQ; // Default to EQ if unspecified
    }
}
