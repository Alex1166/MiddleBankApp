package my.bankapp.dao;

import my.bankapp.exception.DaoException;
import my.bankapp.model.request.GetRequest;
import my.bankapp.model.request.RequestCondition;
import my.bankapp.model.request.RequestOperation;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DaoUtils {

    public static List<DaoValueToInject> applySqlConditions(StringBuilder sql, GetRequest request, Map<String, String> fieldsMap) {

//        Map<Integer, Map<String, String>> parametersToInject = new HashMap<>();
        List<DaoValueToInject> daoValuesToInjectList = new ArrayList<>();
        int parameterCounter = 0;

        if (!request.getFilterBy().isEmpty()) {
            sql.append(" WHERE NOT is_deleted AND ");
//            Iterator<RequestCondition> iterator = request.getFilterBy().iterator();
            Iterator<RequestOperation> iterator = request.getFilterBy().iterator();

            boolean isFirstOperation = true;
            while (iterator.hasNext()) {
                RequestOperation requestOperation = iterator.next();
                if (!isFirstOperation) {
                    sql.append(" AND ");
                }
                sql.append(" ( ");
                boolean isFirstCondition = true;
                for (RequestCondition requestCondition : requestOperation.getConditionList()) {
                    if (fieldsMap.containsKey(requestCondition.parameterName())) {
                        if (!isFirstCondition) {
                            if (requestOperation.isOrOperation()) {
                                sql.append(" OR ");
                            } else {
                                sql.append(" AND ");
                            }
                        }
                        sql.append(fieldsMap.get(requestCondition.parameterName()))
                                .append(DaoConditionOperator.fromName(requestCondition.condition().name()).getOperator())
//                                .append("'")
//                                .append(requestCondition.getParameterValue())
                                .append("?");
//                                .append("'");
                        daoValuesToInjectList.add(new DaoValueToInject(requestCondition.parameterValue(), requestCondition.parameterType()));
//                        daoValuesToInjectList.add(new DaoValueToInject(parameterCounter++, requestCondition.getParameterValue(),
//                                requestCondition.getParameterType()));
//                        sql.append(fieldsMap.get(entry.getKey())).append(" IN ('").append(String.join("', '", entry.getValue())).append("')");
                        isFirstCondition = false;
                    }
                }
                sql.append(" ) ");
                isFirstOperation = false;
            }
        }

        if (request.getSortBy() != null) {
            sql.append(" ORDER BY ");
            Iterator<Map.Entry<String, Boolean>> iterator = request.getSortBy().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Boolean> entry = iterator.next();
                sql.append(entry.getKey()).append(" ").append(entry.getValue() ? "ASC" : "DESC");
                if (iterator.hasNext()) {
                    sql.append(", ");
                }
            }
        }

        if (request.getPage() != null && request.getSize() != null) {
            sql.append(" LIMIT ").append(request.getSize());
            sql.append(" OFFSET ").append(request.getPage() * request.getSize());
        }

        return daoValuesToInjectList;
    }

    public static void setPreparedStatementValues(PreparedStatement preparedStatement, List<DaoValueToInject> daoValuesToInjectList) {

        for (int index = 0; index < daoValuesToInjectList.size(); index++) {
            String value = daoValuesToInjectList.get(index).value();
            Class<?> type = daoValuesToInjectList.get(index).type();
            System.out.println("index = " + index);
            System.out.println("value = " + value);
            System.out.println("type = " + type);
            try {
                if (type == String.class) {
                    preparedStatement.setString(index + 1, value);
                } else if (type == int.class || type == Integer.class) {
                    preparedStatement.setInt(index + 1, Integer.parseInt(value));
                } else if (type == long.class || type == Long.class) {
                    preparedStatement.setLong(index + 1, Long.parseLong(value));
                } else if (type == boolean.class || type == Boolean.class) {
                    preparedStatement.setBoolean(index + 1, Boolean.parseBoolean(value));
                } else if (type == double.class || type == Double.class) {
                    preparedStatement.setDouble(index + 1, Double.parseDouble(value));
                } else {
                    throw new DaoException("Invalid parameter type");
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new DaoException("Exception while creating prepared statement", e);
            }
        }
    }
}
