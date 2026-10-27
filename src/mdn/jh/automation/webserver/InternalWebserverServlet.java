package mdn.jh.automation.webserver;

import java.io.IOException;
import jakarta.servlet.http.*;
import mdn.jh.automation.Main;
import mdn.jh.automation.device.Device;
import mdn.jh.automation.devices.internalweb.*;
import mdn.jh.automation.io.*;
import mdn.jh.automation.security.WebUserStore;

/** GET /api?id=N reads a sink; GET or form POST with value updates a source. */
public class InternalWebserverServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException { handle(request, response, false); }
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException { handle(request, response, false); }
    @Override protected void doHead(HttpServletRequest request, HttpServletResponse response) throws IOException { handle(request, response, true); }

    protected WebUserStore users() { return Main.getWebUserStore(); }
    protected boolean isApiEnabled() { return InternalWebserverDevice.isWebApiEnabled(); }
    protected DataComponentStub findComponent(int id) {
        if (Main.getMySmartHomeHandler() == null) return null;
        for (Device device : Main.getMySmartHomeHandler().getDevices()) {
            if (!(device instanceof InternalWebserverDevice)) continue;
            for (var source : device.getDataSourceHandler().getMyDataSources()) if (source.getId() == id) return source;
            for (var sink : device.getDataSinkHandler().getMyDataSinks()) if (sink.getId() == id) return sink;
        }
        return null;
    }

    private void handle(HttpServletRequest request, HttpServletResponse response, boolean head) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
        if (!isApiEnabled()) { error(response, 503, "Web API is disabled", head); return; }
        int id;
        try {
            String idText = request.getParameter("id");
            // Also accept /api/id=123?value=... for webhook clients using path IDs.
            if (idText == null && request.getPathInfo() != null && request.getPathInfo().startsWith("/id="))
                idText = request.getPathInfo().substring(4);
            id = Integer.parseInt(idText);
        } catch (Exception e) { error(response, 400, "Parameter 'id' must be an integer", head); return; }
        DataComponentStub component = findComponent(id);
        if (!(component instanceof WebhookDataSource) && !(component instanceof ApiDataSink)) {
            error(response, 404, "API endpoint not found", head); return;
        }
        boolean writing = component instanceof WebhookDataSource;
        boolean requireLogin = writing ? ((WebhookDataSource) component).isLoginRequired() : ((ApiDataSink) component).isLoginRequired();
        if (requireLogin && !ApiAuthentication.allowed(request, users(), writing)) {
            response.setHeader("WWW-Authenticate", "Basic realm=\"Home Automation API\", charset=\"UTF-8\"");
            error(response, 401, writing ? "Login with write access is required" : "Login with read access is required", head);
            return;
        }
        if (writing) {
            if (head) { response.setHeader("Allow", "GET, POST"); error(response, 405, "HEAD cannot update a webhook", true); return; }
            try {
                WebhookDataSource source = (WebhookDataSource) component;
                source.receive(request.getParameter("value"));
                response.setContentType("application/json");
                response.getWriter().write("{\"id\":" + id + ",\"updated\":true}");
            } catch (IllegalArgumentException e) { error(response, 400, e.getMessage(), false); }
            return;
        }
        if (!"GET".equals(request.getMethod()) && !head) {
            response.setHeader("Allow", "GET, HEAD"); error(response, 405, "Use GET to read a sink", false); return;
        }
        if (request.getParameter("value") != null) { error(response, 400, "Sink endpoints are read-only; omit 'value'", head); return; }
        ApiDataSink sink = (ApiDataSink) component;
        DataOutputIF input = sink.getApiValue();
        if (input == null || !input.isDataValid()) { error(response, 503, "Sink has no valid connected value", head); return; }
        String format = sink.getFormat();
        response.setContentType("plain".equals(format) ? "text/plain" : "json".equals(format) ? "application/json" : "application/xml");
        if (head) return;
        String value = input.getOutputAsString();
        if ("plain".equals(format)) response.getWriter().write(value == null ? "" : value);
        else if ("json".equals(format)) response.getWriter().write("{\"id\":" + id + ",\"value\":" + OutputApiServlet.jsonValue(input) + "}");
        else response.getWriter().write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><output><id>" + id + "</id><value>"
            + OutputApiServlet.escapeXml(value) + "</value></output>");
    }

    private void error(HttpServletResponse response, int status, String message, boolean head) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        if (!head) response.getWriter().write("{\"error\":" + OutputApiServlet.jsonString(message) + "}");
    }
}
