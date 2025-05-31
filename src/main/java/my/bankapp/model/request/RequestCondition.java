package my.bankapp.model.request;

import lombok.Data;

/**
 * @param parameterName private Map<String, List<String>> parameterValues;
 * @param condition     0 - equals, 1 - not equals, 2 - gt, 3 - gte, 4 - lt, 5 - lte, 6 - contains
 */
public record RequestCondition(String parameterName, String parameterValue, Class<?> parameterType, ConditionOperator condition) {
}
