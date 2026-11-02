package mdn.jh.automation.devices.fritz.datasource;

import java.lang.reflect.Constructor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.Main;
import mdn.jh.automation.io.converter.Converter;
import mdn.jh.automation.io.source.DataSource;
import mdn.jh.automation.io.source.WrongDataTypeException;

/**
 * DataSource with a String input and a Converter. public void
 * updateValue(String value) can be used for input
 * 
 * @author jhrib
 *
 */
public class FritzDataSource extends DataSource {

	private static final long serialVersionUID = 1411164975126427564L;
	FritzDataSourceInputParameter dataSourceInputParameter = null;
	Converter myDataConverter = null;
	boolean fritzValid = true;
	private boolean timestampPulseEnabled;
	private long timestampPulseDurationMillis = 1000;
	private transient String lastTimestamp;
	private transient volatile boolean timestampPulseActive;
	private transient long timestampPulseGeneration;
	private static final ScheduledExecutorService PULSE_SCHEDULER = Executors.newSingleThreadScheduledExecutor(task -> {
		Thread thread = new Thread(task, "fritz-timestamp-pulse");
		thread.setDaemon(true);
		return thread;
	});

	public void setDataFromFritzBoxValid(boolean dataFromFritzBoxValid) {
		this.fritzValid = dataFromFritzBoxValid;
	}

	public FritzDataSource() {
		super();
	}

	@Override
	public JPanel getSpecificDetailsPanel() {
		FritzSourceDetailsPanel p = new FritzSourceDetailsPanel();
		p.setFritzSource(this);
		return p;
	}

	public FritzDataSourceInputParameter getMyDataSourceInputParameter() {
		return dataSourceInputParameter;
	}

	public void setDataSourceInputParameter(FritzDataSourceInputParameter dataSourceInputParameter) {
		this.dataSourceInputParameter = dataSourceInputParameter;
		if (dataSourceInputParameter == null) {
			return;
		}
		setStatusMessage("Name:" + dataSourceInputParameter.getParameter2());

	}

	public void configureTimestampPulse(boolean enabled, long durationMillis) {
		if (enabled && durationMillis < 1) throw new IllegalArgumentException("Pulse duration must be at least 1 millisecond");
		timestampPulseEnabled = enabled;
		if (enabled) timestampPulseDurationMillis = durationMillis;
		lastTimestamp = null;
		timestampPulseActive = false;
		timestampPulseGeneration++;
		if (enabled) setStatusMessage("Boolean pulse when the selected FRITZ! value changes (" + durationMillis + " ms)");
	}

	public boolean isTimestampPulseEnabled() { return timestampPulseEnabled; }
	public long getTimestampPulseDurationMillis() { return timestampPulseDurationMillis; }

	public Converter getDataConverter() {
		return myDataConverter;
	}

	public void setDataConverter(Converter dataConverter) throws WrongDataTypeException {

		if (isDataTypeLocked()) {
			throw new WrongDataTypeException();
		}
		// setMyDataValue(dataConverter);
		this.myDataConverter = dataConverter;
	}

	public void updateValue(String value) {
		if (dataSourceInputParameter == null || myDataConverter == null) {
			Main.getLogger().log(Level.WARNING, "DataSourceBoolean - Parameter: null OR Converter: null");
			return;
		}
		if (timestampPulseEnabled) {
			updateTimestampPulse(value);
			return;
		}
		myDataConverter.setValue(value);
		// setMyRawValue(value);
		updateMyOutputs();
	}

	private void updateTimestampPulse(String value) {
		long generation = -1;
		synchronized (this) {
			if (lastTimestamp == null) {
				lastTimestamp = value;
				timestampPulseActive = false;
			} else if (!java.util.Objects.equals(lastTimestamp, value)) {
				lastTimestamp = value;
				timestampPulseActive = true;
				generation = ++timestampPulseGeneration;
			}
		}
		updateMyOutputs();
		if (generation >= 0) {
			final long scheduledGeneration = generation;
			PULSE_SCHEDULER.schedule(() -> finishTimestampPulse(scheduledGeneration), timestampPulseDurationMillis,
					TimeUnit.MILLISECONDS);
		}
	}

	private void finishTimestampPulse(long generation) {
		synchronized (this) {
			if (!timestampPulseEnabled || generation != timestampPulseGeneration) return;
			timestampPulseActive = false;
		}
		updateMyOutputs();
	}

	/**
	 * Returns the actual value depending on the datatype
	 */
	@Override
	public String toString() {
		if (timestampPulseEnabled) return Boolean.toString(getOutputAsBoolean());
		if (myDataConverter == null) {
			return null;
		}
		return myDataConverter.getOutputAsString();
	}

	@Override
	public boolean getOutputAsBoolean() {
		if (timestampPulseEnabled) return timestampPulseActive;
		if (myDataConverter == null)
			return false;
		return myDataConverter.getOutputAsBoolean();
	}

	@Override
	public double getOutputAsNumber() {
		if (timestampPulseEnabled) return timestampPulseActive ? 1 : 0;
		if (myDataConverter == null)
			return 0;
		return myDataConverter.getOutputAsNumber();
	}

	@Override
	public String getOutputAsString() {
		if (timestampPulseEnabled) return Boolean.toString(timestampPulseActive);
		if (myDataConverter == null)
			return null;
		return myDataConverter.getOutputAsString();
	}

	public int getDataTypeOutput() {
		if (timestampPulseEnabled) return TYPE_BOOLEAN_IO;
		return myDataConverter.getDataTypeOutput();
	}

	@Override
	public boolean isDataValid() {
	//	System.out.println(" Is Valid: " + fritzValid + " - " + this.hashCode());
		if (!fritzValid) {
			return false;
		}

		if (timestampPulseEnabled) return lastTimestamp != null;

		if (myDataConverter == null) {
			return false;
		}
		if (!myDataConverter.isDataValid()) {
			return false;
		}

		return true;
	}

	@Override
	public Node getStorageXML(Document doc) {
		Element rootElement = null;

		rootElement = getStorage(doc);

		// rootElement.appendChild(getDataComponentBaseXML(doc));

		if (dataSourceInputParameter != null)
			rootElement.appendChild(dataSourceInputParameter.getStorageXML(doc));

		rootElement.appendChild(myDataConverter.getStorageXML(doc));
		if (timestampPulseEnabled) {
			Element pulse = doc.createElement("TimestampPulse");
			pulse.setAttribute("durationMillis", Long.toString(timestampPulseDurationMillis));
			rootElement.appendChild(pulse);
		}

		return rootElement;

	}

	@Override
	public boolean initDataComponent(Node node) throws Exception {
		if (node == null)
			return false;
		String name = node.getNodeName();
		if (!"DataSource".equals(name)) {
			throw new Exception("Creating - Expected Node: DataSource - Found:" + name);
		}

		initDataSource(node);

		// String nodeID = node.getAttributes().getNamedItem("id").getNodeValue();
		// overrideID(Integer.valueOf(nodeID));

		NodeList nl = node.getChildNodes();
		Node temp = null;
		for (int i = 0; i < nl.getLength(); i++) {
			temp = nl.item(i);

			if ("Parameters".equals(temp.getNodeName())) {
				FritzDataSourceInputParameter di = new FritzDataSourceInputParameter(temp);
				setDataSourceInputParameter(di);

			}

			if ("DataConverter".equals(temp.getNodeName())) {
				Node converterClass = temp.getAttributes().getNamedItem("class");
				if (converterClass == null) {
					throw new Exception("DataConverter attribute not set for DataSource:");
				}
				String cl = converterClass.getNodeValue();
				Class<?> clazz = Class.forName(cl);
				Constructor<?> constructor = clazz.getConstructor();
				Object instance = constructor.newInstance();

				myDataConverter = (Converter) instance;
				myDataConverter.initDataComponent(temp);
				// setMyDataValue(myDataConverter);

			}

			if ("TimestampPulse".equals(temp.getNodeName())) {
				Node duration = temp.getAttributes().getNamedItem("durationMillis");
				timestampPulseDurationMillis = duration == null ? 1000 : Long.parseLong(duration.getNodeValue());
				timestampPulseEnabled = true;
			}

		}

		if (timestampPulseEnabled)
			setStatusMessage("Boolean pulse when the selected FRITZ! value changes (" + timestampPulseDurationMillis + " ms)");
		return true;
	}

}
