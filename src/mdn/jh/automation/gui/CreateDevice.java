package mdn.jh.automation.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import mdn.jh.automation.Main;
import mdn.jh.automation.device.Device;
import mdn.jh.automation.device.DeviceCreator;

public class CreateDevice extends JFrame {

	private static final long serialVersionUID = -4711250092215518024L;
	private static Vector<DeviceCreator> listCreators = new Vector<DeviceCreator>();
	private JPanel panel;
	private JComboBox<DeviceCreator> comboBox;
	private JPanel panel_1;
	private JButton btnNewButton;
	private JButton btnNewButton_1;
	DeviceCreator selectedCreator = null;
	private JLabel lbInformation;

	public Device getDevice() {

		return null;
	}

	public static void registerDeviceCreator(DeviceCreator deviceCreator) {
		listCreators.add(deviceCreator);
	}

	private void initialize() {
		setSize(new Dimension(800, 800));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		fillComboBox();
		setTitle("Create Device");
		getContentPane().add(getPanel(), BorderLayout.NORTH);

		getContentPane().add(getPanel_1(), BorderLayout.SOUTH);
	}

	private void fillComboBox() {

		Iterator<DeviceCreator> it = listCreators.iterator();
		while (it.hasNext())
			getComboBox().addItem(it.next());
		getComboBox().addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				// selectDevice();

			}
		});

		getComboBox().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectDevice();
			}
		});

	}

	private void addCreatedDevice() {
		if (selectedCreator == null) {
			JOptionPane.showMessageDialog(this, "Please select a device type first");
			return;
		}

		// if (!selectedCreator.deviceIsValid()) {
		// JOptionPane.showMessageDialog(this,"Please start 'check' first. Must be ok");
		// return;
		// }

		Device device = null;
		try {
			device = selectedCreator.getCreatedDevice();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		JOptionPane.showMessageDialog(this, "Device created: " + device.getName(), "New Device available",
				JOptionPane.INFORMATION_MESSAGE);
		Main.getMySmartHomeHandler().registerDevice(device);
		device.startUpdateThreads();

		this.dispose();
	}

	private void selectDevice() {
		if (selectedCreator != null) {
			getContentPane().remove(selectedCreator);
			selectedCreator = null;
			repaint();
		}

		Object o = getComboBox().getSelectedItem();
		if (o == null)
			return;

		selectedCreator = (DeviceCreator) o;
		// System.out.println("Device Selected: " + o);
		getContentPane().add(selectedCreator, BorderLayout.CENTER);
		revalidate();

	}

	private void cancelOption() {
		dispose();
	}

	public CreateDevice() throws HeadlessException {
		initialize();
	}

	public CreateDevice(GraphicsConfiguration gc) {
		super(gc);
		initialize();
	}

	public CreateDevice(String title) throws HeadlessException {
		super(title);
		initialize();
	}

	public CreateDevice(String title, GraphicsConfiguration gc) {
		super(title, gc);
		initialize();
	}

	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.add(getComboBox());
		}
		return panel;
	}

	private JComboBox<DeviceCreator> getComboBox() {
		if (comboBox == null) {
			comboBox = new JComboBox<DeviceCreator>();

		}
		return comboBox;
	}

	private JPanel getPanel_1() {
		if (panel_1 == null) {
			panel_1 = new JPanel();
			panel_1.add(getBtnNewButton());
			panel_1.add(getBtnNewButton_1());
			panel_1.add(getLbInformation());
		}
		return panel_1;
	}

	private JButton getBtnNewButton() {
		if (btnNewButton == null) {
			btnNewButton = new JButton("Ok");
			btnNewButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					addCreatedDevice();
				}
			});
		}
		return btnNewButton;
	}

	private JButton getBtnNewButton_1() {
		if (btnNewButton_1 == null) {
			btnNewButton_1 = new JButton("Cancel");
			btnNewButton_1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cancelOption();
				}
			});
		}
		return btnNewButton_1;
	}

	private JLabel getLbInformation() {
		if (lbInformation == null) {
			lbInformation = new JLabel("-");
		}
		return lbInformation;
	}
}
