package mdn.jh.automation.webserver;

import org.json.*;
import java.util.*;
import mdn.jh.automation.Main;
import mdn.jh.automation.io.logic.LogicBase;
import mdn.jh.automation.io.logic.DataInputConnection;
import mdn.jh.automation.corefunctions.WeeklyTimerDataSource;
import mdn.jh.automation.devices.fritz.datasource.FritzDataSource;
import mdn.jh.automation.io.*;
import mdn.jh.automation.io.logic.TimerTimeFormat;
import mdn.jh.automation.io.logic.components.bool.BooleanDelay;
import mdn.jh.automation.io.logic.components.bool.BooleanPulse;
import mdn.jh.automation.io.logic.components.generator.RandomBooleanTimer;
import mdn.jh.automation.io.logic.components.generator.RandomNumberGenerator;
import mdn.jh.automation.io.sink.DataSink;
import mdn.jh.automation.io.source.DataSource;

/** Read-only snapshot: never runs a component's update, reset, or actions. */
public final class ComponentApiSnapshot {
    private ComponentApiSnapshot() { }

    public static DataOutputIF outputOf(DataComponentStub component) {
        if (component instanceof DataOutputIF) return new ComponentOutputView(component, (DataOutputIF) component);
        if (component instanceof DataSink) return ((DataSink) component).getOutputDataValue();
        return null;
    }

    public static JSONObject create(DataComponentStub component) throws JSONException {
        List<DataComponentStub> components = new ArrayList<>();
        var handler = Main.getMySmartHomeHandler();
        if (handler != null) {
            for (var device : handler.getDevices()) {
                components.addAll(device.getDataSourceHandler().getMyDataSources());
                components.addAll(device.getDataSinkHandler().getMyDataSinks());
            }
            components.addAll(handler.getDataProcessors());
        }
        return create(component, components);
    }

    static JSONObject create(DataComponentStub component, Collection<? extends DataComponentStub> components) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("id", component.getId());
        result.put("name", component.getName() == null ? "" : component.getName());
        result.put("kind", component instanceof DataSource ? "source" : component instanceof DataSink ? "sink" : "logic");
        result.put("componentType", component.getClass().getSimpleName());
        result.put("description", component.getDescription());
        result.put("configuration", ComponentApiConfiguration.create(component));
        result.put("automationPageId", component.getAutomationPageId());
        result.put("api", new JSONObject().put("enabled", component.isReadOnlyApiEnabled()).put("readOnly", true)
            .put("url", "/api/component?id=" + component.getId()));
        var bounds = component.getBounds();
        if (bounds != null) result.put("bounds", new JSONObject().put("x", bounds.x).put("y", bounds.y).put("width", bounds.width).put("height", bounds.height));
        JSONArray ranges = new JSONArray();
        for (var range : component.getValueColorRanges()) ranges.put(new JSONObject().put("minimum", range.minimum()).put("maximum", range.maximum()).put("color", range.color()));
        result.put("colors", new JSONObject().put("true", component.getTrueColor()).put("false", component.getFalseColor()).put("ranges", ranges));
        JSONArray connections = new JSONArray();
        List<DataComponentStub> candidates = new ArrayList<>(components);
        if (!candidates.contains(component)) candidates.add(component);
        for (var source : candidates) for (var connection : connectionsOf(source)) {
            if (source == component || connection.getDataInput().getId() == component.getId())
                connections.put(new JSONObject().put("sourceId", source.getId()).put("outputIndex", connection.getOutputIndex())
                    .put("targetId", connection.getDataInput().getId()).put("inputIndex", connection.getInputConnectionID()));
        }
        result.put("connections", connections);
        result.put("status", component.getStatusMessage() == null ? "" : component.getStatusMessage());
        JSONArray inputs = new JSONArray(), outputs = new JSONArray();
        if (component instanceof DataInputIF) {
            DataInputIF input = (DataInputIF) component;
            var definitions = input.getInputDefinitions();
            if (definitions != null) for (int i = 0; i < definitions.length; i++) {
                DataOutputIF value = input.getInputData(i);
                boolean overridden = component.hasInputOverride(i);
                if (overridden) {
                    DataValue override = new DataValue();
                    String text = component.getInputOverrideValue(i);
                    switch (definitions[i].getDataType()) {
                        case DataComponentStub.TYPE_BOOLEAN_IO: override.setValue(Boolean.parseBoolean(text)); break;
                        case DataComponentStub.TYPE_DOUBLE_IO: override.setValue(Double.parseDouble(text)); break;
                        default: override.setValue(text);
                    }
                    override.setDataValid(true);
                    value = override;
                }
                JSONObject port = port(i, definitions[i].getName(), definitions[i].getDataType(), value);
                port.put("connected", input.isInputConnected(i));
                port.put("overridden", overridden);
                port.put("overridePersistent", overridden && component.isInputOverridePersistent(i));
                inputs.put(port);
            }
        }
        DataOutputIF output = outputOf(component);
        int outputCount = component instanceof DataConnectionIF source ? source.getOutputCount() : output == null ? 0 : 1;
        for (int index = 0; index < outputCount; index++) {
            DataOutputIF value = component instanceof DataConnectionIF source ? source.getOutputPort(index) : output;
            String name = component instanceof DataConnectionIF source ? source.getOutputName(index) : "Result";
            JSONObject port = port(index, name, outputType(value), value);
            port.put("overridden", index == 0 && component instanceof DataOutputIF && component.hasOutputOverride());
            port.put("overridePersistent", index == 0 && component.hasOutputOverride() && component.isOutputOverridePersistent());
            outputs.put(port);
            if (index == 0) { result.put("datatype", port.get("datatype")); result.put("value", port.get("value")); }
        }
        boolean valid;
        try { valid = outputs.length() > 0 ? outputs.getJSONObject(0).getBoolean("valid") : component.isDataValid(); }
        catch (RuntimeException unavailable) { valid = false; }
        result.put("valid", valid);
        result.put("inputs", inputs); result.put("outputs", outputs);
        JSONObject timer = timer(component);
        if (timer != null) result.put("timer", timer);
        if (component instanceof RandomNumberGenerator) {
            RandomNumberGenerator generator = (RandomNumberGenerator) component;
            result.put("generator", new JSONObject().put("type", "randomNumber").put("minimum", generator.getMinimum())
                .put("maximum", generator.getMaximum()).put("outputType", generator.getOutputType())
                .put("enabled", generator.isEnabled()).put("intervalMillis", generator.getIntervalMillis()));
        }
        return result;
    }

    private static List<DataInputConnection> connectionsOf(DataComponentStub component) {
        if (component instanceof DataSource source) return new ArrayList<>(source.getMyOutputs());
        if (component instanceof LogicBase logic) return new ArrayList<>(logic.getMyOutputs());
        return List.of();
    }

    private static int outputType(DataOutputIF output) {
        try { return output.getDataTypeOutput(); }
        catch (RuntimeException unconfigured) { return 0; }
    }

    private static JSONObject port(int index, String name, int type, DataOutputIF data) throws JSONException {
        JSONObject port = new JSONObject().put("index", index).put("name", name == null ? "" : name)
            .put("datatype", DataComponentStub.getTypeAsString(type));
        Object value = JSONObject.NULL;
        boolean valid = false;
        if (data != null) try {
            valid = data.isDataValid();
            switch (type) {
                case DataComponentStub.TYPE_BOOLEAN_IO: value = data.getOutputAsBoolean(); break;
                case DataComponentStub.TYPE_DOUBLE_IO:
                    double number = data.getOutputAsNumber();
                    if (Double.isFinite(number)) value = number; else valid = false;
                    break;
                default: value = data.getOutputAsString(); if (value == null) value = JSONObject.NULL;
            }
        } catch (RuntimeException unavailable) { value = JSONObject.NULL; valid = false; }
        return port.put("value", value).put("valid", valid);
    }

    private static JSONObject timing(String type, long elapsed, long remaining) throws JSONException {
        return new JSONObject().put("type", type).put("elapsedMillis", elapsed).put("remainingMillis", remaining)
            .put("elapsedTime", TimerTimeFormat.format(elapsed)).put("remainingTime", TimerTimeFormat.format(remaining));
    }

    private static JSONObject timer(DataComponentStub component) throws JSONException {
        if (component instanceof BooleanDelay) {
            BooleanDelay timer = (BooleanDelay) component;
            return timing("delay", timer.getElapsedMillis(), timer.getRemainingMillis()).put("settings", new JSONObject()
                .put("onDelayMillis", timer.getOnDelayMillis()).put("offDelayMillis", timer.getOffDelayMillis()));
        }
        if (component instanceof BooleanPulse) {
            BooleanPulse timer = (BooleanPulse) component;
            return timing("pulse", timer.getElapsedMillis(), timer.getRemainingMillis()).put("settings", new JSONObject().put("pulseMillis", timer.getPulseMillis()));
        }
        if (component instanceof RandomBooleanTimer) {
            RandomBooleanTimer timer = (RandomBooleanTimer) component;
            return timing("random", timer.getElapsedMillis(), timer.getRemainingMillis()).put("enabled", timer.isEnabled())
                .put("settings", new JSONObject().put("minimumOnMillis", timer.getMinimumOnMillis()).put("maximumOnMillis", timer.getMaximumOnMillis())
                    .put("minimumOffMillis", timer.getMinimumOffMillis()).put("maximumOffMillis", timer.getMaximumOffMillis()));
        }
        if (component instanceof WeeklyTimerDataSource) {
            WeeklyTimerDataSource timer = (WeeklyTimerDataSource) component;
            JSONArray schedule = new JSONArray();
            for (var entry : timer.getSchedule()) schedule.put(new JSONObject().put("day", entry.day().name()).put("time", entry.time().toString()).put("on", entry.on()));
            return new JSONObject().put("type", "weekly").put("enabled", timer.isEnabled()).put("schedule", schedule);
        }
        if (component instanceof FritzDataSource && ((FritzDataSource) component).isTimestampPulseEnabled())
            return new JSONObject().put("type", "timestampPulse").put("settings", new JSONObject().put("pulseMillis", ((FritzDataSource) component).getTimestampPulseDurationMillis()));
        return null;
    }
}
