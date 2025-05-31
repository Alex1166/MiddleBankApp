package my.bankapp.dao;

import lombok.Getter;

@Getter
public enum DaoConditionOperator {
    EQ("="),              // Equals
    GT(">"),              // Greater than
    LT("<"),              // Less than
    GTE(">="),            // Greater than or equal
    LTE("<="),            // Less than or equal
    CONTAINS("LIKE");  // Case-insensitive LIKE

    private final String operator;

    DaoConditionOperator(String operator) {
        this.operator = operator;
    }

    public static DaoConditionOperator fromName(String name) {
        for (DaoConditionOperator op : values()) {
            if (op.name().equalsIgnoreCase(name)) {
                return op;
            }
        }
        return EQ; // Default to EQ if unspecified
    }
}
