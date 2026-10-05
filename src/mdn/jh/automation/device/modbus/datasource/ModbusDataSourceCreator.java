package mdn.jh.automation.device.modbus.datasource;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import mdn.jh.automation.device.modbus.DataCellEditor;
import mdn.jh.automation.device.modbus.DataCellRenderer;
import mdn.jh.automation.device.modbus.ModbusDataTable;
import mdn.jh.automation.device.modbus.ModbusDevice;
import mdn.jh.automation.device.modbus.ModbusSelectorWindow;
import mdn.jh.automation.gui.DataSourceCreator;
import mdn.jh.automation.io.DataOutputIF;
import mdn.jh.automation.io.source.DataSource;
import javax.swing.border.LineBorder;

public class ModbusDataSourceCreator extends DataSourceCreator implements ModbusSelectorWindow {

	private static final long serialVersionUID = 7355263103131573105L;
	private ModbusDevice modbusDevice = null;
	int selectedRegister = -1;
	protected DataOutputIF myOutput = null;
	
	
	@Override
	public DataSource getDataSource() throws Exception {

		if (selectedRegister == -1) {
			throw new Exception("Please select a register");
		}

		int type = buttonGroupModbusType.getSelection().getMnemonic() - 48;

		ModbusDataSource modbusDataSource = new ModbusDataSource(type, selectedRegister);
		
		// modbusDataSource.set
		return modbusDataSource;
	}

	private void getSelectedAddress() {
		int row = getTable().getSelectedRow();
		int col = getTable().getSelectedColumn();

		if (col < 2 || row < 0)
			return;

		selectedRegister = row * 16 + col - 2 + start;
		getTextFieldSelected().setText(Integer.toString(selectedRegister));
	}

	public int getSelectedRegister() {
		return selectedRegister;
	}

	/**
	 * as defined in ModbusDataTable ModbusType_Coil_RW_1_bit = 1;
	 * ModbusType_Discrete_RO_1_bit = 2; ModbusType_Holding_RW_16_bit = 3;
	 * ModbusType_Input_RO_16_bit = 4;
	 */
	public int getType() {
		return buttonGroupModbusType.getSelection().getMnemonic() - 48;
	}

	/**
	 * Create the frame.
	 */
	public ModbusDataSourceCreator(ModbusDevice modbusDevice) {
		this.modbusDevice = modbusDevice;
		// setTitle("Modbus Monitor");
		initialize();
		setConnectionData();
		// checkConnection();
		startAutoUpdate();

	}

	private void setConnectionData() {
		if (modbusDevice == null)
			return;
		setHostData(modbusDevice.getIp_hostname(), "" + modbusDevice.getPort(), "" + modbusDevice.getSlaveID());

	}

	private JPanel pnNorthSelect;
	private JTextField tfHost;
	private JTextField tfPort;
	private JPanel panel;
	private JTable table;
	private JButton btnNewButton;

	ModbusDataTable modbusClient = null;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	private JTextField tfStartAddress;
	private JLabel lblNewLabel_2;
	private JLabel lblNewLabel_3;
	private JTextField tfUpdateTime;
	private JPanel pnSouth;
	private JLabel lbUpdate;

	private boolean runUpdate = false;
	private int updateTime = 200;
	private JPanel panel_1;
	private JRadioButton rdbtnCoils;
	private JRadioButton rdbtnDiscrete;
	private JRadioButton rdbtnHolding;
	private JRadioButton rdbtnInput;
	private final ButtonGroup buttonGroupModbusType = new ButtonGroup();
	private JLabel lblNewLabel_4;
	private JTextField tfSlaveID;
	private JLabel lblNewLabel_5;
	private JTextField tfRegisterNumber;
	private JButton btSnapshot;
	private JButton btDisconnect;
	private JLabel lblNewLabel_6;
	private JTextField tfStatus;
	private JCheckBox chckbxStart1;
	private JLabel lblNewLabel_7;
	private JPanel panel_2;
	private JScrollPane scrollPane_1;
	private JTextArea textArea;

	@Override
	public void addMessage(String message) {
		getTextArea().setText(getTextArea().getText() + "\n" + message);

	}

	public void setHostData(String host, String port, String slaveID) {
		getTfHost().setText(host);
		getTfPort().setText(port);
		getTfSlaveID().setText(slaveID);
	}

	private void startAutoUpdate() {
		updateModbusData(Reason_Regular_Update);
		try {
			updateTime = Integer.valueOf(getTfUpdateTime().getText());
		} catch (Exception e) {
			updateTime = 200;
			getTfUpdateTime().setText("" + updateTime);
		}

		runUpdate = true;
		runAutoUpdate();
	}

	private void stopAutoUpdate() {
		runUpdate = false;
	}

	private void invokeDisconnect() {
		if (modbusClient == null) {
			return;
		}
		modbusClient.disconnect();
	}

	@Override
	public boolean displayValuesAsHex() {
		return getChckbxDisplayHex().isSelected();
	}

	private void runAutoUpdate() {
		Thread t = new Thread() {
			@Override
			public void run() {

				while (runUpdate) {

					try {
						Thread.sleep(updateTime);
					} catch (Exception e) {
						// TODO: handle exception
					}
					updateModbusData(Reason_Regular_Update);
				}

			}

		};
		t.start();
	}

	public static final int Reason_Regular_Update = 0;
	public static final int Reason_Changed_Modbus_Type = 1;
	public static final int Reason_Changed_Host_Port = 2;
	private JScrollPane scrollPane;
	private JButton btnNewButton_1;
	private JCheckBox chckbxDisplayHex;
	private JTextField textFieldSelected;
	int start = 0;
	private JLabel lblNewLabel_8;

	private void updateModbusData(int reason) {
		if (modbusDevice == null)
			return;

		getLbUpdate().setForeground(Color.GREEN);

		// int slaveID = 1;
		int registerNumber = 1;
		int modbusType = buttonGroupModbusType.getSelection().getMnemonic() - 48;
		try {
			start = Integer.valueOf(getTfStartAddress().getText());
			if (getChckbxStart1().isSelected()) {
				if (start < 1) {
					start = 1;
					getTfStartAddress().setText("" + start);
				}
				start = start - 1;
		//		getTfUsedStart().setText("" + start);
			}

			else {
				if (start < 0) {
					start = 0;
					getTfStartAddress().setText("" + start);
				}
			//	getTfUsedStart().setText("" + start);
			}

		} catch (Exception e) {

			return;
		}

		// slaveID = modbusDevice.getSlaveID();

		try {
			registerNumber = Integer.valueOf(getTfRegisterNumber().getText());
		} catch (Exception e) {
			// getTfRegisterNumber().setText("" + registerNumber);
			return;
		}

		// guiForConnection(false);

		if (modbusClient == null || !modbusClient.isConnected() || reason > Reason_Regular_Update) {

			String host = modbusDevice.getIp_hostname();
			if ("".equals(host) || host == null) {
				getLbUpdate().setForeground(Color.GRAY);
				return;
			}

			// int port = modbusDevice.getPort();

			if (reason == Reason_Changed_Host_Port || modbusClient == null) {
				// modbusClient = new ModbusDataTable(host, port, slaveID, this);
				modbusClient = new ModbusDataTable(modbusDevice.getModbusMaster(), modbusDevice.getSlaveID(), this);
			}
			modbusClient.setStartWith1(getChckbxStart1().isSelected());

			if (!modbusClient.isConnected() && reason == Reason_Changed_Modbus_Type) {
				// do nothing here
			} else {
				modbusClient.update(start, registerNumber, modbusType);
			}
			getTable().setModel(modbusClient);

			DataCellEditor ce = null;
			if (modbusClient.is_modbus_type_16_bit()) {
				ce = new DataCellEditor(new JTextField());
			} else {
				ce = new DataCellEditor(new JCheckBox());
			}

			for (int i = 2; i < 18; i++) {
				table.getColumnModel().getColumn(i).setCellEditor(ce);
				table.getColumnModel().getColumn(i).setCellRenderer(new DataCellRenderer(modbusClient));
			}

		} else {
			modbusClient.setStartWith1(getChckbxStart1().isSelected());
			modbusClient.update(start, registerNumber, modbusType);
			DefaultTableModel dm = (DefaultTableModel) getTable().getModel();
			dm.fireTableDataChanged();
		}

		getLbUpdate().setForeground(Color.GRAY);
	}

	private void changedModbusType() {
		getTfRegisterNumber().requestFocus();
		getTfStartAddress().requestFocus();
		updateModbusData(Reason_Changed_Modbus_Type);
	}

	private void initialize() {
		// setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 811, 606);

		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout(0, 0));
		// setContentPane(contentPane);
		add(getPnNorthSelect(), BorderLayout.NORTH);
		add(getPanel(), BorderLayout.CENTER);
		add(getPnSouth(), BorderLayout.SOUTH);

	}

	private JPanel getPnNorthSelect() {
		if (pnNorthSelect == null) {
			pnNorthSelect = new JPanel();
			GridBagLayout gbl_pnNorthSelect = new GridBagLayout();
			gbl_pnNorthSelect.columnWidths = new int[] { 22, 86, 0, 20, 86, 67, 0, 0, 0 };
			gbl_pnNorthSelect.rowHeights = new int[] { 20, 0, 23, 0, 0 };
			gbl_pnNorthSelect.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
			gbl_pnNorthSelect.rowWeights = new double[] { 1.0, 0.0, 1.0, 1.0, Double.MIN_VALUE };
			pnNorthSelect.setLayout(gbl_pnNorthSelect);
			GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
			gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
			gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel.gridx = 0;
			gbc_lblNewLabel.gridy = 0;
			pnNorthSelect.add(getLblNewLabel(), gbc_lblNewLabel);
			GridBagConstraints gbc_tfHost = new GridBagConstraints();
			gbc_tfHost.anchor = GridBagConstraints.NORTHWEST;
			gbc_tfHost.insets = new Insets(0, 0, 5, 5);
			gbc_tfHost.gridx = 1;
			gbc_tfHost.gridy = 0;
			pnNorthSelect.add(getTfHost(), gbc_tfHost);
			GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
			gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
			gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_2.gridx = 4;
			gbc_lblNewLabel_2.gridy = 0;
			pnNorthSelect.add(getLblNewLabel_2(), gbc_lblNewLabel_2);
			GridBagConstraints gbc_tfStartAddress = new GridBagConstraints();
			gbc_tfStartAddress.anchor = GridBagConstraints.NORTHWEST;
			gbc_tfStartAddress.insets = new Insets(0, 0, 5, 5);
			gbc_tfStartAddress.gridx = 5;
			gbc_tfStartAddress.gridy = 0;
			pnNorthSelect.add(getTfStartAddress(), gbc_tfStartAddress);
			GridBagConstraints gbc_chckbxStart1 = new GridBagConstraints();
			gbc_chckbxStart1.anchor = GridBagConstraints.WEST;
			gbc_chckbxStart1.insets = new Insets(0, 0, 5, 5);
			gbc_chckbxStart1.gridx = 6;
			gbc_chckbxStart1.gridy = 0;
			pnNorthSelect.add(getChckbxStart1(), gbc_chckbxStart1);
			GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
			gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
			gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
			gbc_btnNewButton.anchor = GridBagConstraints.NORTH;
			gbc_btnNewButton.gridx = 7;
			gbc_btnNewButton.gridy = 0;
			pnNorthSelect.add(getBtnNewButton(), gbc_btnNewButton);
			GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
			gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
			gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_1.gridx = 0;
			gbc_lblNewLabel_1.gridy = 1;
			pnNorthSelect.add(getLblNewLabel_1(), gbc_lblNewLabel_1);
			GridBagConstraints gbc_tfPort = new GridBagConstraints();
			gbc_tfPort.anchor = GridBagConstraints.NORTHWEST;
			gbc_tfPort.insets = new Insets(0, 0, 5, 5);
			gbc_tfPort.gridx = 1;
			gbc_tfPort.gridy = 1;
			pnNorthSelect.add(getTfPort(), gbc_tfPort);
			GridBagConstraints gbc_lblNewLabel_8 = new GridBagConstraints();
			gbc_lblNewLabel_8.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_8.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_8.gridx = 2;
			gbc_lblNewLabel_8.gridy = 1;
			pnNorthSelect.add(getLblNewLabel_8(), gbc_lblNewLabel_8);
			GridBagConstraints gbc_textFieldSelected = new GridBagConstraints();
			gbc_textFieldSelected.anchor = GridBagConstraints.WEST;
			gbc_textFieldSelected.insets = new Insets(0, 0, 5, 5);
			gbc_textFieldSelected.gridx = 3;
			gbc_textFieldSelected.gridy = 1;
			pnNorthSelect.add(getTextFieldSelected(), gbc_textFieldSelected);
			GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
			gbc_lblNewLabel_5.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_5.gridx = 4;
			gbc_lblNewLabel_5.gridy = 1;
			pnNorthSelect.add(getLblNewLabel_5(), gbc_lblNewLabel_5);
			GridBagConstraints gbc_tfRegisterNumber = new GridBagConstraints();
			gbc_tfRegisterNumber.insets = new Insets(0, 0, 5, 5);
			gbc_tfRegisterNumber.fill = GridBagConstraints.HORIZONTAL;
			gbc_tfRegisterNumber.gridx = 5;
			gbc_tfRegisterNumber.gridy = 1;
			pnNorthSelect.add(getTfRegisterNumber(), gbc_tfRegisterNumber);
			GridBagConstraints gbc_chckbxDisplayHex = new GridBagConstraints();
			gbc_chckbxDisplayHex.anchor = GridBagConstraints.WEST;
			gbc_chckbxDisplayHex.insets = new Insets(0, 0, 5, 5);
			gbc_chckbxDisplayHex.gridx = 6;
			gbc_chckbxDisplayHex.gridy = 1;
			pnNorthSelect.add(getChckbxDisplayHex(), gbc_chckbxDisplayHex);
			GridBagConstraints gbc_btSnapshot = new GridBagConstraints();
			gbc_btSnapshot.fill = GridBagConstraints.HORIZONTAL;
			gbc_btSnapshot.insets = new Insets(0, 0, 5, 0);
			gbc_btSnapshot.gridx = 7;
			gbc_btSnapshot.gridy = 1;
			pnNorthSelect.add(getBtSnapshot(), gbc_btSnapshot);
			GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
			gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_4.gridx = 0;
			gbc_lblNewLabel_4.gridy = 2;
			pnNorthSelect.add(getLblNewLabel_4(), gbc_lblNewLabel_4);
			GridBagConstraints gbc_tfSlaveID = new GridBagConstraints();
			gbc_tfSlaveID.insets = new Insets(0, 0, 5, 5);
			gbc_tfSlaveID.fill = GridBagConstraints.HORIZONTAL;
			gbc_tfSlaveID.gridx = 1;
			gbc_tfSlaveID.gridy = 2;
			pnNorthSelect.add(getTfSlaveID(), gbc_tfSlaveID);
			GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
			gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_3.gridx = 4;
			gbc_lblNewLabel_3.gridy = 2;
			pnNorthSelect.add(getLblNewLabel_3(), gbc_lblNewLabel_3);
			GridBagConstraints gbc_tfUpdateTime = new GridBagConstraints();
			gbc_tfUpdateTime.insets = new Insets(0, 0, 5, 5);
			gbc_tfUpdateTime.fill = GridBagConstraints.HORIZONTAL;
			gbc_tfUpdateTime.gridx = 5;
			gbc_tfUpdateTime.gridy = 2;
			pnNorthSelect.add(getTfUpdateTime(), gbc_tfUpdateTime);
			GridBagConstraints gbc_btDisconnect = new GridBagConstraints();
			gbc_btDisconnect.insets = new Insets(0, 0, 5, 0);
			gbc_btDisconnect.gridx = 7;
			gbc_btDisconnect.gridy = 2;
			pnNorthSelect.add(getBtDisconnect(), gbc_btDisconnect);
			GridBagConstraints gbc_panel_1 = new GridBagConstraints();
			gbc_panel_1.gridwidth = 8;
			gbc_panel_1.fill = GridBagConstraints.BOTH;
			gbc_panel_1.gridx = 0;
			gbc_panel_1.gridy = 3;
			pnNorthSelect.add(getPanel_1(), gbc_panel_1);
		}
		return pnNorthSelect;
	}

	private JTextField getTfHost() {
		if (tfHost == null) {
			tfHost = new JTextField();
			tfHost.setEnabled(false);
			tfHost.setText("192.168.17.32");
			tfHost.setMinimumSize(new Dimension(80, 20));
			tfHost.setPreferredSize(new Dimension(60, 20));
			tfHost.setHorizontalAlignment(SwingConstants.LEFT);
			tfHost.setColumns(10);
			tfHost.requestFocusInWindow();

		}
		return tfHost;
	}

	private JTextField getTfPort() {
		if (tfPort == null) {
			tfPort = new JTextField();
			tfPort.setEnabled(false);
			tfPort.setMinimumSize(new Dimension(80, 20));
			tfPort.setPreferredSize(new Dimension(60, 20));
			tfPort.setHorizontalAlignment(SwingConstants.LEFT);
			tfPort.setText("502");
			tfPort.setColumns(10);
		}
		return tfPort;
	}

	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setLayout(new BorderLayout(0, 0));
			// panel.add(getTable(), BorderLayout.CENTER);
			panel.add(getScrollPane(), BorderLayout.NORTH);

			// panel.add(getScrollPane(), BorderLayout.CENTER);
		}
		return panel;
	}

	private JTable getTable() {
		if (table == null) {
			table = new JTable(0, 18);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					getSelectedAddress();
				}
			});

			// table.setDefaultRenderer(String.class, new BoardTableCellRenderer());
		}
		return table;
	}

	private JButton getBtnNewButton() {
		if (btnNewButton == null) {
			btnNewButton = new JButton("Connect / Start");
			btnNewButton.setVisible(false);
			btnNewButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if (runUpdate) {
						btnNewButton.setText("Start");
						btnNewButton.setBackground(new Color(240, 240, 240));
						stopAutoUpdate();
					} else {
						btnNewButton.setText("Stop");
						btnNewButton.setBackground(Color.GREEN);
						startAutoUpdate();
					}

				}
			});
		}
		return btnNewButton;
	}

	private JLabel getLblNewLabel() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel("Host");
		}
		return lblNewLabel;
	}

	private JLabel getLblNewLabel_1() {
		if (lblNewLabel_1 == null) {
			lblNewLabel_1 = new JLabel("Port");
		}
		return lblNewLabel_1;
	}

	private JTextField getTfStartAddress() {
		if (tfStartAddress == null) {
			tfStartAddress = new JTextField();
			tfStartAddress.setName("StartRegister");
			tfStartAddress.setMinimumSize(new Dimension(80, 20));
			tfStartAddress.setPreferredSize(new Dimension(60, 20));
			tfStartAddress.setText("0");
			tfStartAddress.setHorizontalAlignment(SwingConstants.LEFT);
			tfStartAddress.setColumns(10);
			tfStartAddress.setInputVerifier(new AddressVerifier());
		}
		return tfStartAddress;
	}

	private JLabel getLblNewLabel_2() {
		if (lblNewLabel_2 == null) {
			lblNewLabel_2 = new JLabel("Start Register");
		}
		return lblNewLabel_2;
	}

	private JLabel getLblNewLabel_3() {
		if (lblNewLabel_3 == null) {
			lblNewLabel_3 = new JLabel("Update [ms]");
		}
		return lblNewLabel_3;
	}

	private JTextField getTfUpdateTime() {
		if (tfUpdateTime == null) {
			tfUpdateTime = new JTextField();
			tfUpdateTime.setEditable(false);
			tfUpdateTime.setText("500");
			tfUpdateTime.setColumns(10);
		}
		return tfUpdateTime;
	}

	private JPanel getPnSouth() {
		if (pnSouth == null) {
			pnSouth = new JPanel();
			GridBagLayout gbl_pnSouth = new GridBagLayout();
			gbl_pnSouth.columnWidths = new int[] { 104, 35, 31, 86, 0, 5, 0, 0, 0, 0, 0, 0 };
			gbl_pnSouth.rowHeights = new int[] { 22, 80, 0 };
			gbl_pnSouth.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0,
					Double.MIN_VALUE };
			gbl_pnSouth.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
			pnSouth.setLayout(gbl_pnSouth);
			GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
			gbc_lblNewLabel_6.anchor = GridBagConstraints.WEST;
			gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_6.gridx = 2;
			gbc_lblNewLabel_6.gridy = 0;
			pnSouth.add(getLblNewLabel_6(), gbc_lblNewLabel_6);
			GridBagConstraints gbc_tfStatus = new GridBagConstraints();
			gbc_tfStatus.fill = GridBagConstraints.HORIZONTAL;
			gbc_tfStatus.insets = new Insets(0, 0, 5, 5);
			gbc_tfStatus.gridx = 3;
			gbc_tfStatus.gridy = 0;
			pnSouth.add(getTfStatus(), gbc_tfStatus);
			GridBagConstraints gbc_lbUpdate = new GridBagConstraints();
			gbc_lbUpdate.insets = new Insets(0, 0, 5, 0);
			gbc_lbUpdate.anchor = GridBagConstraints.WEST;
			gbc_lbUpdate.gridx = 10;
			gbc_lbUpdate.gridy = 0;
			pnSouth.add(getLbUpdate(), gbc_lbUpdate);
			GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
			gbc_lblNewLabel_7.insets = new Insets(0, 0, 0, 5);
			gbc_lblNewLabel_7.gridx = 0;
			gbc_lblNewLabel_7.gridy = 1;
			pnSouth.add(getLblNewLabel_7(), gbc_lblNewLabel_7);
			GridBagConstraints gbc_panel_2 = new GridBagConstraints();
			gbc_panel_2.insets = new Insets(0, 0, 0, 5);
			gbc_panel_2.fill = GridBagConstraints.BOTH;
			gbc_panel_2.gridx = 1;
			gbc_panel_2.gridy = 1;
			pnSouth.add(getPanel_2(), gbc_panel_2);
			GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
			gbc_btnNewButton_1.insets = new Insets(0, 0, 0, 5);
			gbc_btnNewButton_1.gridx = 3;
			gbc_btnNewButton_1.gridy = 1;
			pnSouth.add(getBtnNewButton_1(), gbc_btnNewButton_1);
		}
		return pnSouth;
	}

	private JLabel getLbUpdate() {
		if (lbUpdate == null) {
			lbUpdate = new JLabel("Update");
			lbUpdate.setForeground(Color.GRAY);
		}
		return lbUpdate;
	}

	private JPanel getPanel_1() {
		if (panel_1 == null) {
			panel_1 = new JPanel();
			panel_1.add(getRdbtnCoils());
			panel_1.add(getRdbtnDiscrete());
			panel_1.add(getRdbtnHolding());
			panel_1.add(getRdbtnInput());
		}
		return panel_1;
	}

	private JRadioButton getRdbtnCoils() {
		if (rdbtnCoils == null) {
			rdbtnCoils = new JRadioButton(" Coils R/W");
			rdbtnCoils.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					changedModbusType();
				}
			});
			buttonGroupModbusType.add(rdbtnCoils);
			rdbtnCoils.setMnemonic('1');
			rdbtnCoils.setDisplayedMnemonicIndex(1);
			rdbtnCoils.setSelected(true);
		}
		return rdbtnCoils;
	}

	private JRadioButton getRdbtnDiscrete() {
		if (rdbtnDiscrete == null) {
			rdbtnDiscrete = new JRadioButton("Discrete R");
			rdbtnDiscrete.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					changedModbusType();
				}
			});
			buttonGroupModbusType.add(rdbtnDiscrete);
			rdbtnDiscrete.setMnemonic('2');
			rdbtnDiscrete.setDisplayedMnemonicIndex(2);
		}
		return rdbtnDiscrete;
	}

	private JRadioButton getRdbtnHolding() {
		if (rdbtnHolding == null) {
			rdbtnHolding = new JRadioButton("Holding R/W");
			rdbtnHolding.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					changedModbusType();
				}
			});
			buttonGroupModbusType.add(rdbtnHolding);
			rdbtnHolding.setMnemonic('3');
			rdbtnHolding.setDisplayedMnemonicIndex(3);
		}
		return rdbtnHolding;
	}

	private JRadioButton getRdbtnInput() {
		if (rdbtnInput == null) {
			rdbtnInput = new JRadioButton("Input R");
			rdbtnInput.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					changedModbusType();
				}
			});
			buttonGroupModbusType.add(rdbtnInput);
			rdbtnInput.setMnemonic('4');
			rdbtnInput.setDisplayedMnemonicIndex(4);
		}
		return rdbtnInput;
	}

	private JLabel getLblNewLabel_4() {
		if (lblNewLabel_4 == null) {
			lblNewLabel_4 = new JLabel("Slave ID");
		}
		return lblNewLabel_4;
	}

	private JTextField getTfSlaveID() {
		if (tfSlaveID == null) {
			tfSlaveID = new JTextField();
			tfSlaveID.setEnabled(false);
			tfSlaveID.setText("1");
			tfSlaveID.setColumns(10);
		}
		return tfSlaveID;
	}

	private JLabel getLblNewLabel_5() {
		if (lblNewLabel_5 == null) {
			lblNewLabel_5 = new JLabel("Number");
		}
		return lblNewLabel_5;
	}

	private JTextField getTfRegisterNumber() {
		if (tfRegisterNumber == null) {
			tfRegisterNumber = new JTextField();
			tfRegisterNumber.setName("NumberRegister");
			tfRegisterNumber.setText("16");
			tfRegisterNumber.setColumns(10);
			tfRegisterNumber.setInputVerifier(new AddressVerifier());
		}
		return tfRegisterNumber;
	}

	private JButton getBtSnapshot() {
		if (btSnapshot == null) {
			btSnapshot = new JButton("Snapshot");
			btSnapshot.setVisible(false);
			btSnapshot.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					updateModbusData(Reason_Regular_Update);
				}
			});
		}
		return btSnapshot;
	}

	private JButton getBtDisconnect() {
		if (btDisconnect == null) {
			btDisconnect = new JButton("Disconnect");
			btDisconnect.setVisible(false);
			btDisconnect.setEnabled(false);
			btDisconnect.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					invokeDisconnect();
				}
			});
		}
		return btDisconnect;
	}

	private JLabel getLblNewLabel_6() {
		if (lblNewLabel_6 == null) {
			lblNewLabel_6 = new JLabel("Status");
			lblNewLabel_6.setVisible(false);
		}
		return lblNewLabel_6;
	}

	private JTextField getTfStatus() {
		if (tfStatus == null) {
			tfStatus = new JTextField();
			tfStatus.setVisible(false);
			tfStatus.setEditable(false);
			tfStatus.setText("Disconnected");
			tfStatus.setColumns(10);
		}
		return tfStatus;
	}

	private JCheckBox getChckbxStart1() {
		if (chckbxStart1 == null) {
			chckbxStart1 = new JCheckBox("Start \"1\"");
			chckbxStart1.setEnabled(false);
			chckbxStart1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if (modbusClient != null) {
						modbusClient.setStartWith1(getChckbxStart1().isSelected());
					}

				}
			});
		}
		return chckbxStart1;
	}

	private JLabel getLblNewLabel_7() {
		if (lblNewLabel_7 == null) {
			lblNewLabel_7 = new JLabel("Messages");
		}
		return lblNewLabel_7;
	}

	private JPanel getPanel_2() {
		if (panel_2 == null) {
			panel_2 = new JPanel();
			panel_2.setLayout(new BorderLayout(0, 0));
			panel_2.add(getScrollPane_1(), BorderLayout.CENTER);
		}
		return panel_2;
	}

	private JScrollPane getScrollPane_1() {
		if (scrollPane_1 == null) {
			scrollPane_1 = new JScrollPane();
			scrollPane_1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			scrollPane_1.setViewportView(getTextArea());
		}
		return scrollPane_1;
	}

	private JTextArea getTextArea() {
		if (textArea == null) {
			textArea = new JTextArea();
			textArea.setRows(8);
		}
		return textArea;
	}

	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(getTable());
		}
		return scrollPane;
	}

	private JButton getBtnNewButton_1() {
		if (btnNewButton_1 == null) {
			btnNewButton_1 = new JButton("Clear");
			btnNewButton_1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					getTextArea().setText(null);
				}
			});
		}
		return btnNewButton_1;
	}

	private JCheckBox getChckbxDisplayHex() {
		if (chckbxDisplayHex == null) {
			chckbxDisplayHex = new JCheckBox("Hex");
		}
		return chckbxDisplayHex;
	}

	private class AddressVerifier extends InputVerifier {
		@Override
		public boolean verify(JComponent arg0) {
			JTextField tf = null;
			try {
				tf = (JTextField) arg0;
				String text = tf.getText();
				int address = Integer.valueOf(text);
				int min = 0;
				int max = 65535;

				if ("StartRegister".equals(tf.getName())) {

					if (address < min || address > max) {
						addMessage("Start address must be =>" + min + " and <=" + max);
						tf.setBackground(Color.RED);
						return false;
					}
				}

				if ("NumberRegister".equals(tf.getName())) {
					min = 0;
					max = 2000;
					int type = buttonGroupModbusType.getSelection().getMnemonic() - 48;
					if (type >= 3) {
						max = 125;
					}

					if (address < min || address > max) {
						addMessage("Number must be =>" + min + " and <=" + max);
						tf.setBackground(Color.RED);
						return false;
					}

				}

			} catch (Exception e) {
				addMessage("Please enter a valid number");
				tf.setBackground(Color.RED);
				return false;
			}

			tf.setBackground(Color.WHITE);
			return true;
		}
	};

	private JTextField getTextFieldSelected() {
		if (textFieldSelected == null) {
			textFieldSelected = new JTextField();
			textFieldSelected.setBorder(new LineBorder(new Color(171, 173, 179), 3, true));
			textFieldSelected.setEditable(false);
			textFieldSelected.setColumns(10);
		}
		return textFieldSelected;
	}
	private JLabel getLblNewLabel_8() {
		if (lblNewLabel_8 == null) {
			lblNewLabel_8 = new JLabel("Selected Address");
		}
		return lblNewLabel_8;
	}
}
