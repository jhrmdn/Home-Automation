package mdn.jh.automation.device.modbus;

import java.util.GregorianCalendar;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusNumberException;
import com.intelligt.modbus.jlibmodbus.exception.ModbusProtocolException;
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;

public class ModbusDataTable extends DefaultTableModel implements TableModel {

	private static final long serialVersionUID = 6588733074436766414L;
	public static final int ModbusType_Coil_RW_1_bit = 1;
	public static final int ModbusType_Discrete_RO_1_bit = 2;
	public static final int ModbusType_Holding_RW_16_bit = 3;
	public static final int ModbusType_Input_RO_16_bit = 4;

	ModbusMaster modbusMaster = null;

	boolean updateInProgess = false;
	// ModbusDataTableModel t = null;

	// private String host = null;
	// private int port = 0;
	private int slaveID;
	private int modbusType = ModbusType_Coil_RW_1_bit;
	private int numberRegister = 0;
	private long lastUpdateTime = -1;
	private ModbusSelectorWindow myMainWindow = null;
	private boolean startWith1 = false;

	public boolean isStartWith1() {
		return startWith1;
	}

	public void setStartWith1(boolean startWith1) {
		this.startWith1 = startWith1;
	}

	public ModbusDataTable() {

	}

	/**
	 * 
	 * @return true if the modbus type is discrete or holding
	 */
	public boolean is_modbus_type_16_bit() {
		if (modbusType == ModbusType_Input_RO_16_bit || modbusType == ModbusType_Holding_RW_16_bit) {
			return true;
		}
		return false;
	}

	public ModbusDataTable(ModbusMaster modbusMaster, int slaveID, ModbusSelectorWindow mainWindow) {
		this.modbusMaster = modbusMaster;
		this.slaveID = slaveID;
		this.myMainWindow = mainWindow;
	}

	/*
	 * public ModbusDataTable(String host, int port, int slaveID,
	 * ModbusDataSourceCreator mainWindow) { this.host = host; this.port = port;
	 * this.slaveID = slaveID; this.myMainWindow = mainWindow;
	 * 
	 * }
	 */
	public int getNumberRegister() {
		return numberRegister;
	}

	boolean[] booleanRegisterValues = null;
	int startAdress = 0;

	int[] holdingRegisterValues = null;

	public boolean isConnected() {
		if (modbusMaster == null) {
			return false;
		}
		return modbusMaster.isConnected();
	}

	public void disconnect() {
		if (modbusMaster == null) {
			return;
		}
		try {
			modbusMaster.disconnect();
		} catch (ModbusIOException e) {
		}
	}

	public int getModbusType() {
		return modbusType;
	}

	public void setModbusType(int modbusType) {
		this.modbusType = modbusType;
	}

	public boolean isUpdateInProgess() {
		while (updateInProgess) {
			try {
				Thread.sleep(10);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return true;
	}

	private void setLastUpdateTimestamp() {
		lastUpdateTime = new GregorianCalendar().getTimeInMillis();
	}

	private void setUpdateInProgess(boolean updateInProgess) {
		this.updateInProgess = updateInProgess;
	}

	/*
	 * public void connect() {
	 * 
	 * TcpParameters tcpParameters = new TcpParameters();
	 * 
	 * try { tcpParameters.setHost(InetAddress.getByName(host));
	 * tcpParameters.setPort(port); } catch (UnknownHostException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); }
	 * tcpParameters.setKeepAlive(true); Main.getLogger().log(Level.INFO,
	 * "Connect to Modbus: " + tcpParameters.getHost() + " Port: " +
	 * tcpParameters.getPort());
	 * 
	 * modbusMaster = ModbusMasterFactory.createModbusMasterTCP(tcpParameters);
	 * Modbus.setAutoIncrementTransactionId(true);
	 * 
	 * }
	 */
	public CoilValues getCoilRegisterValues() {
		CoilValues v = new CoilValues();
		if (booleanRegisterValues == null) {
			return v;
		}
		v.setRegisterValues(booleanRegisterValues);
		v.setStart(startAdress);
		v.setValid(true);
		v.setTimestamp(lastUpdateTime);
		return v;
	}

	public HoldingRegisterValues getHoldingRegisterValues() {
		HoldingRegisterValues v = new HoldingRegisterValues();
		if (holdingRegisterValues == null) {
			return v;
		}
		v.setRegisterValues(holdingRegisterValues);
		v.setStart(startAdress);
		v.setTimestamp(lastUpdateTime);
		v.setValid(true);
		return v;
	}

	/*
	 * public ModbusDataTableModel getTableModel() { t = new
	 * ModbusDataTableModel(this); return t; }
	 */

	public boolean update(int start, int numberRegister, int modbusType) {
		if (modbusMaster == null)
			return false;

		setUpdateInProgess(true);
		startAdress = start;
		this.numberRegister = numberRegister;

		this.modbusType = modbusType;
		try {

			int slaveId = slaveID;
			try {
				if (modbusMaster == null || !modbusMaster.isConnected()) {
					// connect();
				}

				if (modbusType == ModbusType_Coil_RW_1_bit) {
					booleanRegisterValues = modbusMaster.readCoils(slaveId, startAdress, numberRegister);
				}
				if (modbusType == ModbusType_Holding_RW_16_bit) {
					holdingRegisterValues = modbusMaster.readHoldingRegisters(slaveId, startAdress, numberRegister);
				}
				if (modbusType == ModbusType_Discrete_RO_1_bit) {
					booleanRegisterValues = modbusMaster.readDiscreteInputs(slaveId, startAdress, numberRegister);
				}
				if (modbusType == ModbusType_Input_RO_16_bit) {
					holdingRegisterValues = modbusMaster.readInputRegisters(slaveId, startAdress, numberRegister);
				}

			} catch (ModbusProtocolException e) {
				// myMainWindow.addMessage(e.getLocalizedMessage());
				// e.printStackTrace();

				return false;
			} catch (ModbusNumberException e) {
				// myMainWindow.addMessage(e.getLocalizedMessage());
				// e.printStackTrace();
				return false;
			} catch (ModbusIOException e) {
				// myMainWindow.addMessage(e.getLocalizedMessage());
				// e.printStackTrace();
				return false;
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		setLastUpdateTimestamp();
		setUpdateInProgess(false);
		// fireTableDataChanged();
		return true;
	}

	/**
	 * For Debug Purposes
	 * 
	 * @param start
	 * @param registerValues
	 */
	public void printRegisterValues(int start, boolean[] registerValues) {
		int bits = 0;
		System.out.print("\n\nBit             ");
		for (int i = 0; i < 16; i++) {
			String b = "" + i;
			while (b.length() < 2) {
				b = "0" + b;
			}
			System.out.print(b + "| ");

		}

		String reg = "" + start;
		while (reg.length() < 4) {
			reg = "0" + reg;
		}

		System.out.print("\nRegister " + reg + ":  ");
		for (int i = 0; i < registerValues.length; i++) {
			if (bits == 16) {
				start++;
				bits = 0;
				reg = "" + start;
				while (reg.length() < 4) {
					reg = "0" + reg;
				}

				System.out.print("\nRegister " + reg + ":  ");
			}

			String print = "0";

			if (registerValues[i]) {
				print = "1";
			}

			// System.out.print("["+bits+"] "+print+" | ");
			System.out.print(print + " | ");
			bits++;
		}
	}

	/// ##################################################################

	boolean valid = false;

	@Override
	public int getColumnCount() {
		return 18;
	}

	@Override
	public String getColumnName(int col) {
		if (col == 0) {
			return "Timestamp";
		}
		if (col == 1) {
			return "First Reg";
		}
		int offset = col - 2;
		return "+" + offset;

	}

	@Override
	public int getRowCount() {
		if (getNumberRegister() <= 16) {
			return 1;
		}
		return (getNumberRegister() + 15) / 16;
	}

	@Override
	public Object getValueAt(int row, int col) {
		try {

			// boolean
			if (!is_modbus_type_16_bit()) {
				CoilValues coilRegisterValues = getCoilRegisterValues();

				if (!coilRegisterValues.isValid()) {
					return "###";
				}
				if (col == 0) {
					return coilRegisterValues.timestamp_string;
					// return coilRegisterValues.timestamp;
				} else if (col == 1) {
					int start = 16 * row + coilRegisterValues.start;
					if (isStartWith1()) {
						start++;
					}
					return start;
				} else {

					int bit = row * 16 + col - 2;
					if (bit >= getNumberRegister()) {
						return "-";
					}
					return coilRegisterValues.registerValues[bit];
				}
			}

			// 16 bit int values
			if (is_modbus_type_16_bit()) {
				HoldingRegisterValues holdingRegisterValues = getHoldingRegisterValues();
				if (!holdingRegisterValues.isValid()) {
					return "###";
				}
				if (col == 0) {
					return holdingRegisterValues.getTimestamp_string();
				} else if (col == 1) {
					int start = row * 16 + holdingRegisterValues.start;
					if (isStartWith1()) {
						start++;
					}
					return start;
					// return row + holdingRegisterValues.start;
				} else {
					int register = row * 16 + col - 2;
					if (register >= getNumberRegister()) {
						return "-";
					}
					int value = holdingRegisterValues.registerValues[register];
					String val = null;
					if (myMainWindow.displayValuesAsHex()) {
						val = "0x" + Integer.toHexString(value);
					} else {
						val = "" + value;
					}

					return val;
				}

			}

		} catch (Exception e) {
			// TODO: handle exception

		}

		return null;
	}

	@Override
	public boolean isCellEditable(int arg0, int arg1) {

		/*
		 * if (arg1 <= 1) { return false; }
		 * 
		 * if (getModbusType() == ModbusType_Holding_RW_16_bit || getModbusType() ==
		 * ModbusType_Coil_RW_1_bit) { return true;
		 * 
		 * }
		 */

		return false;

	}

	@Override
	public void setValueAt(Object arg0, int row, int col) {

		if (arg0 == null) {
			System.out.println("SetValue is Null");
			return;
		}

		// System.out.println("Write Address:" + address + " - Start: "+ startAdress +"
		// - row "+row+ " - col " + col + " - ID: "+slaveID);
		int address = -1;
		address = startAdress + row * 16 + col - 2;
		int dispaddr = address;

		if (isStartWith1()) {
			dispaddr++;
		}

		if (is_modbus_type_16_bit()) {

			int reg = -1;

			if (myMainWindow.displayValuesAsHex()) {
				String hex = arg0.toString();
				hex = hex.replace("0x", "");
				reg = Integer.parseInt(hex, 16);
				myMainWindow.addMessage("Write Register:" + dispaddr + " - Value: 0x" + hex + " / Dec:" + reg);
				// System.out.println("Hex/Int: "+reg);
			} else {
				reg = Integer.valueOf(arg0.toString());
				myMainWindow.addMessage("Write Register:" + dispaddr + " - Value: " + reg);
			}

		//	modbusMaster.writeSingleRegister(slaveID, address, reg);

		} else {

			boolean v = true;
			if ("false".equals(arg0.toString())) {
				v = false;
			}

			myMainWindow.addMessage("Write coil:" + dispaddr + " - Value: " + v);

			boolean[] booleanRegisterValues = { v };
		//	modbusMaster.writeMultipleCoils(slaveID, address, booleanRegisterValues);
			// m.writeSingleCoil(slaveID, address, v);
		}

	}

}
