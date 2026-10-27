package mdn.jh.automation.device;

import java.awt.BorderLayout;

import javax.swing.JPanel;

public abstract class DeviceCreator extends JPanel {
	private static final long serialVersionUID = 7785128684277938947L;
	private String deviceName = "UNKNOWN";

	//public abstract boolean deviceIsValid();
	public abstract Device getCreatedDevice() throws Exception;
	
	@Override
	public String toString() {
		return "" + getDeviceName();
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public DeviceCreator() {
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout(0, 0));
	}
}
