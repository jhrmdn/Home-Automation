package mdn.jh.automation.webserver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mdn.jh.automation.Main;
import mdn.jh.automation.device.Device;
import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.DataOutputIF;
import mdn.jh.automation.io.logic.LogicBase;
import mdn.jh.automation.io.source.DataSource;
import mdn.jh.automation.security.WebUserStore.User;

/** Read-only HTTP API for retrieving a component output by ID. */
public class OutputApiServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		boolean xml = wantsXml(request);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setHeader("Cache-Control", "no-store");
		if (!mayRead(request)) {
			response.setHeader("WWW-Authenticate", "Basic realm=\"Home Automation API\", charset=\"UTF-8\"");
			writeError(response, xml, HttpServletResponse.SC_UNAUTHORIZED, "Authentication with read access is required");
			return;
		}
		String idText = request.getParameter("id");
		int id;
		try {
			id = Integer.parseInt(idText);
		} catch (Exception e) {
			writeError(response, xml, HttpServletResponse.SC_BAD_REQUEST, "Query parameter 'id' must be an integer");
			return;
		}
		DataComponentStub component = findOutput(id);
		if (!(component instanceof DataOutputIF)) {
			writeError(response, xml, HttpServletResponse.SC_NOT_FOUND, "No output found for ID " + id);
			return;
		}
		DataOutputIF output = (DataOutputIF) component;
		String name = outputName(component);
		String datatype = DataComponentStub.getTypeAsString(output.getDataTypeOutput());
		if (xml) {
			response.setContentType("application/xml");
			response.getWriter().write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><output><id>" + id
					+ "</id><datatype>" + escapeXml(datatype) + "</datatype><value>"
					+ escapeXml(output.getOutputAsString()) + "</value><name>" + escapeXml(name) + "</name></output>");
		} else {
			response.setContentType("application/json");
			response.getWriter().write("{\"id\":" + id + ",\"datatype\":" + jsonString(datatype)
					+ ",\"value\":" + jsonValue(output) + ",\"name\":" + jsonString(name) + "}");
		}
	}

	private boolean mayRead(HttpServletRequest request) {
		if (!Main.isUsersEnabled() || Main.getWebUserStore().isAnonymousViewEnabled()) return true;
		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.regionMatches(true, 0, "Basic ", 0, 6)) return false;
		try {
			String credentials = new String(Base64.getDecoder().decode(authorization.substring(6).trim()), StandardCharsets.UTF_8);
			int separator = credentials.indexOf(':');
			if (separator < 0) return false;
			User user = Main.getWebUserStore().authenticate(credentials.substring(0, separator), credentials.substring(separator + 1));
			return user != null && user.canRead();
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private DataComponentStub findOutput(int id) {
		if (Main.getMySmartHomeHandler() == null) return null;
		for (Device device : Main.getMySmartHomeHandler().getDevices())
			for (DataSource source : device.getDataSourceHandler().getMyDataSources())
				if (source.getId() == id) return source;
		for (LogicBase logic : Main.getMySmartHomeHandler().getDataProcessors())
			if (logic.getId() == id) return logic;
		return null;
	}

	private String jsonValue(DataOutputIF output) {
		if (output.getDataTypeOutput() == DataComponentStub.TYPE_BOOLEAN_IO) return Boolean.toString(output.getOutputAsBoolean());
		if (output.getDataTypeOutput() == DataComponentStub.TYPE_DOUBLE_IO) {
			double value = output.getOutputAsNumber();
			return Double.isFinite(value) ? Double.toString(value) : jsonString(output.getOutputAsString());
		}
		String value = output.getOutputAsString();
		return value == null ? "null" : jsonString(value);
	}

	private String outputName(DataComponentStub component) {
		String name = component.getName();
		if (name != null && !name.trim().isEmpty()) return name;
		if (component instanceof DataSource) {
			String sourceName = ((DataSource) component).getSourceName();
			if (sourceName != null) return sourceName;
		}
		if (component instanceof LogicBase && ((LogicBase) component).getLogicComponentDescription() != null) {
			String logicName = ((LogicBase) component).getLogicComponentDescription().getName();
			if (logicName != null) return logicName;
		}
		return "";
	}

	private boolean wantsXml(HttpServletRequest request) {
		String format = request.getParameter("format");
		if (format != null) return "xml".equalsIgnoreCase(format);
		String accept = request.getHeader("Accept");
		return accept != null && accept.toLowerCase().contains("application/xml");
	}

	private void writeError(HttpServletResponse response, boolean xml, int status, String message) throws IOException {
		response.setStatus(status);
		response.setContentType(xml ? "application/xml" : "application/json");
		response.getWriter().write(xml ? "<?xml version=\"1.0\" encoding=\"UTF-8\"?><error>" + escapeXml(message) + "</error>"
				: "{\"error\":" + jsonString(message) + "}");
	}

	private String jsonString(String value) {
		if (value == null) return "null";
		StringBuilder result = new StringBuilder("\"");
		for (int index = 0; index < value.length(); index++) {
			char character = value.charAt(index);
			switch (character) {
			case '\\': result.append("\\\\"); break;
			case '"': result.append("\\\""); break;
			case '\n': result.append("\\n"); break;
			case '\r': result.append("\\r"); break;
			case '\t': result.append("\\t"); break;
			default:
				if (character < 0x20) result.append(String.format("\\u%04x", (int) character));
				else result.append(character);
			}
		}
		return result.append('"').toString();
	}

	private String escapeXml(String value) {
		if (value == null) return "";
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\"", "&quot;").replace("'", "&apos;");
	}
}
