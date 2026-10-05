package mdn.jh.automation.io.logic.components.generator;

import java.util.concurrent.*;
import org.w3c.dom.*;
import mdn.jh.automation.io.logic.*;

/** Generates a bounded random value at a configurable interval, holding the value while disabled. */
public class RandomNumberGenerator extends LogicBase {
    private static final long serialVersionUID = 1L;
    private static final double MAX_SAFE_INTEGER = 9_007_199_254_740_991d;
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "random-number-generator"); thread.setDaemon(true); return thread;
    });
    private double minimum = 0, maximum = 100;
    private String outputType = "integer";
    private boolean manuallyEnabled = true;
    private transient ScheduledFuture<?> task;
    private long intervalMillis = 1000;
    private transient long scheduleVersion;

    public RandomNumberGenerator() {
        super(TYPE_DOUBLE_IO, new InputDefinition[]{new InputDefinition("Enable", TYPE_BOOLEAN_IO)}, new LogicComponentDescription("Random Number Generator"));
        setName("Random Number Generator");
        setHelptext("Generates a random number at the selected update interval between Minimum and Maximum. Integer mode includes both limits. Disable to hold the last value; an Enable input takes precedence over the component switch.");
        generate(); start();
    }

    public synchronized void configure(double minimum, double maximum, String outputType) {
        configure(minimum, maximum, outputType, intervalMillis);
    }

    public synchronized void configure(double minimum, double maximum, String outputType, double intervalMillis) {
        if (!Double.isFinite(intervalMillis) || intervalMillis < 1 || intervalMillis > MAX_SAFE_INTEGER || intervalMillis != Math.rint(intervalMillis))
            throw new IllegalArgumentException("Update interval must be a positive whole number of milliseconds");
        if (!Double.isFinite(minimum) || !Double.isFinite(maximum) || minimum > maximum)
            throw new IllegalArgumentException("Minimum and maximum must be finite numbers, with minimum no greater than maximum");
        if (!"integer".equals(outputType) && !"decimal".equals(outputType)) throw new IllegalArgumentException("Choose integer or decimal output");
        if ("integer".equals(outputType) && (minimum != Math.rint(minimum) || maximum != Math.rint(maximum)
                || Math.abs(minimum) > MAX_SAFE_INTEGER || Math.abs(maximum) > MAX_SAFE_INTEGER))
            throw new IllegalArgumentException("Integer limits must be whole numbers between -9007199254740991 and 9007199254740991");
        boolean changed = this.minimum != minimum || this.maximum != maximum || !this.outputType.equals(outputType);
        boolean intervalChanged = this.intervalMillis != (long) intervalMillis;
        this.intervalMillis = (long) intervalMillis;
        if (intervalChanged && task != null) { task.cancel(false); task = null; start(); }
        this.minimum = minimum; this.maximum = maximum; this.outputType = outputType;
        if (changed) { generate(); updateMyOutputs(); }
    }

    private synchronized void start() {
        if (task != null) return;
        long version = ++scheduleVersion;
        task = SCHEDULER.scheduleAtFixedRate(() -> {
            synchronized (this) { if (version == scheduleVersion) tick(); }
        }, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
    }
    synchronized void tick() {
        if (task == null || !isEnabled()) return;
        generate(); updateMyOutputs();
    }
    private void generate() {
        double value;
        if (minimum == maximum) value = minimum;
        else if ("integer".equals(outputType)) value = ThreadLocalRandom.current().nextLong((long) minimum, (long) maximum + 1);
        else {
            double fraction = ThreadLocalRandom.current().nextDouble();
            // Weighted endpoints avoid overflowing maximum - minimum for large ranges.
            value = minimum * (1 - fraction) + maximum * fraction;
            value = Math.max(minimum, Math.min(maximum, value));
        }
        myActualOutputState.setValue(value); myActualOutputState.setDataValid(true);
    }

    @Override protected synchronized void calculateMyActualState() { /* Input changes never resample the output. */ }
    @Override public int getDataTypeOutput() { return TYPE_DOUBLE_IO; }
    @Override public synchronized double getOutputAsNumber() { return super.getOutputAsNumber(); }
    @Override public synchronized String getOutputAsString() { return "integer".equals(outputType) ? Long.toString((long) getOutputAsNumber()) : Double.toString(getOutputAsNumber()); }
    public synchronized long getIntervalMillis() { return intervalMillis; }
    public synchronized double getMinimum() { return minimum; }
    public synchronized double getMaximum() { return maximum; }
    public synchronized String getOutputType() { return outputType; }
    public synchronized boolean isEnabled() { return isInputInUse(0) ? myInputValues[0] != null && myInputValues[0].isDataValid() && myInputValues[0].getOutputAsBoolean() : manuallyEnabled; }
    public synchronized boolean isManuallyEnabled() { return manuallyEnabled; }
    public synchronized void setManuallyEnabled(boolean enabled) { manuallyEnabled = enabled; }
    @Override public synchronized void resetSpecific() { if (isEnabled()) { generate(); updateMyOutputs(); } }
    @Override public synchronized void shutdownRuntime() { scheduleVersion++; if (task != null) task.cancel(false); task = null; }
    @Override public synchronized void deleteThis() { shutdownRuntime(); super.deleteThis(); }

    @Override public synchronized Node getStorageXML(Document document) {
        Element root = (Element) super.getStorageXML(document), settings = document.createElement("RandomNumberGenerator");
        settings.setAttribute("minimum", Double.toString(minimum)); settings.setAttribute("maximum", Double.toString(maximum));
        settings.setAttribute("outputType", outputType); settings.setAttribute("enabled", Boolean.toString(manuallyEnabled));
        settings.setAttribute("intervalMillis", Long.toString(intervalMillis));
        root.appendChild(settings); return root;
    }
    @Override public synchronized boolean initDataComponent(Node node) throws Exception {
        if (!super.initDataComponent(node)) return false;
        Element settings = (Element) ((Element) node).getElementsByTagName("RandomNumberGenerator").item(0);
        if (settings != null) {
            manuallyEnabled = Boolean.parseBoolean(settings.getAttribute("enabled"));
            configure(Double.parseDouble(settings.getAttribute("minimum")), Double.parseDouble(settings.getAttribute("maximum")), settings.getAttribute("outputType"), settings.hasAttribute("intervalMillis") ? Double.parseDouble(settings.getAttribute("intervalMillis")) : 1000);
        }
        start(); return true;
    }
}
