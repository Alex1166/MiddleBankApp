package my.bankapp.servlet.api.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import my.bankapp.controller.AuthenticatingController;
import my.bankapp.controller.Controller;
import my.bankapp.controller.CreatableController;
import my.bankapp.controller.DeletableController;
import my.bankapp.controller.ReadableController;
import my.bankapp.controller.UpdatableController;
import my.bankapp.dto.UserDto;
import my.bankapp.dto.UserReadDto;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.request.ConditionOperator;
import my.bankapp.model.request.GetRequest;
import my.bankapp.model.request.IdRequest;
import my.bankapp.model.request.RequestCondition;
import my.bankapp.model.request.RequestOperation;
import my.bankapp.model.response.ControllerResponse;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: tests, jwt

@WebServlet(urlPatterns = "/api/*")
public class MainServletApi extends ApiHttpServlet {

    private static final String FULL_STABLE_VERSION = "2.0.0";
    private static final String METHOD_NOT_SUPPORTED = "МЕТОД НЕ ПОДДЕРЖИВАЕТСЯ";
    private static final Pattern ID_PATH_PATTERN = Pattern.compile(".*/\\d+/?$");
    private static final Pattern API_RESOURCE_ID_PATTERN = Pattern.compile("^/api/v\\d+/[a-z]+/\\d+$");
    private static final Pattern API_RESOURCE_VERSION_PATTERN = Pattern.compile("^/api/v([\\d.]+)");
    private static final Pattern API_RESOURCE_ENDPOINT_PATTERN = Pattern.compile("^/api/v[\\d.]+(/[^/]+)");
    private ServiceFactory serviceFactory;
    private Logger logger;
    //    private Map<String, Controller<?, ?>> controllerMap;
//    private Map<String, Map<Integer, Controller<?, ?>>> controllerMap;
    private Map<String, Map<String, Controller<?, ?>>> versionedControllerMap;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {

        Object attr = getServletContext().getAttribute("serviceFactory");
        if (!(attr instanceof ServiceFactory factory)) {
            throw new IllegalStateException("ServiceFactory not found in servlet context.");
        }
        this.serviceFactory = factory;

        this.versionedControllerMap = new HashMap<>();
        for (Controller<?, ?> controller : serviceFactory.getControllerFactory().getAllControllers()) {
            versionedControllerMap.computeIfAbsent(controller.getVersion(), v -> new HashMap<>()).put(controller.getPath(), controller);
        }

        this.objectMapper = new ObjectMapper();
        this.logger = ServiceFactory.createLogger(MainServletApi.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String path = req.getServletPath() + req.getPathInfo();

        logger.debug("req.getContextPath() = " + req.getContextPath());
        logger.debug("req.getRequestURI() = " + req.getRequestURI());
        logger.debug("req.getServletPath() = " + req.getServletPath());
        logger.debug("req.getPathInfo() = " + req.getPathInfo());

//        path = trimPath(path);

        try {
            boolean isGetAll = !isDetailRequest(path);
            logger.debug("isGetAll = " + isGetAll);
            String version = extractVersionFromPath(path);
            logger.debug("version = " + version);
            String endpoint = extractEndpointFromPath(path);
            logger.debug("endpoint = " + endpoint);

            Controller<?, ?> currentController = versionedControllerMap.getOrDefault(version, versionedControllerMap.get(FULL_STABLE_VERSION))
                    .get(endpoint);

            ControllerResponse<?> response;

            if (isControllerNotAccessible(req, resp, currentController)) {
                return;
            }

            if (currentController instanceof ReadableController<?, ?> readableController) {
                if (isGetAll) {

                    Map<String, Class<?>> dtoFields = getDtoFieldNames(readableController.getReadableDtoClass());

                    GetRequest request = parseQueryParameters(req.getParameterMap(), dtoFields);
                    request.setUserId(extractIdFromSession(req));

                    response = readableController.processGetAll(request, serviceFactory);

//                    response = readableController.processGetAll(extractIdFromSession(req), serviceFactory);
                } else {
                    response = readableController.processGet(extractIdFromPath(path), serviceFactory);
                }
                logger.debug(response);
            } else {
                writeError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, METHOD_NOT_SUPPORTED);
                return;
            }

            writeResponse(resp, response);

        } catch (Exception e) {
            logger.error("ERROR: ", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String path = req.getServletPath() + req.getPathInfo();

        try {
            String version = extractVersionFromPath(path);
            logger.debug("version = " + version);
            String endpoint = extractEndpointFromPath(path);
            logger.debug("endpoint = " + endpoint);
            logger.debug(req.getPathInfo());
            logger.debug(req.getContextPath());
            logger.debug(req.getServletPath());
            logger.debug(req.getQueryString());

            if (!checkEndpointHasNoId(path, endpoint)) {
                writeError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, METHOD_NOT_SUPPORTED);
                return;
            }

            Controller<?, ?> currentController = versionedControllerMap.getOrDefault(version, versionedControllerMap.get(FULL_STABLE_VERSION))
                    .get(endpoint);

            if (isControllerNotAccessible(req, resp, currentController)) {
                return;
            }

            ControllerResponse<?> response;
            if (currentController instanceof CreatableController<?, ?> rawCreatableController) {

                Object data = objectMapper.readValue(req.getInputStream(), rawCreatableController.getCreatableRequestClass());

                @SuppressWarnings("unchecked")
                CreatableController<?, Object> creatableController =
                        (CreatableController<?, Object>) rawCreatableController;
//                if (!(currentController instanceof AuthenticatingController) && data.getUserId() == null) {
//                    data.setUserId(extractIdFromSession(req));
//                    if (data.getUserId() == null) {
//                        throw new RuntimeException("Current user is undefined");
//                    }
//                }
                logger.debug(data);
                response = creatableController.processCreate(data, serviceFactory);
                logger.debug(response);
            } else {
                writeError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, METHOD_NOT_SUPPORTED);
                return;
            }

            if (currentController instanceof AuthenticatingController) {
                createSession(req, response);
            }

            writeResponse(resp, response);

        } catch (Exception e) {
            logger.error("ERROR: ", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String path = req.getServletPath() + req.getPathInfo();

        try {
            String version = extractVersionFromPath(path);
            logger.debug("version = " + version);
            String endpoint = extractEndpointFromPath(path);
            logger.debug("endpoint = " + endpoint);

            Controller<?, ?> currentController = versionedControllerMap.getOrDefault(version, versionedControllerMap.get(FULL_STABLE_VERSION))
                    .get(endpoint);

            if (isControllerNotAccessible(req, resp, currentController)) {
                return;
            }

            ControllerResponse<?> response;
            if (currentController instanceof UpdatableController<?, ?> rawUpdatableController) {

                Object data = objectMapper.readValue(req.getInputStream(), rawUpdatableController.getUpdatableRequestClass());

                @SuppressWarnings("unchecked")
                UpdatableController<?, Object> updatableController =
                        (UpdatableController<?, Object>) rawUpdatableController;
                long id = extractIdFromPath(path);
//                if (data.getId() == null) {
//                    throw new RuntimeException("Target object is undefined");
//                }
                logger.debug(data);
                response = updatableController.processUpdate(id, data, serviceFactory);
                logger.debug(response);
            } else {
                writeError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, METHOD_NOT_SUPPORTED);
                return;
            }

            if (currentController instanceof AuthenticatingController) {
                createSession(req, response);
            }

            writeResponse(resp, response);

        } catch (Exception e) {
            logger.error("ERROR: ", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String path = req.getServletPath() + req.getPathInfo();

        try {
            String version = extractVersionFromPath(path);
            logger.debug("version = " + version);
            String endpoint = extractEndpointFromPath(path);
            logger.debug("endpoint = " + endpoint);

            Controller<?, ?> currentController = versionedControllerMap.getOrDefault(version, versionedControllerMap.get(FULL_STABLE_VERSION))
                    .get(endpoint);

            if (isControllerNotAccessible(req, resp, currentController)) {
                return;
            }

            ControllerResponse<?> response;
            if (currentController instanceof DeletableController<?, ?> deletableController) {

                response = deletableController.processDelete(extractIdFromPath(path), serviceFactory);
                logger.debug(response);
            } else {
                writeError(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, METHOD_NOT_SUPPORTED);
                return;
            }

            if (currentController instanceof AuthenticatingController) {
                createSession(req, response);
            }

            writeResponse(resp, response);

        } catch (Exception e) {
            logger.error("ERROR: ", e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private boolean isNotAuthorized(HttpServletRequest req) {

        HttpSession session = req.getSession(false);
        return session == null || session.getAttribute("userId") == null;
    }

    private void createSession(HttpServletRequest req, ControllerResponse<?> response) {
        if (response.isSuccess() && response.getResult() instanceof UserReadDto userReadDto) {
            HttpSession session;
            session = req.getSession(true);
            session.setAttribute("userId", userReadDto.getId());
            logger.debug("session created");
            logger.debug(session);
        }
    }

    private void writeResponse(HttpServletResponse resp, ControllerResponse<?> response) throws IOException {
        resp.setContentType(response.getType());
        resp.setStatus(response.getStatus());
        resp.getWriter().write(objectMapper.writeValueAsString(response));
    }

    private void writeError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setStatus(statusCode);
        resp.sendError(statusCode, message);
    }

    private String trimPath(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    private Map<String, Class<?>> getDtoFieldNames(Class<?> dtoClass) {
        Map<String, Class<?>> dtoFields = new HashMap<>();

        System.out.println("getDtoClass = " + dtoClass);
        Field[] allFields = dtoClass.getDeclaredFields();
        System.out.println("getFields = " + Arrays.toString(allFields));
        for (Field field : allFields) {
            field.setAccessible(true); // make private fields accessible
            System.out.println(field.getName() + " " + field.getType());
            dtoFields.put(field.getName(), field.getType());
        }

        if (dtoFields.isEmpty()) {
            throw new RuntimeException("Имена полей не найдены в DTO");
        }
        return dtoFields;
    }

    private GetRequest parseQueryParameters(Map<String, String[]> queryParams, Map<String, Class<?>> dtoFields) {

//        System.out.println("queryParams = " + queryParams);
//        for (Map.Entry<String, String[]> entry : queryParams.entrySet()) {
//            System.out.println(entry.getKey() + " = " + Arrays.toString(entry.getValue()));
//        }

//        Map<String, List<String>> filterBy = new HashMap<>();
        List<RequestOperation> filterBy = new ArrayList<>();
        Map<String, Boolean> sortBy = null;
        Integer page = null;
        Integer size = null;


        for (String parameter : queryParams.keySet()) {
            String[] values = queryParams.get(parameter);
            switch (parameter) {
                case GetRequest.PAGE_PARAM -> {
                    try {
                        page = Integer.parseInt(values[0]);
                    } catch (NumberFormatException nfe) {
                        page = 0;
                    }
                }
                case GetRequest.SIZE_PARAM -> {
                    try {
                        size = Integer.parseInt(values[0]);
                    } catch (NumberFormatException nfe) {
                        size = 10;
                    }
                }
                case GetRequest.SORT_PARAM -> {
                    sortBy = new HashMap<>();
                    for (String value : values) {
                        String[] split = value.split(",");
                        if (split.length == 2) {
                            sortBy.put(split[0], split[1].equalsIgnoreCase("asc"));
                        }
                    }

                }
                default -> {
                    String[] params = parameter.split("\\|");

                    RequestOperation requestOperation = new RequestOperation();
                    requestOperation.setOrOperation(params.length > 1 || values.length > 1);
                    requestOperation.setConditionList(new ArrayList<>());

                    System.out.println("parameter = " + parameter);

                    for (String param : params) {
                        System.out.println("param = " + param);
                        if (!dtoFields.containsKey(param)) {
                            throw new RuntimeException("Invalid parameter name in query");
                        }
//                    filterBy.put(parameter, List.of(values));
//                        tmpMap.put(param, List.of(values));

                        for (String value : values) {
                            System.out.println("    value = " + value);

//                    filterBy.put(parameter, List.of(values));
//                            tmpMap.put(param, List.of(values));

                            String[] valueSplit = value.split(",");
                            if (valueSplit.length < 2) {
                                throw new RuntimeException("Invalid condition");
                            }

                            if (!isValidValue(valueSplit[0], dtoFields.get(param))) {
                                throw new RuntimeException("Invalid value type");
                            }

                            RequestCondition requestCondition = new RequestCondition(param, valueSplit[0], dtoFields.get(param),
                                    ConditionOperator.fromString(valueSplit[1]));

                            requestOperation.getConditionList().add(requestCondition);

//                            Map<String, List<String>> tmpMap = new HashMap<>();
//                            requestCondition.setParameterValues(tmpMap);
                        }
                    }

                    filterBy.add(requestOperation);

                }
            }
        }

//        System.out.println("filterBy:");
//        for (Map.Entry<String, Object> entry : filterBy.entrySet()) {
//            System.out.println(entry.getKey() + " = " + entry.getValue());
//        }
//
//        if (sortBy != null) {
//            System.out.println("sortBy:");
//            for (Map.Entry<String, Boolean> entry : sortBy.entrySet()) {
//                System.out.println(entry.getKey() + " = " + entry.getValue());
//            }
//        }

        GetRequest request = new GetRequest();

        request.setPage(page);
        request.setSize(size);
        request.setFilterBy(filterBy);
        request.setSortBy(sortBy);

        System.out.println("request = " + request);

//        queryParams.getOrDefault(PaginatedRequestParameters.PAGE.name(), null);

//        request.setPage();


        return request;
    }

    private static final Map<Class<?>, Function<String, ?>> typeValidationParsers = Map.of(
            String.class, s -> s,
            Integer.class, Integer::parseInt,
            int.class, Integer::parseInt,
            Long.class, Long::parseLong,
            long.class, Long::parseLong,
            Boolean.class, Boolean::parseBoolean,
            boolean.class, Boolean::parseBoolean,
            Double.class, Double::parseDouble,
            double.class, Double::parseDouble
    );

    private boolean isValidValue(String value, Class<?> type) {

        Function<String, ?> parser = typeValidationParsers.get(type);

        if (parser == null) {
            return false;
        }

        try {
            parser.apply(value);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private boolean isDetailRequest(String path) {
        return ID_PATH_PATTERN.matcher(path).matches();
    }

    private long extractIdFromSession(HttpServletRequest req) {
        if (isNotAuthorized(req)) {
            throw new RuntimeException("Not authorized");
        } else {
            return (long) req.getSession(false).getAttribute("userId");
        }
    }

    private long extractIdFromPath(String path) throws NumberFormatException {
        return Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
    }

    private String extractEndpointFromPath(String path) {
        Matcher matcher = API_RESOURCE_ENDPOINT_PATTERN.matcher(path);

        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new RuntimeException("NOT FOUND");
    }

    private String extractVersionFromPath(String path) {
        Matcher matcher = API_RESOURCE_VERSION_PATTERN.matcher(path);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return FULL_STABLE_VERSION;
    }

    private boolean checkEndpointHasNoId(String path, String endpoint) {
        return path.substring(path.lastIndexOf(endpoint)).length() == endpoint.length();
    }

    private boolean isControllerNotAccessible(HttpServletRequest req, HttpServletResponse resp, Controller<?, ?> controller) throws IOException {
        if (controller == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "РЕСУРС НЕ НАЙДЕН");
            return true;
        }

        if (isNotAuthorized(req) && !(controller instanceof AuthenticatingController)) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "ДОСТУП ЗАПРЕЩЁН");
            return true;
        }

        return false;
    }
}
