
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.io.IOException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.server.ServerEndpoint;
import mdn.jh.automation.Main;
import mdn.jh.automation.security.WebUserStore;
import mdn.jh.automation.security.WebUserStore.User;
import mdn.jh.automation.InMemoryLogHandler;
import mdn.jh.automation.SmartHomeHandler;
import mdn.jh.automation.device.Device;
import mdn.jh.automation.io.sink.DataSink;
import mdn.jh.automation.io.logic.DataInputConnection;
import mdn.jh.automation.io.logic.LogicBase;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.logic.components.bool.AND;
import mdn.jh.automation.io.logic.components.bool.INVERT;
import mdn.jh.automation.io.logic.components.bool.OR;
import mdn.jh.automation.io.logic.components.bool.XOR;
import mdn.jh.automation.io.logic.components.bool.RSFlipFlop;
import mdn.jh.automation.io.logic.components.bool.BooleanDelay;
import mdn.jh.automation.io.logic.components.bool.BooleanPulse;
import mdn.jh.automation.io.logic.components.number.Addition;
import mdn.jh.automation.io.logic.components.number.Counter;
import mdn.jh.automation.io.logic.components.number.Difference;
import mdn.jh.automation.io.logic.components.number.Division;
import mdn.jh.automation.io.logic.components.number.Multiplication;
import mdn.jh.automation.io.logic.components.number.NumericOperation;
import mdn.jh.automation.io.logic.components.converter.BooleanValueConverter;
import mdn.jh.automation.io.logic.components.converter.IntegerBooleanConverter;
import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.DataConnectionIF;
import mdn.jh.automation.io.DataInputIF;
import mdn.jh.automation.io.DataOutputIF;
import mdn.jh.automation.io.source.DataSource;
import mdn.jh.automation.device.modbus.ModbusDevice;
import mdn.jh.automation.device.modbus.ModbusServerDevice;
import mdn.jh.automation.device.modbus.datasource.ModbusDataSource;
import mdn.jh.automation.devices.fritz.FritzBoxDevice;
import mdn.jh.automation.devices.fritz.FritzSessionHandler;
import mdn.jh.automation.devices.database.DatabaseDevice;
import mdn.jh.automation.devices.database.DatabaseDataSink;
import mdn.jh.automation.devices.fritz.datasource.FritzDataSource;
import mdn.jh.automation.devices.fritz.datasource.FritzDataSourceHandler;
import mdn.jh.automation.devices.fritz.datasource.FritzDataSourceInputParameter;
import mdn.jh.automation.devices.xml.XmlDevice;
import mdn.jh.automation.devices.xml.datasource.XMLDataSource;
import mdn.jh.automation.io.converter.Converter;
import mdn.jh.automation.io.converter.ConvertValueToBoolean;
import mdn.jh.automation.io.converter.ConvertValueToNumber;
import mdn.jh.automation.io.converter.ConvertValueToString;
import mdn.jh.automation.corefunctions.CoreDataSource;
import mdn.jh.automation.corefunctions.WebUiBooleanDataSource;
import mdn.jh.automation.device.modbus.datasink.ModbusDataSinkCoil;
import mdn.jh.automation.device.modbus.datasink.ModbusDataSinkRegister;
import mdn.jh.automation.devices.fritz.datasink.FritzSimpleOnOff;
import mdn.jh.automation.devices.fritz.datasink.FritzDataSink;
import mdn.jh.automation.devices.shelly.ShellyDevice;
import mdn.jh.automation.devices.shelly.ShellyDataSource;
import mdn.jh.automation.devices.shelly.ShellyDataSink;
import mdn.jh.automation.devices.mqtt.MqttBrokerDevice;
import mdn.jh.automation.devices.mqtt.MqttDataSource;
import mdn.jh.automation.devices.mqtt.MqttDataSink;
import mdn.jh.automation.webserver.DashboardStore;

/**
 *
 * @author jhrib Web Socket connection http://127.0.0.1:8080/home-automation/
 */

@ServerEndpoint("/automation")
public class HomeAutomation {
	private User authenticatedUser;
	private int failedLoginCount;
	private long loginBlockedUntil;

	
	
	/*
	DataProviderSession support is retained below as historical documentation. It
	was never used by the active endpoint implementation.
	
	Vector<DataProviderSession> mySessions = new Vector<DataProviderSession>();

	private DataProviderSession createNewSession() {
		DataProviderSession session = new DataProviderSession();
		mySessions.add(session);
		return session;
	}

	private DataProviderSession getSession(long sessionID) {
		if (sessionID==-1)return null;
		Iterator<DataProviderSession> it = mySessions.iterator();
		DataProviderSession session = null;
		while (it.hasNext()) {
			session = it.next();
			if (session.getSessionID() == sessionID) {
				return session;
			}
		}
		return null;
	}

	*/

	@OnOpen
	public void onOpen() {
		 System.out.println("Open Web Connection Automation ...");
	}

	@OnClose
	public void onClose() {
		 System.out.println("Close Web Connection ...");
	}

	/*
	
	private long getSessionIDFromCommand(String message) {
		String[] s = message.split(":");

		long session = -1;
		if (s.length > 1) {
			try {
				session = Long.valueOf(s[1]);
			} catch (Exception e) {
				return -1;
			}
		}
		
		DataProviderSession dps = getSession(session);
		if (dps == null) {
			return -1;
		}
		return session;
	}
	*/
	
	@OnMessage
	public String onMessage(String message) {
		
	
	//	System.out.println("Message: " + message);
		if (message == null)
			return null;

		if (message.equals("authStatus")) return authStatus();
		if (message.startsWith("authInitialize:")) return initializeAdministrator(message.substring(15));
		if (message.startsWith("authLogin:")) return login(message.substring(10));
		if (message.equals("authLogout")) { authenticatedUser = null; return authStatus(); }
		if (message.startsWith("authAdmin:")) return manageUsers(message.substring(10));
		if (!canView()) return authFailure("Login required", true);
		if (requiresWrite(message) && !canWrite()) return authFailure("Write access required", false);
		if (message.equals("dashboardGet")) return getDashboard(true);
		if (message.equals("dashboardValues")) return getDashboardValues();
		if (message.startsWith("dashboardSave:")) return saveDashboard(message.substring(14));
		
		
		
		
		if (message.startsWith("getDataList:")) {
			Main.getLogger().log(Level.FINEST, "Websocket getDataList - JSON Message:\n" + message);
			
			/*long session = getSessionIDFromCommand(message);
			
			DataProviderSession dps = getSession(session);
			if (dps == null) {
				return "session_invalid";
			}
			*/
			return getListAvailableDataPoints();	
		}
				
		if (message.startsWith("connect:")) {
			return createConnection(message.substring(8));
		}
		if (message.startsWith("disconnect:")) {
			return removeConnection(message.substring(11));
		}
		if (message.startsWith("addLogic:")) {
			return addLogicComponent(message.substring(9));
		}
		if (message.startsWith("addConverter:")) return addConverterComponent(message.substring(13));
		if (message.startsWith("databaseScheduler:")) return toggleDatabaseScheduler(message.substring(18));
		if (message.startsWith("modbusServer:")) return controlModbusServer(message.substring(13));
		if (message.startsWith("mqttBroker:")) return controlMqttBroker(message.substring(11));
		if (message.startsWith("addWebSource:")) return addWebSource(message.substring(13));
		if (message.startsWith("addFixedValue:")) return addFixedValue(message.substring(14));
		if (message.equals("sourceCatalog")) return getSourceCatalog();
		if (message.startsWith("addSource:")) return addDataSource(message.substring(10));
		if (message.startsWith("webSource:")) return operateWebSource(message.substring(10));
		if (message.startsWith("fixedValue:")) return updateFixedValue(message.substring(11));
		if (message.equals("sinkCatalog")) return getSinkCatalog();
		if (message.startsWith("addSink:")) return addDataSink(message.substring(8));
		if (message.startsWith("discoverShelly:")) return discoverShelly(message.substring(15));
		if (message.startsWith("addDevice:")) return addDevice(message.substring(10));
		if (message.equals("deviceCatalog")) return getDeviceCatalog();
		if (message.equals("errorLog")) return getErrorLog();
		if (message.equals("clearErrorLog")) { Main.getInMemoryLogHandler().clear(); return getErrorLog(); }
		if (message.startsWith("deleteDevice:")) return deleteDevice(message.substring(13));
		if (message.startsWith("delete:")) {
			return deleteComponent(message.substring(7));
		}
		if (message.startsWith("details:")) {
			return getComponentDetails(message.substring(8));
		}
		if (message.startsWith("updateDetails:")) return updateComponentDetails(message.substring(14));
		if (message.equals("save")) {
			Main.save();
			return success("Configuration saved");
		}
if (message.startsWith("move:")) {
			updateComponentPosition(message.substring(5));
			return "{\"moved\":true}";
		}
if (message.startsWith("update:")) {
			Main.getLogger().log(Level.FINEST, "Websocket UPDATE Request - JSON Message:\n" + message);

			
			/*
			long session = getSessionIDFromCommand(message);
			DataProviderSession dps = getSession(session);
			if (dps == null) {
				return "session_invalid";
			}
			*/
			String jsonData=getComponentUpdatesAsJSON();
			Main.getLogger().log(Level.FINEST, "Websocket UPDATE - Returning:\n" + jsonData);
			
			return jsonData;
		}
		
		
		
		if (message.startsWith("init_session")) {
			//Create new session
			
			/*
			DataProviderSession session=createNewSession();
			Main.getLogger().log(Level.FINE, "New Websocket Session Created: "+session.getSessionID());
			System.out.println(session.getDataModelAsJSON());
			*/
			return getComponentModelAsJSON();
			//return "new_session:" + session;
		}
		
		if (message.startsWith("init_data:")) {
			Main.getLogger().log(Level.FINEST, "Websocket UPDATE Request - JSON Message:\n" + message);
			/*
			long session = getSessionIDFromCommand(message);
			DataProviderSession dps = getSession(session);
			if (dps == null) {
				return "session_invalid";
			}
			*/
				
			String jsonData=getComponentModelAsJSON();
		
			//Main.getLogger().log(Level.FINEST, "Websocket UPDATE - Returning:\n" + jsonData);
			return jsonData;
		}
		
		
		return null;
	

	}

	private boolean canView() {
		refreshAuthenticatedUser();
		return !Main.isUsersEnabled() || (authenticatedUser != null && authenticatedUser.canRead())
				|| Main.getWebUserStore().isAnonymousViewEnabled();
	}

	private boolean canWrite() {
		refreshAuthenticatedUser();
		return !Main.isUsersEnabled() || (authenticatedUser != null && authenticatedUser.canWrite());
	}

	private boolean isAdmin() {
		refreshAuthenticatedUser();
		return Main.isUsersEnabled() && authenticatedUser != null && authenticatedUser.isAdmin();
	}

	private void refreshAuthenticatedUser() {
		if (Main.isUsersEnabled() && authenticatedUser != null)
			authenticatedUser = Main.getWebUserStore().getUser(authenticatedUser.getUsername());
	}

	static boolean requiresWrite(String message) {
		return message.startsWith("connect:") || message.startsWith("disconnect:")
				|| message.startsWith("addLogic:") || message.startsWith("addConverter:")
				|| message.startsWith("databaseScheduler:") || message.startsWith("modbusServer:")
				|| message.startsWith("mqttBroker:") || message.startsWith("addWebSource:")
				|| message.startsWith("addFixedValue:") || message.startsWith("addSource:")
				|| message.startsWith("webSource:") || message.startsWith("fixedValue:")
				|| message.startsWith("addSink:") || message.startsWith("discoverShelly:")
				|| message.startsWith("addDevice:") || message.equals("clearErrorLog")
				|| message.startsWith("deleteDevice:") || message.startsWith("delete:")
				|| message.startsWith("updateDetails:") || message.equals("save") || message.startsWith("move:")
				|| message.startsWith("dashboardSave:");
	}

	private String getDashboard(boolean includeLayout) {
		try {
			JSONObject response = new JSONObject();
			response.put("dashboard", true);
			if (includeLayout) {
				response.put("layout", DashboardStore.load());
				response.put("catalog", getDashboardCatalog());
			}
			response.put("values", getDashboardValueArray());
			response.put("auth", authObject(Main.isUsersEnabled() ? Main.getWebUserStore() : null));
			return response.toString();
		} catch (Exception e) {
			Main.getLogger().log(Level.WARNING, "Unable to read dashboard configuration", e);
			return error("Dashboard configuration cannot be read: " + e.getMessage());
		}
	}

	private String getDashboardValues() {
		try {
			JSONObject response = new JSONObject();
			response.put("dashboardValues", getDashboardValueArray());
			return response.toString();
		} catch (Exception e) {
			return error("Dashboard values cannot be read: " + e.getMessage());
		}
	}

	private String saveDashboard(String payload) {
		try {
			DashboardStore.save(new JSONObject(payload));
			return getDashboard(true);
		} catch (Exception e) {
			Main.getLogger().log(Level.WARNING, "Unable to save dashboard configuration", e);
			return error("Dashboard configuration cannot be saved: " + e.getMessage());
		}
	}

	private JSONArray getDashboardCatalog() throws JSONException {
		JSONArray catalog = new JSONArray();
		for (DataComponentStub component : getOutputComponents()) {
			DataOutputIF output = (DataOutputIF) component;
			JSONObject item = new JSONObject();
			item.put("id", component.getId());
			item.put("name", dashboardComponentName(component));
			item.put("datatype", DataComponentStub.getTypeAsString(output.getDataTypeOutput()));
			if (component instanceof WebUiBooleanDataSource) {
				item.put("webControl", true);
				item.put("controlMode", ((WebUiBooleanDataSource) component).getMode());
			}
			catalog.put(item);
		}
		return catalog;
	}

	private JSONArray getDashboardValueArray() throws JSONException {
		JSONArray values = new JSONArray();
		for (DataComponentStub component : getOutputComponents()) {
			DataOutputIF output = (DataOutputIF) component;
			JSONObject item = new JSONObject();
			item.put("id", component.getId());
			item.put("value", output.getOutputAsString() == null ? "" : output.getOutputAsString());
			item.put("valid", output.isDataValid());
			values.put(item);
		}
		return values;
	}

	private Vector<DataComponentStub> getOutputComponents() {
		Vector<DataComponentStub> result = new Vector<DataComponentStub>();
		SmartHomeHandler handler = Main.getMySmartHomeHandler();
		if (handler == null) return result;
		for (Device device : handler.getDevices())
			for (DataSource source : device.getDataSourceHandler().getMyDataSources()) result.add(source);
		for (LogicBase logic : handler.getDataProcessors()) result.add(logic);
		return result;
	}

	private String dashboardComponentName(DataComponentStub component) {
		String name = component.getName();
		if (name != null && !name.trim().isEmpty()) return name;
		if (component instanceof DataSource) {
			String sourceName = ((DataSource) component).getSourceName();
			if (sourceName != null && !sourceName.trim().isEmpty()) return sourceName;
		}
		if (component instanceof LogicBase && ((LogicBase) component).getLogicComponentDescription() != null)
			return ((LogicBase) component).getLogicComponentDescription().getName();
		return "Component " + component.getId();
	}

	private String authStatus() {
		try {
			WebUserStore store = Main.isUsersEnabled() ? Main.getWebUserStore() : null;
			JSONObject response = new JSONObject();
			response.put("auth", authObject(store));
			if (isAdmin()) response.put("users", userArray(store));
			return response.toString();
		} catch (Exception e) {
			Main.getLogger().log(Level.SEVERE, "Web user configuration cannot be read", e);
			return authFailure("Web user configuration cannot be read", true);
		}
	}

	private JSONObject authObject(WebUserStore store) throws JSONException {
		JSONObject auth = new JSONObject();
		auth.put("enabled", Main.isUsersEnabled());
		auth.put("initialized", !Main.isUsersEnabled() || store.hasUsers());
		auth.put("authenticated", authenticatedUser != null);
		auth.put("username", authenticatedUser == null ? "" : authenticatedUser.getUsername());
		auth.put("canView", canView());
		auth.put("canWrite", canWrite());
		auth.put("admin", isAdmin());
		auth.put("anonymousView", Main.isUsersEnabled() && store.isAnonymousViewEnabled());
		return auth;
	}

	private String initializeAdministrator(String json) {
		if (!Main.isUsersEnabled()) return authFailure("Web users are not enabled", false);
		try {
			JSONObject request = new JSONObject(json);
			WebUserStore store = Main.getWebUserStore();
			store.initializeAdmin(request.optString("username", "admin"), request.getString("password"));
			authenticatedUser = store.authenticate(request.optString("username", "admin"), request.getString("password"));
			return authStatus();
		} catch (Exception e) {
			return authFailure(e.getMessage(), true);
		}
	}

	private String login(String json) {
		if (!Main.isUsersEnabled()) return authStatus();
		long now = System.currentTimeMillis();
		if (now < loginBlockedUntil) return authFailure("Too many login attempts. Please wait a moment.", true);
		try {
			JSONObject request = new JSONObject(json);
			authenticatedUser = Main.getWebUserStore().authenticate(request.optString("username", ""), request.optString("password", ""));
			if (authenticatedUser == null) {
				failedLoginCount++;
				if (failedLoginCount >= 5) { loginBlockedUntil = now + 15_000; failedLoginCount = 0; }
				return authFailure("Invalid username or password", true);
			}
			failedLoginCount = 0;
			loginBlockedUntil = 0;
			return authStatus();
		} catch (JSONException e) {
			return authFailure("Invalid login request", true);
		}
	}

	private String manageUsers(String json) {
		if (!isAdmin()) return authFailure("Administrator access required", false);
		try {
			JSONObject request = new JSONObject(json);
			String action = request.optString("action", "list");
			WebUserStore store = Main.getWebUserStore();
			if ("save".equals(action)) {
				store.putUser(request.getString("username"), request.optString("password", ""),
						request.optBoolean("read", false), request.optBoolean("write", false), request.optBoolean("admin", false));
			} else if ("delete".equals(action)) {
				if (authenticatedUser.getUsername().equals(request.getString("username")))
					throw new IllegalArgumentException("You cannot delete the account used by this session");
				store.deleteUser(request.getString("username"));
			} else if ("anonymousView".equals(action)) {
				store.setAnonymousViewEnabled(request.optBoolean("enabled", false));
			}
			JSONObject response = new JSONObject();
			response.put("auth", authObject(store));
			response.put("users", userArray(store));
			response.put("userManagement", true);
			return response.toString();
		} catch (IOException | JSONException | IllegalArgumentException e) {
			return authFailure(e.getMessage(), false);
		}
	}

	private JSONArray userArray(WebUserStore store) throws JSONException {
		JSONArray users = new JSONArray();
		for (User user : store.listUsers()) {
			JSONObject value = new JSONObject();
			value.put("username", user.getUsername()); value.put("read", user.canRead());
			value.put("write", user.canWrite()); value.put("admin", user.isAdmin()); users.put(value);
		}
		return users;
	}

	private String authFailure(String message, boolean loginRequired) {
		try {
			JSONObject response = new JSONObject();
			response.put("authError", message == null ? "Authentication failed" : message);
			response.put("loginRequired", loginRequired);
			if (Main.isUsersEnabled()) response.put("auth", authObject(Main.getWebUserStore()));
			return response.toString();
		} catch (Exception ignored) {
			return "{\"authError\":\"Authentication failed\"}";
		}
	}

	private String createConnection(String payload) {
		try {
			JSONObject request = new JSONObject(payload);
			DataConnectionIF source = getConnectionSource(request.getInt("sourceId"));
			DataInputIF target = getConnectionTarget(request.getInt("targetId"));
			if (source == null || target == null) return "{\"error\":\"Invalid connection endpoint\"}";
			int input = request.getInt("input");
			if (target.isInputInUse(input)) return "{\"error\":\"Selected input is already in use\"}";
			source.addInputConnection(target, input);
			Main.save();
			return getComponentModelAsJSON();
		} catch (Exception e) {
			Main.getLogger().log(Level.WARNING, "Unable to create web connection", e);
			return "{\"error\":\"" + e.getMessage() + "\"}";
		}
	}

	private DataConnectionIF getConnectionSource(int id) {
		DataComponentStub component = getComponent(id);
		return component instanceof DataConnectionIF ? (DataConnectionIF) component : null;
	}

	private DataInputIF getConnectionTarget(int id) {
		DataComponentStub component = getComponent(id);
		return component instanceof DataInputIF ? (DataInputIF) component : null;
	}
	private void updateComponentPosition(String payload) {
		try {
			JSONObject position = new JSONObject(payload);
			DataComponentStub component = getComponent(position.getInt("id"));
			if (component == null) return;
			Rectangle bounds = component.getBounds();
			int width = bounds == null ? 174 : bounds.width;
			int height = bounds == null ? 150 : bounds.height;
			component.setBounds(new Rectangle(position.getInt("x"), position.getInt("y"), width, height));
			Main.save();
		} catch (Exception e) {
			Main.getLogger().log(Level.WARNING, "Unable to update web component position", e);
		}
	}

	private DataComponentStub getComponent(int id) {
		SmartHomeHandler handler = Main.getMySmartHomeHandler();
		if (handler == null) return null;
		Iterator<Device> devices = handler.getDevices().iterator();
		while (devices.hasNext()) {
			Device device = devices.next();
			Iterator<DataSource> sources = device.getDataSourceHandler().getMyDataSources().iterator();
			while (sources.hasNext()) { DataSource source = sources.next(); if (source.getId() == id) return source; }
			Iterator<DataSink> sinks = device.getDataSinkHandler().getMyDataSinks().iterator();
			while (sinks.hasNext()) { DataSink sink = sinks.next(); if (sink.getId() == id) return sink; }
		}
		Iterator<LogicBase> logicComponents = handler.getDataProcessors().iterator();
		while (logicComponents.hasNext()) { LogicBase logic = logicComponents.next(); if (logic.getId() == id) return logic; }
		return null;
	}
	private String getComponentModelAsJSON() {
		JSONObject result = new JSONObject();
		try { result.put("components", getComponentsAsJSON()); result.put("connections", getConnectionsAsJSON()); } catch (JSONException e) { Main.getLogger().log(Level.WARNING, "Unable to create web component model", e); }
		return result.toString();
	}

	private String success(String message) {
		JSONObject response = new JSONObject();
		try { response.put("success", true); response.put("message", message); }
		catch (JSONException ignored) { }
		return response.toString();
	}

	private String error(String message) {
		JSONObject response = new JSONObject();
		try { response.put("error", message == null ? "Unknown error" : message); }
		catch (JSONException ignored) { }
		return response.toString();
	}

	private String addLogicComponent(String type) {
		LogicBase logic;
		if ("AND".equalsIgnoreCase(type)) logic = new AND();
		else if ("OR".equalsIgnoreCase(type)) logic = new OR();
		else if ("XOR".equalsIgnoreCase(type)) logic = new XOR();
		else if ("RS".equalsIgnoreCase(type) || "RSFLIPFLOP".equalsIgnoreCase(type)) logic = new RSFlipFlop();
		else if ("NOT".equalsIgnoreCase(type) || "INVERT".equalsIgnoreCase(type)) logic = new INVERT();
		else if ("DELAY".equalsIgnoreCase(type) || "BOOLEANDELAY".equalsIgnoreCase(type)) logic = new BooleanDelay();
		else if ("PULSE".equalsIgnoreCase(type) || "BOOLEANPULSE".equalsIgnoreCase(type)) logic = new BooleanPulse();
		else if ("ADD".equalsIgnoreCase(type) || "ADDITION".equalsIgnoreCase(type)) logic = new Addition();
		else if ("SUBTRACT".equalsIgnoreCase(type) || "DIFFERENCE".equalsIgnoreCase(type)) logic = new Difference();
		else if ("MULTIPLY".equalsIgnoreCase(type) || "MULTIPLICATION".equalsIgnoreCase(type)) logic = new Multiplication();
		else if ("DIVIDE".equalsIgnoreCase(type) || "DIVISION".equalsIgnoreCase(type)) logic = new Division();
		else if ("COUNTER".equalsIgnoreCase(type)) logic = new Counter();
		else return error("Unknown logic component: " + type);
		logic.setBounds(new Rectangle(40, 40, 174, 150));
		Main.getMySmartHomeHandler().registerNewDataProcessor(logic);
		Main.save();
		return getComponentModelAsJSON();
	}

	private String addConverterComponent(String payload) {
		try {
			JSONObject request = new JSONObject(payload); String type = request.getString("type"); LogicBase converter;
			if ("booleanValue".equals(type)) {
				BooleanValueConverter component = new BooleanValueConverter();
				component.configure(request.getString("outputType"), request.getString("trueValue"), request.getString("falseValue")); converter = component;
			} else if ("integerBoolean".equals(type)) {
				IntegerBooleanConverter component = new IntegerBooleanConverter();
				component.configure(request.getString("comparison"), request.getLong("value")); converter = component;
			} else return error("Unknown converter type: " + type);
			converter.setName(request.optString("name", "")); converter.setBounds(new Rectangle(40, 40, 190, 160));
			Main.getMySmartHomeHandler().registerNewDataProcessor(converter); Main.save(); return getComponentModelAsJSON();
		} catch (Exception e) { Main.getLogger().log(Level.WARNING, "Unable to create converter", e); return error(e.getMessage()); }
	}

	private String toggleDatabaseScheduler(String idText) {
		try {
			DataComponentStub component = getComponent(Integer.parseInt(idText));
			if (!(component instanceof DatabaseDataSink)) return error("Database DataSink not found");
			DatabaseDataSink sink = (DatabaseDataSink) component; sink.setSchedulerEnabled(!sink.isSchedulerEnabled()); Main.save(); return getComponentModelAsJSON();
		} catch (Exception e) { Main.getLogger().log(Level.WARNING, "Unable to toggle database scheduler", e); return error(e.getMessage()); }
	}

	private String controlModbusServer(String payload) {
		try {
			JSONObject request = new JSONObject(payload); Device found = getDevice(request.getInt("deviceId"));
			if (!(found instanceof ModbusServerDevice)) return error("Built-in Modbus server not found");
			ModbusServerDevice server = (ModbusServerDevice) found; String action = request.getString("action");
			if ("start".equals(action)) server.startServer();
			else if ("stop".equals(action)) server.stopServer();
			else if ("configure".equals(action)) server.configure(request.getInt("port"), request.getInt("unitId"), request.getBoolean("autostart"));
			else return error("Unknown Modbus server action");
			Main.save(); return getDeviceCatalog();
		} catch (Exception e) { Main.getLogger().log(Level.WARNING, "Unable to control built-in Modbus server", e); return error(e.getMessage()); }
	}
	private String controlMqttBroker(String payload) {
		try { JSONObject request=new JSONObject(payload);Device found=getDevice(request.getInt("deviceId"));if(!(found instanceof MqttBrokerDevice))return error("Built-in MQTT broker not found");MqttBrokerDevice broker=(MqttBrokerDevice)found;String action=request.getString("action");if("start".equals(action))broker.startBroker();else if("stop".equals(action))broker.stopBroker();else if("configure".equals(action))broker.configure(request.getInt("port"),request.getBoolean("autostart"));else return error("Unknown MQTT broker action");Main.save();return getDeviceCatalog();}catch(Exception e){Main.getLogger().log(Level.WARNING,"Unable to control MQTT broker",e);return error(e.getMessage());}
	}

	private String addWebSource(String mode) {
		try {
			Device core = null; for (Device device : Main.getMySmartHomeHandler().getDevices()) if (device.getType() == Device.TYPE_CORE) { core = device; break; }
			if (core == null) return error("Core device is not available");
			WebUiBooleanDataSource source = new WebUiBooleanDataSource(mode);
			source.setName(WebUiBooleanDataSource.MODE_PUSHBUTTON.equals(mode) ? "Pushbutton" : "Switch");
			source.setBounds(new Rectangle(40, 40, 174, 170)); source.setMyDataSourceHandler(core.getDataSourceHandler()); core.getDataSourceHandler().addDataSource(source);
			Main.save(); return getComponentModelAsJSON();
		} catch (Exception e) { Main.getLogger().log(Level.WARNING, "Unable to add Web UI source", e); return error(e.getMessage()); }
	}

	private Device getDevice(int deviceId) {
		SmartHomeHandler handler = Main.getMySmartHomeHandler();
		if (handler == null) return null;
		for (Device device : handler.getDevices()) if (device.getDeviceID() == deviceId) return device;
		return null;
	}

	private String xml(org.w3c.dom.Document document) throws Exception {
		if (document == null) return "";
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(document), new StreamResult(writer));
		return writer.toString();
	}

	private String getSourceCatalog() {
		JSONObject response = new JSONObject();
		JSONArray devices = new JSONArray();
		try {
			for (Device device : Main.getMySmartHomeHandler().getDevices()) {
				JSONObject item = new JSONObject();
				item.put("id", device.getDeviceID()); item.put("name", device.getName()); item.put("type", device.getType());
				try {
					if (device instanceof FritzBoxDevice) item.put("document", xml(((FritzDataSourceHandler) device.getDataSourceHandler()).getFritzDeviceList()));
					else if (device instanceof XmlDevice) item.put("document", xml(((XmlDevice) device).getXMLDocument()));
				} catch (Exception e) { item.put("documentError", e.getMessage()); }
				if (device instanceof ShellyDevice) item.put("shellyInputs", ((ShellyDevice) device).getInputs());
				devices.put(item);
			}
			response.put("sourceCatalog", devices);
		} catch (Exception e) { return error(e.getMessage()); }
		return response.toString();
	}

	private Converter createConverter(JSONObject request) throws Exception {
		String kind = request.optString("converter", "number");
		if ("string".equals(kind)) return new ConvertValueToString();
		if ("boolean".equals(kind)) {
			ConvertValueToBoolean converter = new ConvertValueToBoolean();
			converter.setDecisionLimit(request.optDouble("decisionLimit", 1));
			converter.setDecisionType(request.optInt("decisionType", 1));
			return converter;
		}
		ConvertValueToNumber converter = new ConvertValueToNumber();
		converter.setScaleFactorBefore(request.optDouble("scaleFactorBefore", 1));
		converter.setOffsetBefore(request.optDouble("offsetBefore", 0));
		converter.setScaleFactorAfter(request.optDouble("scaleFactorAfter", 1));
		converter.setOffsetAfter(request.optDouble("offsetAfter", 0));
		return converter;
	}

	private String addDataSource(String payload) {
		try {
			JSONObject request = new JSONObject(payload);
			Device device = getDevice(request.getInt("deviceId"));
			if (device == null) return error("Device not found");
			DataSource source;
			if (device instanceof ModbusDevice) {
				int type = request.getInt("registerType"), address = request.getInt("address");
				if (type < 1 || type > 4 || address < 0) return error("Invalid Modbus register type or address");
				source = new ModbusDataSource(type, address);
			} else if (device instanceof FritzBoxDevice) {
				String path = request.getString("path");
				if (path.trim().isEmpty()) return error("Select a Fritz Box value");
				FritzDataSourceInputParameter parameter = new FritzDataSourceInputParameter();
				parameter.setParameter1(path); parameter.setParameter2(request.optString("label", path)); parameter.setParameter3(request.optString("validPath", ""));
				FritzDataSource fritz = new FritzDataSource(); fritz.setDataConverter(createConverter(request)); fritz.setDataSourceInputParameter(parameter);
				boolean timestampPulse = request.optBoolean("timestampPulse", false);
				long pulseDurationMillis = request.optLong("pulseDurationMillis", 1000);
				if (timestampPulse && (pulseDurationMillis < 1 || pulseDurationMillis > 86400000))
					return error("Pulse duration must be between 1 millisecond and 24 hours");
				fritz.configureTimestampPulse(timestampPulse, pulseDurationMillis); source = fritz;
			} else if (device instanceof XmlDevice) {
				String path = request.getString("path");
				if (path.trim().isEmpty()) return error("Select an XML value");
				XMLDataSource xmlSource = new XMLDataSource(); xmlSource.setXpath(path); xmlSource.setDataConverter(createConverter(request)); source = xmlSource;
			} else if (device instanceof ShellyDevice) {
				int channel=request.getInt("channel"); if(!((ShellyDevice)device).isStatefulInput(channel))return error("This Shelly input is stateless or unavailable and cannot be polled as a Boolean source"); source = new ShellyDataSource(channel);
			} else if (device.getType() == Device.TYPE_CORE) {
				if ("webBoolean".equals(request.optString("sourceType"))) source = new WebUiBooleanDataSource(request.optString("mode", WebUiBooleanDataSource.MODE_SWITCH));
				else source = new CoreDataSource(request.optString("valueType", "boolean"), request.getString("value"));
			} else if (device instanceof MqttBrokerDevice) source=new MqttDataSource(request.getString("topic"),request.getInt("qos"),request.getString("valueType"));
			else return error("This device does not support data sources");
			source.setName(request.optString("name", ""));
			source.setBounds(new Rectangle(40, 40, 174, 150));
			source.setMyDataSourceHandler(device.getDataSourceHandler());
			device.getDataSourceHandler().addDataSource(source);
			Main.save();
			return getComponentModelAsJSON();
		} catch (Exception e) {
			Main.getLogger().log(Level.WARNING, "Unable to create web data source", e);
			return error(e.getMessage());
		}
	}

	private String addFixedValue(String payload) {
		try {
			JSONObject request = new JSONObject(payload);
			Device core = null;
			for (Device candidate : Main.getMySmartHomeHandler().getDevices())
				if (candidate.getType() == Device.TYPE_CORE) { core = candidate; break; }
			if (core == null) return error("Core device is not available");
			String valueType = request.getString("valueType");
			if (!"integer".equals(valueType) && !"float".equals(valueType))
				return error("Fixed value type must be integer or float");
			CoreDataSource source = new CoreDataSource(valueType, request.get("value").toString());
			source.setName(request.optString("name", ""));
			source.setBounds(new Rectangle(40, 40, 174, 150));
			source.setMyDataSourceHandler(core.getDataSourceHandler());
			core.getDataSourceHandler().addDataSource(source);
			Main.save();
			return getComponentModelAsJSON();
		} catch (Exception exception) {
			Main.getLogger().log(Level.WARNING, "Unable to create fixed value source", exception);
			return error(exception.getMessage());
		}
	}

	private String operateWebSource(String payload) {
		try {
			JSONObject request = new JSONObject(payload); DataComponentStub component = getComponent(request.getInt("id"));
			if (!(component instanceof WebUiBooleanDataSource)) return error("Web UI Boolean source not found");
			WebUiBooleanDataSource source = (WebUiBooleanDataSource) component; String action = request.getString("action");
			if ("toggle".equals(action)) { source.toggle(); Main.save(); }
			else if ("set".equals(action) && WebUiBooleanDataSource.MODE_PUSHBUTTON.equals(source.getMode())) source.setValue(request.getBoolean("value"));
			else return error("Invalid operation for this Web UI source");
			return getComponentUpdatesAsJSON();
		} catch (Exception e) { Main.getLogger().log(Level.WARNING, "Unable to operate Web UI source", e); return error(e.getMessage()); }
	}

	private String updateFixedValue(String payload) {
		try {
			JSONObject request = new JSONObject(payload);
			DataComponentStub component = getComponent(request.getInt("id"));
			if (!(component instanceof CoreDataSource)) return error("Fixed value DataSource not found");
			CoreDataSource source = (CoreDataSource) component;
			if ("boolean".equals(source.getValueType())) return error("This is not a numeric fixed value");
			source.configure(request.getString("valueType"), request.get("value").toString());
			Main.save();
			return getComponentUpdatesAsJSON();
		} catch (Exception exception) {
			Main.getLogger().log(Level.WARNING, "Unable to update fixed value source", exception);
			return error(exception.getMessage());
		}
	}

	private JSONArray getSinkActions(Device device) throws JSONException {
		JSONArray actions = new JSONArray();
		if (device instanceof FritzBoxDevice) {
			JSONObject action = new JSONObject(); action.put("type", "fritzOnOff"); action.put("name", "Switch on/off"); action.put("selector", "fritzDevice"); actions.put(action);
		} else if (device instanceof ModbusDevice) {
			JSONObject coil = new JSONObject(); coil.put("type", "modbusCoil"); coil.put("name", "Write coil"); coil.put("inputType", "Boolean"); coil.put("address", true); actions.put(coil);
			JSONObject register = new JSONObject(); register.put("type", "modbusHolding"); register.put("name", "Write holding register"); register.put("inputType", "Number"); register.put("address", true); actions.put(register);
		} else if (device instanceof ShellyDevice) {
			JSONObject action = new JSONObject(); action.put("type", "shellySwitch"); action.put("name", "Relay on/off or cover open/close"); action.put("inputType", "Boolean"); action.put("selector", "shellyActor"); actions.put(action);
		} else if (device instanceof DatabaseDevice) {
			JSONObject action = new JSONObject(); action.put("type", "databaseInsert"); action.put("name", "Insert row"); action.put("selector", "databaseTable"); actions.put(action);
		} else if(device instanceof MqttBrokerDevice){JSONObject action=new JSONObject();action.put("type","mqttPublish");action.put("name","Publish MQTT value");action.put("selector","mqttPublish");actions.put(action);
		}
		return actions;
	}

	private JSONArray getDatabaseTables(DatabaseDevice device) throws Exception {
		JSONArray tables = new JSONArray();
		try (Connection connection = device.openConnection()) {
			for (String tableName : device.listTables()) {
				JSONObject table = new JSONObject(); table.put("name", tableName); JSONArray columns = new JSONArray();
				try (ResultSet rs = connection.getMetaData().getColumns(device.getDatabase(), null, tableName, "%")) {
					while (rs.next()) {
						JSONObject column = new JSONObject(); int sqlType = rs.getInt("DATA_TYPE");
						column.put("name", rs.getString("COLUMN_NAME")); column.put("sqlType", sqlType);
						column.put("sqlTypeName", rs.getString("TYPE_NAME")); column.put("inputType", DataComponentStub.getTypeAsString(databaseInputType(sqlType)));
						columns.put(column);
					}
				}
				table.put("columns", columns); tables.put(table);
			}
		}
		return tables;
	}

	private int databaseInputType(int sqlType) {
		if (sqlType == Types.BOOLEAN || sqlType == Types.BIT) return DataComponentStub.TYPE_BOOLEAN_IO;
		switch (sqlType) { case Types.TINYINT: case Types.SMALLINT: case Types.INTEGER: case Types.BIGINT: case Types.FLOAT: case Types.REAL: case Types.DOUBLE: case Types.NUMERIC: case Types.DECIMAL: return DataComponentStub.TYPE_DOUBLE_IO; default: return DataComponentStub.TYPE_STRING_IO; }
	}

	private String getSinkCatalog() {
		JSONObject response = new JSONObject(); JSONArray devices = new JSONArray();
		try {
			for (Device device : Main.getMySmartHomeHandler().getDevices()) {
				JSONObject item = new JSONObject(); item.put("id", device.getDeviceID()); item.put("name", device.getName()); item.put("type", device.getType()); item.put("actions", getSinkActions(device));
				if (device instanceof FritzBoxDevice) {
					try { item.put("document", xml(((FritzDataSourceHandler) device.getDataSourceHandler()).getFritzDeviceList())); }
					catch (Exception e) { item.put("documentError", e.getMessage()); }
				}
				if (device instanceof ShellyDevice) item.put("shellyActors", ((ShellyDevice) device).getActors());
				if (device instanceof DatabaseDevice) {
					try { item.put("databaseTables", getDatabaseTables((DatabaseDevice) device)); }
					catch (Exception e) { item.put("documentError", "Unable to load database tables: " + e.getMessage()); }
				}
				if (item.getJSONArray("actions").length() == 0) item.put("capabilityMessage", "This device currently provides no DataSink / Action implementation.");
				devices.put(item);
			}
			response.put("sinkCatalog", devices);
		} catch (Exception e) { return error(e.getMessage()); }
		return response.toString();
	}

	private String addDataSink(String payload) {
		try {
			JSONObject request = new JSONObject(payload); Device device = getDevice(request.getInt("deviceId"));
			if (device == null) return error("Device not found");
			String type = request.getString("actionType"); DataSink sink;
			if ("fritzOnOff".equals(type) && device instanceof FritzBoxDevice) {
				String identifier = request.getString("identifier"); if (identifier.trim().isEmpty()) return error("Select a Fritz Box device");
				FritzSimpleOnOff action = new FritzSimpleOnOff(); action.setFritzBox((FritzBoxDevice) device); action.setIdentifier(identifier); action.setName(request.optString("targetName", identifier)); sink = action;
			} else if (("modbusCoil".equals(type) || "modbusHolding".equals(type)) && device instanceof ModbusDevice) {
				int address = request.getInt("address"); if (address < 0) return error("Invalid Modbus address");
				if ("modbusCoil".equals(type)) { ModbusDataSinkCoil action = new ModbusDataSinkCoil(); action.setType(ModbusDevice.TYPE_COIL_RW); action.setAddress(address); action.setModbusDevice((ModbusDevice) device); sink = action; }
				else { ModbusDataSinkRegister action = new ModbusDataSinkRegister(); action.setType(ModbusDevice.TYPE_HOLDING_RW); action.setAddress(address); action.setModbusDevice((ModbusDevice) device); sink = action; }
			} else if ("shellySwitch".equals(type) && device instanceof ShellyDevice) {
				sink = new ShellyDataSink((ShellyDevice) device, request.getInt("channel"));
			} else if ("databaseInsert".equals(type) && device instanceof DatabaseDevice) {
				DatabaseDevice databaseDevice = (DatabaseDevice) device; String tableName = request.getString("table");
				if (!databaseDevice.listTables().contains(tableName)) return error("Database table not found");
				java.util.List<String> names = new java.util.ArrayList<String>(); java.util.List<Integer> types = new java.util.ArrayList<Integer>();
				try (Connection connection = databaseDevice.openConnection(); ResultSet rs = connection.getMetaData().getColumns(databaseDevice.getDatabase(), null, tableName, "%")) {
					while (rs.next()) { names.add(rs.getString("COLUMN_NAME")); types.add(rs.getInt("DATA_TYPE")); }
				}
				if (names.isEmpty()) return error("The selected table has no columns");
				InputDefinition[] inputs = new InputDefinition[names.size() + 1]; int[] sqlTypes = new int[names.size()];
				for (int i = 0; i < names.size(); i++) { sqlTypes[i] = types.get(i); inputs[i] = new InputDefinition(names.get(i), databaseInputType(sqlTypes[i])); }
				inputs[names.size()] = new InputDefinition("Write trigger", DataComponentStub.TYPE_BOOLEAN_IO, "A rising edge inserts one row");
				String cron = request.optString("cron", "* * * * *"); boolean schedulerEnabled = request.optBoolean("schedulerEnabled", false);
				new mdn.jh.automation.devices.database.CronSchedule(cron);
				DatabaseDataSink databaseSink = new DatabaseDataSink(inputs);
				databaseSink.configure(databaseDevice, tableName, names.toArray(new String[0]), sqlTypes, cron, schedulerEnabled); sink = databaseSink;
			} else if("mqttPublish".equals(type)&&device instanceof MqttBrokerDevice){sink=new MqttDataSink((MqttBrokerDevice)device,request.getString("topic"),request.getInt("qos"),request.getBoolean("retained"),request.getString("valueType"));
			} else return error("The selected action is not supported by this device");
			sink.setName(request.optString("name", "")); sink.setBounds(new Rectangle(40, 40, 174, 150)); device.getDataSinkHandler().addDataSink(sink); Main.save(); return getComponentModelAsJSON();
		} catch (Exception e) { Main.getLogger().log(Level.WARNING, "Unable to create web data sink", e); return error(e.getMessage()); }
	}

	private String addDevice(String payload) {
		try {
			JSONObject request = new JSONObject(payload); String type = request.getString("deviceType"); Device device;
			if ("fritz".equals(type)) {
				String host = request.getString("host").trim(); if (host.isEmpty()) return error("IP address / hostname is required");
				String tlsMode = request.optString("tlsMode", FritzSessionHandler.TLS_SYSTEM);
				if (!FritzSessionHandler.TLS_SYSTEM.equals(tlsMode) && !FritzSessionHandler.TLS_CERTIFICATE.equals(tlsMode)
						&& !FritzSessionHandler.TLS_INSECURE.equals(tlsMode)) return error("Unknown TLS validation mode");
				String certificate = request.optString("certificate", "");
				if (FritzSessionHandler.TLS_CERTIFICATE.equals(tlsMode) && certificate.isBlank())
					return error("Upload an X.509 certificate when certificate validation is selected");
				FritzBoxDevice fritz = new FritzBoxDevice(host, request.optString("username", ""), request.optString("password", ""), tlsMode, certificate);
				if (!fritz.checkConnectionOK()) return error("Fritz Box connection failed: " + fritz.getConnectionError()); device = fritz;
			} else if ("modbus".equals(type)) {
				String host = request.getString("host").trim(); int port = request.getInt("port"), slaveId = request.getInt("slaveId");
				if (host.isEmpty()) return error("IP address / hostname is required"); if (port < 1 || port > 65535) return error("Port must be between 1 and 65535"); if (slaveId < 0) return error("Device ID must be zero or greater");
				ModbusDevice modbus = new ModbusDevice(); modbus.setConnectionData(host, port, slaveId); if (!modbus.isConnected()) return error("Modbus connection failed. Check host, port, and device ID."); device = modbus;
			} else if ("xml".equals(type)) {
				String location = request.getString("location").trim(); if (location.isEmpty()) return error("URL / file is required");
				boolean file = "file".equals(request.optString("xmlType", "web")); boolean https = !file && "https".equals(request.optString("protocol", "http"));
				device = new XmlDevice(file ? XmlDevice.XML_type_file : XmlDevice.XML_type_webserver, location, https);
			} else if ("shelly".equals(type)) {
				String host=request.getString("host").trim(); if(host.isEmpty())return error("Shelly host is required");
				ShellyDevice shelly=new ShellyDevice(host,request.optString("username","admin"),request.optString("password",""));
				JSONArray selectedInputs=request.optJSONArray("inputs"), selectedOutputs=request.optJSONArray("outputs");
				if(selectedInputs!=null)for(int i=0;i<selectedInputs.length();i++){int channel=selectedInputs.getInt(i);if(!shelly.isStatefulInput(channel))return error("Shelly input "+channel+" is unavailable or cannot be polled");}
				if(selectedOutputs!=null)for(int i=0;i<selectedOutputs.length();i++){int channel=selectedOutputs.getInt(i);if(!shelly.hasActor(channel))return error("Shelly output "+channel+" is unavailable");}
				device=shelly;
			} else if ("database".equals(type)) {
				String host = request.getString("host").trim();
				String database = request.getString("database").trim();
				int port = request.getInt("port");
				if (host.isEmpty()) return error("Database host is required");
				if (database.isEmpty()) return error("Database name is required");
				if (port < 1 || port > 65535) return error("Port must be between 1 and 65535");
				DatabaseDevice databaseDevice = new DatabaseDevice();
				databaseDevice.setConnectionData(host, port, request.optString("username", ""), request.optString("password", ""), database);
				try (java.sql.Connection ignored = databaseDevice.openConnection()) { /* Validate before registering. */ }
				device = databaseDevice;
			} else return error("Unknown device type: " + type);
			Main.getMySmartHomeHandler().registerDevice(device);
			if(device instanceof ShellyDevice){ShellyDevice shelly=(ShellyDevice)device;JSONArray selectedInputs=request.optJSONArray("inputs"),selectedOutputs=request.optJSONArray("outputs");if(selectedInputs!=null)for(int i=0;i<selectedInputs.length();i++){int channel=selectedInputs.getInt(i);ShellyDataSource source=new ShellyDataSource(channel);source.setName(capabilityName(shelly.getInputs(),channel,"Shelly input "+channel));source.setBounds(new Rectangle(40+i*20,40+i*20,174,150));source.setMyDataSourceHandler(shelly.getDataSourceHandler());shelly.getDataSourceHandler().addDataSource(source);}if(selectedOutputs!=null)for(int i=0;i<selectedOutputs.length();i++){int channel=selectedOutputs.getInt(i);ShellyDataSink sink=new ShellyDataSink(shelly,channel);sink.setName(capabilityName(shelly.getActors(),channel,"Shelly output "+channel));sink.setBounds(new Rectangle(260+i*20,40+i*20,174,150));shelly.getDataSinkHandler().addDataSink(sink);}}
			device.startUpdateThreads(); Main.save();
			JSONObject response = new JSONObject(); response.put("deviceCreated", true); response.put("message", "Device created: " + device.getName()); response.put("deviceId", device.getDeviceID()); return response.toString();
		} catch (Exception e) { Main.getLogger().log(Level.WARNING, "Unable to create web device", e); return error(e.getMessage()); }
	}

	private String getDeviceCatalog() {
		JSONObject response = new JSONObject();
		JSONArray devices = new JSONArray();
		try {
			for (Device device : Main.getMySmartHomeHandler().getDevices()) {
				JSONObject item = new JSONObject();
				item.put("id", device.getDeviceID());
				item.put("name", device.getName());
				item.put("type", device.getType());
				item.put("deletable", device.getType() != Device.TYPE_CORE && device.getType() != Device.TYPE_MODBUS_SERVER && device.getType()!=Device.TYPE_MQTT_BROKER);
				if (device instanceof ModbusServerDevice) { ModbusServerDevice server=(ModbusServerDevice)device;item.put("modbusServer",true);item.put("running",server.isServerRunning());item.put("port",server.getPort());item.put("unitId",server.getUnitId());item.put("autostart",server.isAutostart());item.put("serverError",server.getServerError()); }
				if(device instanceof MqttBrokerDevice){MqttBrokerDevice broker=(MqttBrokerDevice)device;item.put("mqttBroker",true);item.put("running",broker.isBrokerRunning());item.put("port",broker.getPort());item.put("autostart",broker.isAutostart());item.put("serverError",broker.getBrokerError());}
				if (device instanceof FritzBoxDevice) {
					FritzBoxDevice fritz = (FritzBoxDevice) device;
					item.put("fritz", true);
					item.put("connected", fritz.isConnected());
					item.put("connectionError", fritz.getConnectionError());
					item.put("lastSuccessfulRequest", fritz.getLastSuccessfulRequest());
					item.put("tlsMode", fritz.getTlsMode());
				}
				devices.put(item);
			}
			response.put("deviceCatalog", devices);
		} catch (JSONException e) {
			return error(e.getMessage());
		}
		return response.toString();
	}

	private String getErrorLog() {
		try {
			JSONObject response = new JSONObject();
			JSONArray entries = new JSONArray();
			for (InMemoryLogHandler.Entry entry : Main.getInMemoryLogHandler().snapshot()) {
				JSONObject item = new JSONObject();
				item.put("timestamp", entry.getTimestamp().toString());
				item.put("level", entry.getLevel());
				item.put("message", entry.getMessage());
				entries.put(item);
			}
			response.put("errorLog", entries);
			return response.toString();
		} catch (JSONException error) {
			return error(error.getMessage());
		}
	}

	private String deleteDevice(String idText) {
		try {
			int deviceID = Integer.parseInt(idText);
			Device device = getDevice(deviceID);
			if (device == null) return error("Device not found");
			if (device.getType() == Device.TYPE_CORE) return error("The core device cannot be deleted");
			if (device.getType() == Device.TYPE_MODBUS_SERVER) return error("The built-in Modbus server cannot be deleted");
			if(device.getType()==Device.TYPE_MQTT_BROKER)return error("The built-in MQTT broker cannot be deleted");

			for (DataSource source : new Vector<DataSource>(device.getDataSourceHandler().getMyDataSources())) {
				removeConnectionsTo(source.getId(), -1);
				removeAllOutputs(source);
			}
			for (DataSink sink : new Vector<DataSink>(device.getDataSinkHandler().getMyDataSinks()))
				removeConnectionsTo(sink.getId(), -1);

			String name = device.getName();
			if (!Main.getMySmartHomeHandler().removeDevice(deviceID)) return error("Device could not be deleted");
			Main.save();

			JSONObject response = new JSONObject(getComponentModelAsJSON());
			response.put("deviceDeleted", true);
			response.put("message", "Device deleted: " + name);
			return response.toString();
		} catch (NumberFormatException e) {
			return error("Invalid device ID");
		} catch (Exception e) {
			Main.getLogger().log(Level.WARNING, "Unable to delete device", e);
			return error(e.getMessage());
		}
	}

	private String discoverShelly(String payload) {
		try {
			JSONObject request=new JSONObject(payload);String host=request.getString("host").trim();if(host.isEmpty())return error("Shelly host is required");
			ShellyDevice device=new ShellyDevice(host,request.optString("username","admin"),request.optString("password",""));
			JSONObject response=new JSONObject();response.put("shellyDiscovered",true);response.put("name",device.getName());response.put("generation",device.getGeneration());response.put("inputs",device.getInputs());response.put("outputs",device.getActors());return response.toString();
		} catch(Exception e){Main.getLogger().log(Level.WARNING,"Unable to discover Shelly device",e);return error(e.getMessage());}
	}

	private String capabilityName(JSONArray capabilities,int id,String fallback) {
		for(int i=0;i<capabilities.length();i++){JSONObject capability=capabilities.optJSONObject(i);if(capability!=null&&capability.optInt("id",-1)==id)return capability.optString("name",fallback);}
		return fallback;
	}

	private String deleteComponent(String idText) {
		try {
			int id = Integer.parseInt(idText);
			DataComponentStub component = getComponent(id);
			if (component == null) return error("Component not found");
			removeConnectionsTo(id, -1);
			if (component instanceof DataConnectionIF) removeAllOutputs((DataConnectionIF) component);
			Main.getMySmartHomeHandler().removeUnit(id);
			Main.save();
			return getComponentModelAsJSON();
		} catch (Exception e) {
			Main.getLogger().log(Level.WARNING, "Unable to delete web component", e);
			return error(e.getMessage());
		}
	}

	private String removeConnection(String payload) {
		try {
			JSONObject request = new JSONObject(payload);
			int targetId = request.getInt("targetId");
			int input = request.getInt("input");
			if (!removeConnectionsTo(targetId, input)) return error("Connection not found");
			DataInputIF target = getConnectionTarget(targetId);
			if (target != null) target.unlinkInput(input);
			Main.save();
			return getComponentModelAsJSON();
		} catch (Exception e) {
			Main.getLogger().log(Level.WARNING, "Unable to remove web connection", e);
			return error(e.getMessage());
		}
	}

	private boolean removeConnectionsTo(int targetId, int input) {
		boolean removed = false;
		for (DataConnectionIF source : getConnectionSources()) {
			Vector<DataInputConnection> outputs = getOutputs(source);
			if (outputs == null) continue;
			Iterator<DataInputConnection> iterator = outputs.iterator();
			while (iterator.hasNext()) {
				DataInputConnection connection = iterator.next();
				if (connection.getDataInput().getId() == targetId && (input < 0 || connection.getInputConnectionID() == input)) {
					connection.getDataInput().unlinkInput(connection.getInputConnectionID());
					iterator.remove();
					removed = true;
				}
			}
		}
		return removed;
	}

	private void removeAllOutputs(DataConnectionIF source) {
		Vector<DataInputConnection> outputs = getOutputs(source);
		if (outputs == null) return;
		for (DataInputConnection connection : outputs) connection.getDataInput().unlinkInput(connection.getInputConnectionID());
		outputs.clear();
	}

	private Vector<DataConnectionIF> getConnectionSources() {
		Vector<DataConnectionIF> result = new Vector<DataConnectionIF>();
		SmartHomeHandler handler = Main.getMySmartHomeHandler();
		for (Device device : handler.getDevices()) result.addAll(device.getDataSourceHandler().getMyDataSources());
		result.addAll(handler.getDataProcessors());
		return result;
	}

	private Vector<DataInputConnection> getOutputs(DataConnectionIF source) {
		if (source instanceof DataSource) return ((DataSource) source).getMyOutputs();
		if (source instanceof LogicBase) return ((LogicBase) source).getMyOutputs();
		return null;
	}

	private String getComponentDetails(String idText) {
		try {
			DataComponentStub component = getComponent(Integer.parseInt(idText));
			if (component == null) return error("Component not found");
			JSONObject details = new JSONObject();
			details.put("details", true);
			details.put("id", component.getId());
			String componentName = component.getName();
			if ((componentName == null || componentName.trim().isEmpty()) && component instanceof DataSink)
				componentName = ((DataSink) component).getSinkName();
			if ((componentName == null || componentName.trim().isEmpty()) && component instanceof LogicBase) {
				LogicBase logic = (LogicBase) component;
				componentName = logic.getLogicComponentDescription() == null ? logic.getClass().getSimpleName()
						: logic.getLogicComponentDescription().getName();
			}
			details.put("name", componentName == null ? "" : componentName);
			details.put("className", component.getClass().getCanonicalName());
			details.put("help", component.getHelptext() == null ? "" : component.getHelptext());
			details.put("status", component.getStatusMessage() == null ? "" : component.getStatusMessage());
			details.put("valid", component.isDataValid());
			if (component instanceof DataConnectionIF) {
				Vector<DataInputConnection> outputs = getOutputs((DataConnectionIF) component);
				details.put("outputCount", outputs == null ? 0 : outputs.size());
			}
			if (component instanceof DataInputIF) details.put("inputs", createInputs((DataInputIF) component));
			if (component instanceof BooleanDelay) {
				BooleanDelay delay = (BooleanDelay) component;
				details.put("booleanDelay", true);
				details.put("onDelayMillis", delay.getOnDelayMillis());
				details.put("offDelayMillis", delay.getOffDelayMillis());
			}
			if (component instanceof BooleanPulse) {
				BooleanPulse pulse = (BooleanPulse) component;
				details.put("booleanPulse", true);
				details.put("pulseMillis", pulse.getPulseMillis());
			}
			if (component instanceof NumericOperation) {
				NumericOperation operation = (NumericOperation) component;
				details.put("numericOperation", true);
				details.put("outputType", operation.getOutputType());
			}
			if (component instanceof Counter) {
				Counter counter = (Counter) component;
				details.put("counter", true);
				details.put("outputType", counter.getOutputType());
				details.put("stepSize", counter.getStepSize());
				details.put("counterValue", counter.getCounterValue());
				details.put("persistenceEnabled", counter.isPersistenceEnabled());
			}
			if (component instanceof CoreDataSource && !"boolean".equals(((CoreDataSource) component).getValueType())) {
				CoreDataSource fixed = (CoreDataSource) component;
				details.put("fixedNumericValue", true);
				details.put("valueType", fixed.getValueType());
				details.put("fixedValue", fixed.getValue());
			}
			if (component instanceof FritzDataSource) {
				Converter converter = ((FritzDataSource) component).getDataConverter();
				details.put("fritzConverter", true);
				if (converter instanceof ConvertValueToNumber) {
					ConvertValueToNumber number = (ConvertValueToNumber) converter;
					details.put("converterType", "number"); details.put("scaleFactorBefore", number.getScaleFactorBefore());
					details.put("offsetBefore", number.getOffsetBefore()); details.put("scaleFactorAfter", number.getScaleFactorAfter());
					details.put("offsetAfter", number.getOffsetAfter());
				} else if (converter instanceof ConvertValueToBoolean) {
					ConvertValueToBoolean bool = (ConvertValueToBoolean) converter;
					details.put("converterType", "boolean"); details.put("decisionLimit", bool.getDecisionLimit());
					details.put("decisionType", bool.getDecisionType());
				} else details.put("converterType", "string");
			}
			if (component instanceof BooleanValueConverter) {
				BooleanValueConverter converter = (BooleanValueConverter) component; details.put("booleanValueConverter", true);
				details.put("outputType", converter.getOutputType()); details.put("trueValue", converter.getTrueValue()); details.put("falseValue", converter.getFalseValue());
			}
			if (component instanceof IntegerBooleanConverter) {
				IntegerBooleanConverter converter = (IntegerBooleanConverter) component; details.put("integerBooleanConverter", true);
				details.put("comparison", converter.getComparison()); details.put("comparisonValue", converter.getComparisonValue());
			}
			if (component instanceof DatabaseDataSink) {
				DatabaseDataSink sink = (DatabaseDataSink) component; details.put("databaseDataSink", true); details.put("table", sink.getTable());
				details.put("schedulerEnabled", sink.isSchedulerEnabled()); details.put("cron", sink.getCronExpression()); details.put("lastWriteDetails", sink.getLastWriteDetails());
			}
			addFritzSelectionDetails(details, component);
			return details.toString();
		} catch (Exception e) { return error(e.getMessage()); }
	}

	private void addFritzSelectionDetails(JSONObject details, DataComponentStub component) throws Exception {
		Document deviceList = null;
		Element selectedDevice = null;
		String identifier = null;
		if (component instanceof FritzDataSource) {
			FritzDataSource source = (FritzDataSource) component;
			if (source.getMyDataSourceDevice() instanceof FritzDataSourceHandler) {
				FritzDataSourceHandler handler = (FritzDataSourceHandler) source.getMyDataSourceDevice();
				deviceList = handler.getFritzDeviceList();
				FritzDataSourceInputParameter parameter = source.getMyDataSourceInputParameter();
				if (deviceList != null && parameter != null && parameter.getParameter1() != null) {
					Node selectedNode = (Node) XPathFactory.newInstance().newXPath().evaluate(parameter.getParameter1(),
							deviceList, XPathConstants.NODE);
					selectedDevice = findDeviceElement(selectedNode);
				}
			}
		} else if (component instanceof FritzDataSink) {
			FritzDataSink sink = (FritzDataSink) component;
			identifier = sink.getIdentifierOriginal();
			if (sink.getFritzBoxDevice() != null
					&& sink.getFritzBoxDevice().getDataSourceHandler() instanceof FritzDataSourceHandler) {
				deviceList = ((FritzDataSourceHandler) sink.getFritzBoxDevice().getDataSourceHandler()).getFritzDeviceList();
				selectedDevice = findDeviceByIdentifier(deviceList, identifier);
			}
		} else {
			return;
		}
		if (selectedDevice != null) {
			identifier = selectedDevice.getAttribute("identifier");
			details.put("fritzProductName", selectedDevice.getAttribute("productname"));
			details.put("fritzDeviceName", directChildText(selectedDevice, "name"));
		}
		details.put("fritzDevice", true);
		details.put("fritzIdentifier", identifier == null ? "" : identifier);
		if (!details.has("fritzProductName")) details.put("fritzProductName", "");
		if (!details.has("fritzDeviceName")) details.put("fritzDeviceName", "");
	}

	private String directChildText(Element parent, String elementName) {
		NodeList children = parent.getChildNodes();
		for (int index = 0; index < children.getLength(); index++) {
			Node child = children.item(index);
			if (child instanceof Element && elementName.equals(child.getNodeName())) return child.getTextContent();
		}
		return "";
	}

	private Element findDeviceElement(Node node) {
		for (Node current = node; current != null; current = current.getParentNode())
			if (current instanceof Element && "device".equals(current.getNodeName())) return (Element) current;
		return null;
	}

	private Element findDeviceByIdentifier(Document deviceList, String identifier) {
		if (deviceList == null || identifier == null) return null;
		NodeList devices = deviceList.getElementsByTagName("device");
		for (int index = 0; index < devices.getLength(); index++) {
			Element device = (Element) devices.item(index);
			if (identifier.equals(device.getAttribute("identifier"))) return device;
		}
		return null;
	}

	private String updateComponentDetails(String payload) {
		try {
			JSONObject request = new JSONObject(payload);
			DataComponentStub component = getComponent(request.getInt("id"));
			if (component == null) return error("Component not found");
			String name = request.optString("name", "").trim();
			component.setName(name);
			if (component instanceof DataSink) ((DataSink) component).setSinkName(name);
			if (component instanceof BooleanDelay) {
				BooleanDelay delay = (BooleanDelay) component;
				delay.setOnDelayMillis(request.getLong("onDelayMillis"));
				delay.setOffDelayMillis(request.getLong("offDelayMillis"));
			}
			if (component instanceof BooleanPulse)
				((BooleanPulse) component).setPulseMillis(request.getLong("pulseMillis"));
			if (component instanceof NumericOperation)
				((NumericOperation) component).setOutputType(request.getString("outputType"));
			if (component instanceof Counter) {
				Counter counter = (Counter) component;
				counter.setStepSize(request.getDouble("stepSize"));
				counter.setOutputType(request.getString("outputType"));
				counter.setPersistenceEnabled(request.getBoolean("persistenceEnabled"));
			}
			if (component instanceof CoreDataSource && request.has("fixedValue"))
				((CoreDataSource) component).configure(request.getString("valueType"), request.get("fixedValue").toString());
			if (component instanceof FritzDataSource && request.has("converter"))
				((FritzDataSource) component).setDataConverter(createConverter(request));
			if (component instanceof BooleanValueConverter) ((BooleanValueConverter) component).configure(request.getString("outputType"), request.getString("trueValue"), request.getString("falseValue"));
			if (component instanceof IntegerBooleanConverter) ((IntegerBooleanConverter) component).configure(request.getString("comparison"), request.getLong("comparisonValue"));
			if (component instanceof DatabaseDataSink) ((DatabaseDataSink) component).setSchedule(request.getBoolean("schedulerEnabled"), request.getString("cron"));
			Main.save();
			return getComponentModelAsJSON();
		} catch (Exception e) {
			Main.getLogger().log(Level.WARNING, "Unable to update component details", e);
			return error(e.getMessage());
		}
	}

	private JSONArray createInputs(DataInputIF input) throws JSONException {
		JSONArray result = new JSONArray();
		InputDefinition[] definitions = input.getInputDefinitions();
		if (definitions == null) return result;
		for (int i = 0; i < definitions.length; i++) {
			JSONObject item = new JSONObject();
			item.put("name", definitions[i].getName());
			item.put("type", DataComponentStub.getTypeAsString(definitions[i].getDataType()));
			item.put("used", input.isInputInUse(i));
			result.put(item);
		}
		return result;
	}

	private String getComponentUpdatesAsJSON() {
		JSONObject result = new JSONObject();
		try { result.put("componentUpdates", getComponentsAsJSON()); } catch (JSONException e) { Main.getLogger().log(Level.WARNING, "Unable to create web component update", e); }
		return result.toString();
	}

	private JSONArray getComponentsAsJSON() throws JSONException {
		JSONArray components = new JSONArray();
		SmartHomeHandler handler = Main.getMySmartHomeHandler();
		if (handler == null) return components;
		Iterator<Device> devices = handler.getDevices().iterator();
		while (devices.hasNext()) {
			Device device = devices.next();
			Iterator<DataSource> sources = device.getDataSourceHandler().getMyDataSources().iterator();
			while (sources.hasNext()) { DataSource source = sources.next(); String sourceName=source.getName()==null||source.getName().trim().isEmpty()?source.getSourceName():source.getName();JSONObject component=createComponent("source", source.getId(), sourceName, "Data Source (" + source.getDataTypeOutputAsString() + ")", source.getOutputAsString(), source.isDataValid(), source.getStatusMessage(), source);if(source instanceof WebUiBooleanDataSource){component.put("webControl",true);component.put("controlMode",((WebUiBooleanDataSource)source).getMode());}if(source instanceof CoreDataSource&&!"boolean".equals(((CoreDataSource)source).getValueType())){component.put("fixedNumericValue",true);component.put("valueType",((CoreDataSource)source).getValueType());}components.put(component); }
			Iterator<DataSink> sinks = device.getDataSinkHandler().getMyDataSinks().iterator();
			while (sinks.hasNext()) { DataSink sink = sinks.next(); JSONObject component=createComponent("sink", sink.getId(), sink.getSinkName(), "Data Sink", sink.getOutputDataValue().getOutputAsString(), sink.isDataValid(), sink.getStatusMessage(), sink);if(sink instanceof DatabaseDataSink){component.put("databaseScheduler",true);component.put("schedulerEnabled",((DatabaseDataSink)sink).isSchedulerEnabled());}components.put(component); }
		}
		Iterator<LogicBase> logicComponents = handler.getDataProcessors().iterator();
		while (logicComponents.hasNext()) { LogicBase logic = logicComponents.next(); String name = logic.getName(); if(name==null||name.trim().isEmpty()) name = logic.getLogicComponentDescription() == null ? logic.getClass().getSimpleName() : logic.getLogicComponentDescription().getName(); components.put(createComponent("logic", logic.getId(), name, "Logic (" + logic.getDataTypeOutputAsString() + ")", logic.getOutputAsString(), logic.isDataValid(), logic.getStatusMessage(), logic)); }
		return components;
	}
	private JSONArray getConnectionsAsJSON() throws JSONException {
		JSONArray connections = new JSONArray();
		for (DataConnectionIF source : getConnectionSources()) {
			Vector<DataInputConnection> sourceOutputs = getOutputs(source);
			if (sourceOutputs != null) {
				Iterator<DataInputConnection> outputs = sourceOutputs.iterator();
				while (outputs.hasNext()) {
					DataInputConnection output = outputs.next();
					{
						JSONObject connection = new JSONObject();
						connection.put("sourceId", ((DataComponentStub) source).getId());
						connection.put("targetId", output.getDataInput().getId());
						connection.put("input", output.getInputConnectionID());
						if (source instanceof DataOutputIF) {
							DataOutputIF value = (DataOutputIF) source;
							connection.put("booleanActive", value.getDataTypeOutput() == DataComponentStub.TYPE_BOOLEAN_IO && value.isDataValid() && value.getOutputAsBoolean());
						}
						connections.put(connection);
					}
				}
			}
		}
		return connections;
	}
	private JSONObject createComponent(String kind, int id, String name, String type, String value, boolean valid, String status, DataComponentStub model) throws JSONException {
		JSONObject component = new JSONObject();
		component.put("kind", kind); component.put("id", id); component.put("name", name == null ? "" : name); component.put("type", type); component.put("value", value == null ? "" : value); component.put("valid", valid); component.put("status", status == null ? "" : status); addInputMetadata(component, model); Rectangle bounds = model.getBounds(); component.put("x", bounds == null ? -1 : bounds.x); component.put("y", bounds == null ? -1 : bounds.y); component.put("width", bounds == null ? 174 : bounds.width); component.put("height", bounds == null ? 150 : bounds.height);
		return component;
	}
	private int getSourceIdForInput(DataInputIF target, int targetInput) {
		SmartHomeHandler handler = Main.getMySmartHomeHandler();
		if (handler == null) return -1;
		Iterator<Device> devices = handler.getDevices().iterator();
		while (devices.hasNext()) { Iterator<DataSource> sources = devices.next().getDataSourceHandler().getMyDataSources().iterator(); while (sources.hasNext()) { DataSource source = sources.next(); Iterator<DataInputConnection> outputs = source.getMyOutputs().iterator(); while (outputs.hasNext()) { DataInputConnection output = outputs.next(); if (output.getDataInput() == target && output.getInputConnectionID() == targetInput) return source.getId(); } } }
		Iterator<LogicBase> logicComponents = handler.getDataProcessors().iterator();
		while (logicComponents.hasNext()) { LogicBase logic = logicComponents.next(); Iterator<DataInputConnection> outputs = logic.getMyOutputs().iterator(); while (outputs.hasNext()) { DataInputConnection output = outputs.next(); if (output.getDataInput() == target && output.getInputConnectionID() == targetInput) return logic.getId(); } }
		return -1;
	}
	private void addInputMetadata(JSONObject component, DataComponentStub model) throws JSONException {
		if (!(model instanceof DataInputIF)) return;
		DataInputIF input = (DataInputIF) model;
		JSONArray inputs = new JSONArray();
		InputDefinition[] definitions = input.getInputDefinitions();
		if (definitions != null) {
			for (int index = 0; index < definitions.length; index++) {
				JSONObject definition = new JSONObject();
				definition.put("id", index);
				definition.put("name", definitions[index].getName());
				definition.put("type", DataComponentStub.getTypeAsString(definitions[index].getDataType()));
				boolean used = input.isInputInUse(index);
				definition.put("used", used);
				if (used) definition.put("sourceId", getSourceIdForInput(input, index));
				inputs.put(definition);
			}
		}
		component.put("inputs", inputs);
	}
	@OnError
	public void onError(Throwable error) {
		if (isConnectionReset(error)) {
			Main.getLogger().log(Level.FINE, "WebSocket connection was reset by the client");
			return;
		}
		Main.getLogger().log(Level.WARNING, "Unexpected WebSocket error", error);
	}

	static boolean isConnectionReset(Throwable error) {
		for (Throwable cause = error; cause != null; cause = cause.getCause()) {
			String message = cause.getMessage();
			if (message != null && message.toLowerCase(java.util.Locale.ROOT).contains("connection reset")) return true;
		}
		return false;
	}

	/**
	 * 
	 * @return A list of all available Datapoints
	 */
	private String getListAvailableDataPoints() {
		JSONArray arr = new JSONArray();

		
		DataSource d = null;
		SmartHomeHandler handler = Main.getMySmartHomeHandler();
		Vector<DataSource> v = handler.getAllDataOutputInterfaces();
		Iterator<DataSource> it = v.iterator();
		JSONObject jsonObject= null;
		try {
			jsonObject=new JSONObject();
			
			//arr.put(temp);

			JSONObject temp = null;
				
			while (it.hasNext()) {
				temp = new JSONObject();
				d = it.next();
				temp.append("name", d.getName());
				temp.append("type", d.getDataTypeOutput());
				temp.append("dataid", d.getId());
				temp.append("latest", d.getOutputAsString());

				arr.put(temp);
			}
			jsonObject.append("AllDataPointList", arr);
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	//	System.out.println("List Data Points:\n"+jsonObject.toString());
		return jsonObject.toString();
	}
	
}
