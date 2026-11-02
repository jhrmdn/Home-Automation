package mdn.jh.automation.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import mdn.jh.automation.Main;
import mdn.jh.automation.device.Device;
import mdn.jh.automation.io.sink.DataSink;
import mdn.jh.automation.scheduler.UpdateListener;

public class CreateActionDialog extends JDialog {

	private static final long serialVersionUID = 106290544823462481L;

	DataSink newDataSink = null;
	private JPanel panel;
	private JPanel panelSelection;
	private JPanel panelSouth;
	private JButton btnOk;
	private JButton btnCancel;
	private JLabel lblNewLabel_2;
	private JComboBox<Device> comboBoxDevices;
	private Device selectedDevice = null;
	DataSinkCreator actionSelector = null;
	private JLabel lblId;
	private JPanel panel_1;

	public CreateActionDialog(Frame owner) {
		super(owner, true);
		initDeviceBox();
		initialize();
	}

	public DataSink getSelectedDataSink() {
		return newDataSink;
	}

	private void initDeviceBox() {
		Iterator<Device> dev = Main.getMySmartHomeHandler().getDevices().iterator();
		getComboBoxDevices().addItem(null);
		while (dev.hasNext()) {
			getComboBoxDevices().addItem(dev.next());
		}

		getComboBoxDevices().addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				setActionSelector();
			}
		});
	}

	private void setActionSelector() {
		Object o = getComboBoxDevices().getSelectedItem();

		getPanelSelection().removeAll();
		getPanelSelection().validate();
		getPanelSelection().repaint();

		if (o == null) {
			selectedDevice = null;
			return;
		}

		selectedDevice = (Device) o;
		actionSelector = selectedDevice.getDataSinkCreator();
		if (actionSelector == null) {
			JOptionPane.showMessageDialog(this, "This device has no action");
			return;
		}

		actionSelector.addUpdateListener(new UpdateListener() {

			@Override
			public void update() {
				// dataSourceInputParameter = actionSelector.getInputParameter();
				// updateInputParameter();
			}
		});
		getPanelSelection().add(new JLabel("Select Parameter"), BorderLayout.NORTH);
		getPanelSelection().add(actionSelector, BorderLayout.CENTER);
		getPanelSelection().validate();
		getPanelSelection().repaint();

	}

	private void storeAndClose() {

		if (actionSelector == null || selectedDevice == null) {
			JOptionPane.showMessageDialog(this, "Device not selected", "Error", JOptionPane.OK_OPTION);
			return;
		}

		try {
			newDataSink = actionSelector.getDataSink();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Please complete selection", "Error", JOptionPane.OK_OPTION);
			return;
		}

		selectedDevice.getDataSinkHandler().addDataSink(newDataSink);

		this.setVisible(false);
	}

	private void cancel() {
		newDataSink = null;
		setVisible(false);
	}

	private void initialize() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(new Dimension(1316, 1047));
		setPreferredSize(new Dimension(800, 600));
		setTitle("Create Action");
		getContentPane().add(getPanel(), BorderLayout.NORTH);
		getContentPane().add(getPanelSelection(), BorderLayout.CENTER);
		getContentPane().add(getPanelSouth(), BorderLayout.SOUTH);
	}

	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.add(getLblNewLabel_2());
			panel.add(getComboBoxDevices());
			panel.add(getLblId());
		}
		return panel;
	}

	private JPanel getPanelSelection() {
		if (panelSelection == null) {
			panelSelection = new JPanel();
			panelSelection.setPreferredSize(new Dimension(350, 10));
			panelSelection.setLayout(new BorderLayout(0, 0));
		}
		return panelSelection;
	}

	private JPanel getPanelSouth() {
		if (panelSouth == null) {
			panelSouth = new JPanel();
			panelSouth.setLayout(new BoxLayout(panelSouth, BoxLayout.Y_AXIS));
			panelSouth.add(getPanel_1_1());
		}
		return panelSouth;
	}

	private JButton getBtnOk() {
		if (btnOk == null) {
			btnOk = new JButton("Save and Close");
			btnOk.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					storeAndClose();
				}
			});
		}
		return btnOk;
	}

	private JButton getBtnCancel() {
		if (btnCancel == null) {
			btnCancel = new JButton("Cancel");
			btnCancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cancel();
				}
			});
		}
		return btnCancel;
	}

	private JLabel getLblNewLabel_2() {
		if (lblNewLabel_2 == null) {
			lblNewLabel_2 = new JLabel("Select Device:");
		}
		return lblNewLabel_2;
	}

	private JComboBox<Device> getComboBoxDevices() {
		if (comboBoxDevices == null) {
			comboBoxDevices = new JComboBox<Device>();

		}
		return comboBoxDevices;
	}

	private JLabel getLblId() {
		if (lblId == null) {
			lblId = new JLabel("ID:New");
		}
		return lblId;
	}

	private JPanel getPanel_1_1() {
		if (panel_1 == null) {
			panel_1 = new JPanel();
			panel_1.add(getBtnOk());
			panel_1.add(getBtnCancel());
		}
		return panel_1;
	}
}
