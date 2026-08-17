package mdn.jh.automation.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import mdn.jh.automation.Main;
import mdn.jh.automation.device.Device;
import mdn.jh.automation.io.source.DataSource;
import mdn.jh.automation.scheduler.UpdateListener;
import javax.swing.border.LineBorder;
import java.awt.Color;

public class CreateDataSourceDialog extends JDialog {

	private static final long serialVersionUID = 106290544823462481L;

	DataSource newDataSource = null;
	private JPanel panel;
	// private final ButtonGroup buttonGroup = new ButtonGroup();
	// private FritzDevice2JTree tree;
	// private Converter dataConverter = null;

	private JPanel panelCenterSource;
	private JPanel panelSouth;
	private JButton btnSelectDatasource;
	private JButton btnOk;
	private JButton btnCancel;
	private JLabel lblNewLabel_2;
	private JComboBox<Device> comboBoxDevices;
	private Device selectedDevice = null;

	public CreateDataSourceDialog(Frame owner) {
		super(owner, true);
		initDeviceBox();
		initialize();
	}

	public DataSource getSelectedDataSource() {
		return newDataSource;
	}

	private void initDeviceBox() {
		Iterator<Device> dev = Main.getMySmartHomeHandler().getDevices().iterator();
		getComboBoxDevices().addItem(null);
		while (dev.hasNext()) {
			getComboBoxDevices().addItem(dev.next());
		}

		getComboBoxDevices().addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				setSourceSelector();
			}
		});

	}

	DataSourceCreator sourceSelector = null;
	private JLabel lblId;

	private void setSourceSelector() {
		// dataSourceInputParameter = null;
		if (sourceSelector != null) {
			getPanelCenterSource().remove(sourceSelector);
			repaint();
		}
		Object o = getComboBoxDevices().getSelectedItem();

		if (o == null) {
			// updateInputParameter();
			// getPanelSelection().removeAll();
			// getPanelSelection().validate();
			// getPanelSelection().repaint();
			return;
		}

		// updateInputParameter();
		selectedDevice = (Device) o;
		try {
			sourceSelector = selectedDevice.getDataSourceCreator();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Exception: " + e.getLocalizedMessage(), "Error Creating DataSource",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (sourceSelector == null) {
			return;
		}
		getPanelCenterSource().add(sourceSelector, BorderLayout.CENTER);
		// repaint();
		revalidate();

		/*
		 * SwingUtilities.invokeLater(new Runnable() { public void run() { try {
		 * 
		 * } catch (Exception e) { e.printStackTrace(); } } });
		 */
		sourceSelector.addUpdateListener(new UpdateListener() {
			@Override
			public void update() {
				// dataSourceInputParameter = sourceSelector.getInputParameter();
				// updateInputParameter();
			}
		});
		// getPanelSelection().add(new JLabel("Select Parameter"), BorderLayout.NORTH);
		//
		// getPanelSelection().validate();
		// getPanelSelection().repaint();

	}

	private void createDataSource() {
		if (selectedDevice == null) {
			return;
		}

		getBtnSelectDatasource().setEnabled(false);
		getBtnOk().setEnabled(true);
		// getComboBoxSelectConverter().setEnabled(true);
		// getBtnSelectDatasource().setEnabled(false);
		getComboBoxDevices().setEnabled(false);

		// newDataSource = new DataSourceStringInput();
		// getLblId().setText("ID: " + newDataSource.getId());
		// newDataSource.setDataSourceInputParameter(dataSourceInputParameter);

	}

	private void showMessage(String title, String message) {
		JOptionPane.showMessageDialog(this, message, title, JOptionPane.OK_OPTION);
	}

	private void storeAndClose() {
		if (sourceSelector == null) {
			showMessage("Hint", "Nothing selected");
		}

		// DataSource newDataSource=null;
		try {
			newDataSource = sourceSelector.getDataSource();
		} catch (Exception e) {
			showMessage("Hint", e.getMessage());
			return;
		}

		selectedDevice.getDataSourceHandler().addDataSource(newDataSource);

		this.setVisible(false);
	}

	private void cancel() {
		newDataSource = null;
		setVisible(false);
	}

	private void initialize() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setSize(new Dimension(1316, 862));
		setPreferredSize(new Dimension(800, 600));
		setTitle("Create Datasource");
		getContentPane().add(getPanel(), BorderLayout.NORTH);
		getContentPane().add(getPanelCenterSource(), BorderLayout.CENTER);
		getContentPane().add(getPanelSouth(), BorderLayout.SOUTH);
	}

	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setBorder(new LineBorder(new Color(0, 0, 0), 3, true));
			panel.add(getLblNewLabel_2());
			panel.add(getComboBoxDevices());
			panel.add(getBtnSelectDatasource());
			panel.add(getLblId());
		}
		return panel;
	}

	private JPanel getPanelCenterSource() {
		if (panelCenterSource == null) {
			panelCenterSource = new JPanel();
			panelCenterSource.setLayout(new BorderLayout(0, 0));

		}
		return panelCenterSource;
	}

	private JPanel getPanelSouth() {
		if (panelSouth == null) {
			panelSouth = new JPanel();
			panelSouth.setBorder(new LineBorder(new Color(0, 0, 0), 3, true));
			panelSouth.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			panelSouth.add(getBtnOk());
			panelSouth.add(getBtnCancel());
		}
		return panelSouth;
	}

	private JButton getBtnSelectDatasource() {
		if (btnSelectDatasource == null) {
			btnSelectDatasource = new JButton("Select");
			btnSelectDatasource.setVisible(false);
			btnSelectDatasource.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					createDataSource();
				}
			});
		}
		return btnSelectDatasource;
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
}
