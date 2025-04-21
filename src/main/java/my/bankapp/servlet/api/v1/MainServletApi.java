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
import my.bankapp.controller.ReadableController;
import my.bankapp.dto.UserDto;
import my.bankapp.factory.ServiceFactory;
import my.bankapp.model.response.ControllerResponse;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: tests, factory, versioning, pagination

@WebServlet(urlPatterns = "/api/*")
public class MainServletApi extends ApiHttpServlet {

    private ServiceFactory serviceFactory;
    private Logger logger;

//    private Map<String, Controller<?, ?>> controllerMap;
//    private Map<String, Map<Integer, Controller<?, ?>>> controllerMap;
    private Map<String, Map<String, Controller<?, ?>>> versionedControllerMap;
    private ObjectMapper objectMapper;

    private static final String FULL_STABLE_VERSION = "2.0.0";
    private static final String METHOD_NOT_SUPPORTED = "МЕТОД НЕ ПОДДЕРЖИВАЕТСЯ";
    private static final Pattern ID_PATH_PATTERN = Pattern.compile(".*/\\d+/?$");
    private static final Pattern API_RESOURCE_ID_PATTERN = Pattern.compile("^/api/v\\d+/[a-z]+/\\d+$");
    private static final Pattern API_RESOURCE_VERSION_PATTERN = Pattern.compile("^/api/v([\\d.]+)");
    private static final Pattern API_RESOURCE_ENDPOINT_PATTERN = Pattern.compile("^/api/v[\\d.]+(/[^/]+)");

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
        this.logger = serviceFactory.getLogger();
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

            Controller<?, ?> currentController = versionedControllerMap.getOrDefault(version, versionedControllerMap.get(FULL_STABLE_VERSION)).get(endpoint);

            ControllerResponse<?> response;

            if (isControllerNotAccessible(req, resp, currentController)) {
                return;
            }

            if (currentController instanceof ReadableController<?, ?> readableController) {
                if (isGetAll) {
                    response = readableController.processGetAll(extractIdFromSession(req), serviceFactory);
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
            logger.error(e);
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

            Controller<?, ?> currentController = versionedControllerMap.getOrDefault(version, versionedControllerMap.get(FULL_STABLE_VERSION)).get(endpoint);

            if (isControllerNotAccessible(req, resp, currentController)) {
                return;
            }

            ControllerResponse<?> response;
            if (currentController instanceof CreatableController<?, ?> rawCreatableController) {

                Object data = objectMapper.readValue(req.getInputStream(), currentController.getRequestClass());

                @SuppressWarnings("unchecked")
                CreatableController<Object, ?> creatableController =
                        (CreatableController<Object, ?>) rawCreatableController;
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
            logger.error(e);
            writeError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private boolean isNotAuthorized(HttpServletRequest req) {

        HttpSession session = req.getSession(false);
        return session == null || session.getAttribute("userId") == null;
    }

    private void createSession(HttpServletRequest req, ControllerResponse<?> response) {
        if (response.isSuccess() && response.getResult() instanceof UserDto userDto) {
            HttpSession session;
            session = req.getSession(true);
            session.setAttribute("userId", userDto.getId());
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
