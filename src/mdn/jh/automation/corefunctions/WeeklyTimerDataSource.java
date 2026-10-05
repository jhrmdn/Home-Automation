package mdn.jh.automation.corefunctions;

import java.awt.BorderLayout;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.ComponentInputView;
import mdn.jh.automation.io.DataInputIF;
import mdn.jh.automation.io.DataOutputIF;
import mdn.jh.automation.io.RecursionException;
import mdn.jh.automation.io.logic.DataInputConnection;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.source.DataSource;

/** Boolean weekly timer whose last event remains active until another event occurs. */
public class WeeklyTimerDataSource extends DataSource implements DataInputIF {
	private static final long serialVersionUID = 1L;
	private static final InputDefinition[] INPUTS = { new InputDefinition("Enable", DataComponentStub.TYPE_BOOLEAN_IO,
			"Optional: the timer is enabled while true; an unconnected input enables it by default") };
	private final List<ScheduleEntry> schedule = new ArrayList<ScheduleEntry>();
	private boolean value;
	private transient DataOutputIF enableInput;
	private transient boolean enableConnected;

	public WeeklyTimerDataSource() {
		setName("Weekly timer");
		setHelptext("Weekly Boolean timer with multiple independent on/off events per weekday. The optional Enable input defaults to enabled when unconnected.");
		setStatusMessage("No switching times configured");
	}

	public synchronized void setSchedule(List<ScheduleEntry> entries) {
		schedule.clear();
		if (entries != null) schedule.addAll(entries);
		schedule.sort(Comparator.comparingInt(ScheduleEntry::minuteOfWeek));
		setStatusMessage(schedule.size() + " weekly switching event(s)");
		refresh(LocalDateTime.now());
	}

	public synchronized List<ScheduleEntry> getSchedule() {
		return new ArrayList<ScheduleEntry>(schedule);
	}

	public synchronized void refresh(LocalDateTime now) {
		boolean next = isEnabled() && valueAt(now);
		if (next != value) {
			value = next;
			updateMyOutputs();
		}
	}

	public synchronized boolean isEnabled() {
		return !enableConnected || enableInput != null && enableInput.getOutputAsBoolean();
	}

	@Override public synchronized InputDefinition[] getInputDefinitions() { return INPUTS; }
	@Override public synchronized DataOutputIF getInputData(int inputNumber) { return inputNumber == 0 ? enableInput : null; }
	@Override public synchronized boolean isInputInUse(int inputNumber) { return inputNumber == 0 && enableConnected; }
	@Override public synchronized boolean isInputConnected(int inputNumber) { return isInputInUse(inputNumber); }
	@Override public synchronized void unlinkInput(int inputNumber) { if(inputNumber==0){enableInput=null;enableConnected=false;refresh(LocalDateTime.now());} }
	@Override public synchronized void setConnection(DataOutputIF output,int sourceId,int inputNumber)throws RecursionException{if(inputNumber!=0)throw new IllegalArgumentException("Weekly timer has only input 0");if(checkForRecursions(sourceId))throw new RecursionException();enableInput=new ComponentInputView(this,0,output);enableConnected=true;refresh(LocalDateTime.now());}
	@Override public synchronized void update(int inputNumber){if(inputNumber==0)refresh(LocalDateTime.now());}
	@Override public synchronized boolean checkForRecursions(int startId){if(getId()==startId)return true;for(DataInputConnection output:myOutputs)if(output.getDataInput().checkForRecursions(startId))return true;return false;}

	public synchronized boolean valueAt(LocalDateTime now) {
		if (schedule.isEmpty()) return false;
		int current = (now.getDayOfWeek().getValue() - 1) * 1440 + now.getHour() * 60 + now.getMinute();
		ScheduleEntry selected = schedule.get(schedule.size() - 1);
		for (ScheduleEntry entry : schedule) {
			if (entry.minuteOfWeek() > current) break;
			selected = entry;
		}
		return selected.on();
	}

	@Override public boolean getOutputAsBoolean() { return value; }
	@Override public double getOutputAsNumber() { return value ? 1 : 0; }
	@Override public String getOutputAsString() { return Boolean.toString(value); }
	@Override public int getDataTypeOutput() { return DataComponentStub.TYPE_BOOLEAN_IO; }
	@Override public synchronized boolean isDataValid() { return !enableConnected || enableInput != null && enableInput.isDataValid(); }

	@Override
	public synchronized Node getStorageXML(Document document) {
		Element root = getStorage(document);
		Element timer = document.createElement("WeeklyTimer");
		for (ScheduleEntry entry : schedule) {
			Element event = document.createElement("Event");
			event.setAttribute("day", entry.day().name());
			event.setAttribute("time", entry.time().toString());
			event.setAttribute("state", entry.on() ? "on" : "off");
			timer.appendChild(event);
		}
		root.appendChild(timer);
		return root;
	}

	@Override
	public synchronized boolean initDataComponent(Node node) throws Exception {
		initDataSource(node);
		List<ScheduleEntry> entries = new ArrayList<ScheduleEntry>();
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (!"WeeklyTimer".equals(children.item(i).getNodeName())) continue;
			NodeList events = children.item(i).getChildNodes();
			for (int j = 0; j < events.getLength(); j++) if ("Event".equals(events.item(j).getNodeName())) {
				Node event = events.item(j);
				entries.add(new ScheduleEntry(DayOfWeek.valueOf(event.getAttributes().getNamedItem("day").getNodeValue()),
						LocalTime.parse(event.getAttributes().getNamedItem("time").getNodeValue()),
						"on".equals(event.getAttributes().getNamedItem("state").getNodeValue())));
			}
		}
		setSchedule(entries);
		return true;
	}

	@Override
	public JPanel getSpecificDetailsPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.add(new JLabel("Schedule (read-only here; edit in the web details)"), BorderLayout.NORTH);
		JTextArea text = new JTextArea();
		getSchedule().forEach(entry -> text.append(entry.day() + " " + entry.time() + " " + (entry.on() ? "ON" : "OFF") + "\n"));
		text.setEditable(false);
		panel.add(new JScrollPane(text), BorderLayout.CENTER);
		return panel;
	}

	public record ScheduleEntry(DayOfWeek day, LocalTime time, boolean on) implements java.io.Serializable {
		private static final long serialVersionUID = 1L;
		public ScheduleEntry {
			if (day == null || time == null) throw new IllegalArgumentException("Day and time are required");
			time = time.withSecond(0).withNano(0);
		}
		int minuteOfWeek() { return (day.getValue() - 1) * 1440 + time.getHour() * 60 + time.getMinute(); }
	}
}
