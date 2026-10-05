package mdn.jh.automation.webserver;

import org.json.*;
import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.converter.*;
import mdn.jh.automation.io.logic.components.number.*;
import mdn.jh.automation.io.logic.components.converter.*;
import mdn.jh.automation.io.logic.components.generator.*;
import mdn.jh.automation.io.logic.components.bool.*;
import mdn.jh.automation.corefunctions.*;
import mdn.jh.automation.devices.internalweb.*;
import mdn.jh.automation.devices.mqtt.*;
import mdn.jh.automation.devices.shelly.*;
import mdn.jh.automation.devices.database.DatabaseDataSink;
import mdn.jh.automation.devices.fritz.datasource.FritzDataSource;
import mdn.jh.automation.devices.fritz.datasink.*;
import mdn.jh.automation.devices.xml.datasource.XMLDataSource;
import mdn.jh.automation.devices.json.datasource.JsonDataSource;
import mdn.jh.automation.device.modbus.datasource.ModbusDataSource;
import mdn.jh.automation.device.modbus.datasink.ModbusDataSink;

/** Explicit component settings only: no device credentials, reflection, or actions. */
final class ComponentApiConfiguration {
    private ComponentApiConfiguration() { }
    static JSONObject create(DataComponentStub component) throws JSONException {
        JSONObject c = new JSONObject();
        if (component instanceof StringSelectorDataSource v) c.put("options",v.getOptions()).put("selectedIndex",v.getSelectedIndex()).put("defaultValue",0);
        if (component instanceof mdn.jh.automation.io.logic.components.custom.CustomComponent v) c.put("templateName", v.getTemplateName()).put("internalComponentCount", v.getInternalComponentCount()).put("outputCount", v.getOutputCount());
        if (component instanceof NumericOperation v) c.put("outputType",v.getOutputType());
        if (component instanceof Counter v) c.put("outputType",v.getOutputType()).put("stepSize",v.getStepSize()).put("resetValue",v.getResetValue()).put("counterValue",v.getCounterValue()).put("persistenceEnabled",v.isPersistenceEnabled());
        if (component instanceof NumericThresholdSwitch v) c.put("onOperator",v.getOnOperator()).put("onThreshold",v.getOnThreshold()).put("offOperator",v.getOffOperator()).put("offThreshold",v.getOffThreshold());
        if (component instanceof PidController v) c.put("setpoint",v.getSetpoint()).put("effectiveSetpoint",finite(v.getEffectiveSetpoint())).put("kp",v.getKp()).put("ki",v.getKi()).put("kd",v.getKd()).put("minimum",v.getMinimum()).put("maximum",v.getMaximum()).put("outputType",v.getOutputType()).put("updateInterval",v.getUpdateInterval()).put("updateIntervalUnit",v.getUpdateIntervalUnit());
        if (component instanceof BooleanValueConverter v) c.put("outputType",v.getOutputType()).put("trueValue",v.getTrueValue()).put("falseValue",v.getFalseValue());
        if (component instanceof IntegerBooleanConverter v) c.put("comparison",v.getComparison()).put("comparisonValue",v.getComparisonValue());
        if (component instanceof StringNumberConverter v) c.put("outputType",v.getOutputType()).put("failureValue",v.getFailureValue());
        if (component instanceof StringListConverter v) {
            JSONArray mappings = new JSONArray();
            for (var mapping : v.getMappings()) mappings.put(new JSONObject().put("input",mapping.input()).put("regex",mapping.regex()).put("output",mapping.output()));
            c.put("defaultValue",v.getDefaultValue()).put("mappings",mappings);
        }
        if (component instanceof BinaryConverter) c.put("bitsPerRow",8).put("negativeRepresentation","64-bit two's complement");
        if (component instanceof SineGenerator v) c.put("frequency",v.getFrequency()).put("peak",v.getPeak()).put("enabled",v.isEnabled()).put("manuallyEnabled",v.isManuallyEnabled());
        if (component instanceof RectangleGenerator v) c.put("outputType",v.getOutputType()).put("onMillis",v.getOnMillis()).put("offMillis",v.getOffMillis()).put("onValue",v.getOnValue()).put("offValue",v.getOffValue()).put("enabled",v.isEnabled()).put("manuallyEnabled",v.isManuallyEnabled());
        if (component instanceof RandomNumberGenerator v) c.put("outputType",v.getOutputType()).put("minimum",v.getMinimum()).put("maximum",v.getMaximum()).put("intervalMillis",v.getIntervalMillis()).put("enabled",v.isEnabled()).put("manuallyEnabled",v.isManuallyEnabled());
        if (component instanceof RandomBooleanTimer v) c.put("minimumOnMillis",v.getMinimumOnMillis()).put("maximumOnMillis",v.getMaximumOnMillis()).put("minimumOffMillis",v.getMinimumOffMillis()).put("maximumOffMillis",v.getMaximumOffMillis()).put("enabled",v.isEnabled()).put("manuallyEnabled",v.isManuallyEnabled());
        if (component instanceof BooleanDelay v) c.put("onDelayMillis",v.getOnDelayMillis()).put("offDelayMillis",v.getOffDelayMillis()).put("onDelay",v.getOnDelay()).put("onDelayUnit",v.getOnDelayUnit()).put("offDelay",v.getOffDelay()).put("offDelayUnit",v.getOffDelayUnit());
        if (component instanceof BooleanPulse v) c.put("pulseMillis",v.getPulseMillis()).put("pulseDuration",v.getPulseDuration()).put("pulseUnit",v.getPulseUnit());
        if (component instanceof WeeklyTimerDataSource v) {
            JSONArray schedule = new JSONArray();
            for (var entry : v.getSchedule()) schedule.put(new JSONObject().put("day",entry.day().name()).put("time",entry.time().toString()).put("on",entry.on()));
            c.put("enabled",v.isEnabled()).put("schedule",schedule);
        }
        if (component instanceof CoreDataSource v) c.put("valueType",v.getValueType()).put("configuredValue",v.getValue());
        if (component instanceof WebUiBooleanDataSource v) c.put("mode",v.getMode());
        if (component instanceof WebhookDataSource v) c.put("valueType",v.getValueType()).put("loginRequired",v.isLoginRequired());
        if (component instanceof ApiDataSink v) c.put("valueType",v.getValueType()).put("format",v.getFormat()).put("loginRequired",v.isLoginRequired());
        if (component instanceof MqttDataSource v) c.put("topic",v.getTopic()).put("qos",v.getQos()).put("valueType",v.getValueType());
        if (component instanceof MqttDataSink v) c.put("topic",v.getTopic()).put("qos",v.getQos()).put("valueType",v.getValueType()).put("retained",v.isRetained());
        if (component instanceof ModbusDataSource v) c.put("address",v.getAddress()).put("registerType",v.getTypeAsString());
        if (component instanceof ModbusDataSink v) c.put("address",v.getAddress()).put("registerType",v.getTypeAsString());
        if (component instanceof XMLDataSource v) c.put("path",v.getXpath()).put("converter",converter(v.getDataConverter()));
        if (component instanceof JsonDataSource v) c.put("path",v.getJsonPath()).put("converter",converter(v.getDataConverter()));
        if (component instanceof FritzDataSource v) {
            var selection = v.getMyDataSourceInputParameter();
            if (selection != null) c.put("path",selection.getParameter1()).put("selectionName",selection.getParameter2()).put("validPath",selection.getParameter3());
            c.put("converter",converter(v.getDataConverter())).put("timestampPulseEnabled",v.isTimestampPulseEnabled()).put("timestampPulseMillis",v.getTimestampPulseDurationMillis());
        }
        if (component instanceof FritzDataSink v) c.put("identifier",v.getIdentifierOriginal());
        if (component instanceof FritzAnalogValue v) c.put("mode",v.getMode()).put("writeOnChangeWithTrigger",v.isWriteOnChangeWithTrigger());
        if (component instanceof ShellyDataSource v) c.put("channel",v.getChannel());
        if (component instanceof ShellyDataSink v) c.put("channel",v.getChannel());
        if (component instanceof ShellyCoverDataSink v) c.put("channel",v.getChannel()).put("coverState",v.getCoverState());
        if (component instanceof DatabaseDataSink v) {
            JSONArray columns = new JSONArray(); String[] names = v.getColumnNames(); int[] types = v.getSqlTypes();
            for (int i=0;i<names.length;i++) columns.put(new JSONObject().put("name",names[i]).put("sqlType",types[i]));
            c.put("table",v.getTable()).put("columns",columns).put("schedulerEnabled",v.isSchedulerEnabled()).put("cron",v.getCronExpression()).put("lastWriteDetails",v.getLastWriteDetails());
        }
        return c;
    }
    private static Object finite(double value) { return Double.isFinite(value) ? value : JSONObject.NULL; }
    private static JSONObject converter(Converter value) throws JSONException {
        JSONObject c = new JSONObject();
        if (value instanceof ConvertValueToNumber v) c.put("type","number").put("scaleFactorBefore",v.getScaleFactorBefore()).put("offsetBefore",v.getOffsetBefore()).put("scaleFactorAfter",v.getScaleFactorAfter()).put("offsetAfter",v.getOffsetAfter());
        else if (value instanceof ConvertValueToBoolean v) c.put("type","boolean").put("decisionLimit",v.getDecisionLimit()).put("decisionType",v.getDecisionType());
        else c.put("type",value==null ? "unconfigured" : "string");
        return c;
    }
}
