package mdn.jh.automation.gui;

import java.awt.Color;
import java.util.logging.Level;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import mdn.jh.automation.Main;
import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.DataInputIF;
import mdn.jh.automation.io.DataOutputIF;

public class InputPanelRow extends JPanel {

	private static final long serialVersionUID = -2336629804357968791L;
	private JCheckBox chckbxNewCheckBox;
	private JLabel lblNewLabel;
	private JTextField txtUnused;
	private JLabel lblType;
	boolean inputInUse = false;
	private JLabel lblNewLabel_1;
	private DataInputIF input = null;
	private int id = -1;
	private JCheckBox chckbxValid;

	public InputPanelRow(DataInputIF input, int inputID) {
		
		this.input = input;
		this.id = inputID;
		initialize();
		
		if (input == null) {
			Main.getLogger().log(Level.WARNING, "input is null");
			getTxtUnused().setBackground(Color.RED);
			return;
		}
		
		if(inputID >= input.getInputDefinitions().length) {
			setBackground(Color.RED);
			getTxtUnused().setText("INVALID input ID: "+inputID);
			return;
		}
		
		
		String name = input.getInputDefinitions()[inputID].getName();

		getChckbxNewCheckBox().setText(name);
		inputInUse = input.isInputInUse(inputID);
		getChckbxNewCheckBox().setSelected(inputInUse);
		add(getChckbxValid());

		DataOutputIF out = input.getInputData(inputID);
		if (out != null) {
			int type=input.getInputDefinitions()[inputID].getDataType();
			getLblType().setText(DataComponentStub.getTypeAsString(type));
		} else {
			getLblType().setText("[Unused] " + DataComponentStub.getTypeAsString(input.getInputDefinitions()[inputID].getDataType())) ; 
			;
		}
		update();
	}

	private void initialize() {
		add(getChckbxNewCheckBox());
		add(getLblNewLabel_1());
		add(getLblType());
		add(getLblNewLabel());
		add(getTxtUnused());
	}

	public void update() {
		// System.out.println("Update: "+id+" - "+inputInUse+ " - "+input);
		
		if (input == null || !inputInUse)
			return;

		DataOutputIF out = input.getInputData(id);
		if (out != null) {
			getTxtUnused().setText(out.getOutputAsString());
			getChckbxValid().setSelected(out.isDataValid());
		} else {
			getTxtUnused().setText("---");
		}
	}

	/*
	 * public InputPanelRow(LayoutManager layout) { super(layout); // TODO
	 * Auto-generated constructor stub }
	 * 
	 * public InputPanelRow(boolean isDoubleBuffered) { super(isDoubleBuffered); //
	 * TODO Auto-generated constructor stub }
	 * 
	 * public InputPanelRow(LayoutManager layout, boolean isDoubleBuffered) {
	 * super(layout, isDoubleBuffered); // TODO Auto-generated constructor stub }
	 */
	private JCheckBox getChckbxNewCheckBox() {
		if (chckbxNewCheckBox == null) {
			chckbxNewCheckBox = new JCheckBox("In");
			chckbxNewCheckBox.setEnabled(false);
		}
		return chckbxNewCheckBox;
	}

	private JLabel getLblNewLabel() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel("Value:");
		}
		return lblNewLabel;
	}

	private JTextField getTxtUnused() {
		if (txtUnused == null) {
			txtUnused = new JTextField();
			txtUnused.setEditable(false);
			txtUnused.setColumns(10);
		}
		return txtUnused;
	}

	private JLabel getLblType() {
		if (lblType == null) {
			lblType = new JLabel("Not in Use");
		}
		return lblType;
	}

	private JLabel getLblNewLabel_1() {
		if (lblNewLabel_1 == null) {
			lblNewLabel_1 = new JLabel("Type:");
		}
		return lblNewLabel_1;
	}
	private JCheckBox getChckbxValid() {
		if (chckbxValid == null) {
			chckbxValid = new JCheckBox("Valid");
			chckbxValid.setEnabled(false);
		}
		return chckbxValid;
	}
}
