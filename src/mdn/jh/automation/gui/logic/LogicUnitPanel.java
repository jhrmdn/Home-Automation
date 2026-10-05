package mdn.jh.automation.gui.logic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import mdn.jh.automation.gui.DataComponentPanel;
import mdn.jh.automation.gui.InputSelectorPopup;
import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.DataUpdateListenerIF;
import mdn.jh.automation.io.logic.DataInputConnection;
import mdn.jh.automation.io.logic.LogicBase;
import mdn.jh.automation.io.logic.InputDefinition;

public class LogicUnitPanel extends DataComponentPanel implements DataUpdateListenerIF {
	private static final long serialVersionUID = 4297342569844515699L;
	private JPanel panelTop;
	private JLabel lblType;
	LogicBase myLogicBase = null;
	private JLabel lbName;
	private JPanel panel_outputs;
	private JPanel panel_details;
	private JLabel lblNewLabel_1;
	private JTextField tfValue;
	private JPanel panel_in;
	private JLabel lblInputType;
	private JLabel lblOutputType;
	private JPanel panel;
	private JButton btnDetails;
	private JLabel lblDataValid;
	private Vector<JLabel> inputLabels = new Vector<JLabel>();

	public LogicBase getMyLogicBase() {
		return myLogicBase;
	}

	private void showDetailDialog() {
		LogicUnitDetailsPanel logicUnitDetailsPanel = new LogicUnitDetailsPanel();
		logicUnitDetailsPanel.setLogicUnit(myLogicBase);

		logicUnitDetailsPanel.setVisible(true);
	}

	@Override
	public Vector<DataInputConnection> getIDsConnectTo() {
		if (myLogicBase == null)
			return null;
		return myLogicBase.getMyOutputs();

	}

	@Override
	public int getMaximumNumberOfInputs() {
		if (myLogicBase == null) {
			return 0;
		}
		if (myLogicBase.getInputDefinitions() == null)
			return 0;
		return myLogicBase.getInputDefinitions().length;
	}

	@Override
	public int getMyID() {
		if (myLogicBase == null)
			return -1;
		return myLogicBase.getId();
	}

	public void setMyLogicBase(LogicBase logicBase) {
		this.myLogicBase = logicBase;
		if (logicBase == null)
			return;
		logicBase.subscribeUpdateListener(this);
		// getLbName().setText(logicBase.getLogicName() + " ID:" + logicBase.getId());
		getLbName().setText("ID:" + logicBase.getId());
		getLblType().setText(logicBase.getLogicComponentDescription().getName());
		showInputLabels(logicBase.getInputDefinitions());

		// TODO for all inputs show type
		/*
		 * char[] ina = logicBase.getDataTypeInputAsString().toCharArray(); String ins =
		 * "<HTML>";
		 * 
		 * for (int i = 0; i < ina.length; i++) { ins = ins + ina[i] + "<br>"; } ins +=
		 * "</HTML>"; getLblInputType().setText(ins);
		 */

		char[] oa = logicBase.getDataTypeOutputAsString().toCharArray();
		String os = "<HTML>";

		for (int i = 0; i < oa.length; i++) {
			os = os + oa[i] + "<br>";
		}
		os += "</HTML>";
		getLblOutputType().setText(os);

		logicBase.setBounds(this.getBounds());
	}

	private void showInputLabels(InputDefinition[] definitions) {
		panel_in.removeAll();
		inputLabels.clear();
		if (definitions == null || definitions.length == 0) {
			panel_in.add(getLblInputType());
		} else {
			panel_in.setLayout(new GridLayout(definitions.length, 1, 0, 4));
			for (InputDefinition definition : definitions) {
				JLabel label = new JLabel(definition.getName());
				label.setFont(new Font("Tahoma", Font.BOLD, 11));
				inputLabels.add(label);
				panel_in.add(label);
			}
		}
		panel_in.revalidate();
		panel_in.repaint();
	}

	@Override
	public int getInputConnectionY(int inputNumber) {
		if (inputNumber >= 0 && inputNumber < inputLabels.size()) {
			JLabel label = inputLabels.get(inputNumber);
			if (label.getHeight() > 0) {
				return SwingUtilities.convertPoint(label, 0, label.getHeight() / 2, this).y;
			}
		}
		return super.getInputConnectionY(inputNumber);
	}

	private void setValid(boolean valid) {		
		if(valid) {
			getLblDataValid().setText("Data Valid");
			getLblDataValid().setBackground(Color.green);
		}else {
			getLblDataValid().setText("Data Invalid");
			getLblDataValid().setBackground(Color.red);
		}
	}
	
	@Override
	public void dataUpdateFired(DataComponentStub data) {
		
		getTfValue().setText("" + myLogicBase.getMyState().getOutputAsString());
		setValid(myLogicBase.isDataValid());
	}

	public LogicUnitPanel() {
		// TODO Auto-generated constructor stub
		initialize();
	}

	/**
	 * 1=source, 2=target
	 * 
	 * @param selector
	 */
	public void setSourceOrTarget(int selector) {
		if (copyPasteListener == null)
			return;
		if (selector == SELECTOR_SOURCE) {
			copyPasteListener.setSourceComponent(this.getMyLogicBase());
		}
		if (selector == SELECTOR_SINK) {
			InputSelectorPopup pop = new InputSelectorPopup();
			pop.setInputs(getMyLogicBase());
			pop.setModal(true);
			pop.setVisible(true);
			if (pop.getSelectedInput() == -1) {
				return;
			}
			copyPasteListener.setTargetComponent(this.getMyLogicBase(), pop.getSelectedInput());
		}
		if (selector == SELECTOR_CANCEL) {
			copyPasteListener.cancelSelection();
		}
	}

	private void initialize() {
		setPreferredSize(new Dimension(260, 230));
		setBorder(new LineBorder(new Color(0, 0, 0)));
		setLayout(new BorderLayout(0, 0));
		add(getPanelTop(), BorderLayout.NORTH);
		add(getPanel_outputs(), BorderLayout.EAST);
		add(getPanel_details(), BorderLayout.CENTER);

		add(getPanel_1_1(), BorderLayout.WEST);
		add(getPanel_1(), BorderLayout.SOUTH);
	}

	public LogicUnitPanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public LogicUnitPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public LogicUnitPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	private JPanel getPanelTop() {
		if (panelTop == null) {
			panelTop = new JPanel();
			GridBagLayout gbl_panelTop = new GridBagLayout();
			gbl_panelTop.columnWidths = new int[] { 118, 0 };
			gbl_panelTop.rowHeights = new int[] { 0, 14, 0 };
			gbl_panelTop.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
			gbl_panelTop.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
			panelTop.setLayout(gbl_panelTop);
			GridBagConstraints gbc_lblType = new GridBagConstraints();
			gbc_lblType.fill = GridBagConstraints.BOTH;
			gbc_lblType.insets = new Insets(0, 0, 5, 0);
			gbc_lblType.gridx = 0;
			gbc_lblType.gridy = 0;
			panelTop.add(getLblType(), gbc_lblType);
			GridBagConstraints gbc_lbName = new GridBagConstraints();
			gbc_lbName.fill = GridBagConstraints.BOTH;
			gbc_lbName.gridx = 0;
			gbc_lbName.gridy = 1;
			panelTop.add(getLbName(), gbc_lbName);
		}
		return panelTop;
	}

	private JLabel getLblType() {
		if (lblType == null) {
			lblType = new JLabel("Logic");
		}
		return lblType;
	}

	private JLabel getLbName() {
		if (lbName == null) {
			lbName = new JLabel("New label");
		}
		return lbName;
	}

	private JPanel getPanel_outputs() {
		if (panel_outputs == null) {
			panel_outputs = new JPanel();
			panel_outputs.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			panel_outputs.add(getLblOutputType());
		}
		return panel_outputs;
	}

	private JPanel getPanel_details() {
		if (panel_details == null) {
			panel_details = new JPanel();
			GridBagLayout gbl_panel_details = new GridBagLayout();
			gbl_panel_details.columnWidths = new int[] { 22, 96, 0 };
			gbl_panel_details.rowHeights = new int[] { 0, 0, 0 };
			gbl_panel_details.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gbl_panel_details.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
			panel_details.setLayout(gbl_panel_details);
			GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
			gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_1.gridx = 0;
			gbc_lblNewLabel_1.gridy = 0;
			panel_details.add(getLblNewLabel_1(), gbc_lblNewLabel_1);
			GridBagConstraints gbc_tfValue = new GridBagConstraints();
			gbc_tfValue.insets = new Insets(0, 0, 5, 0);
			gbc_tfValue.fill = GridBagConstraints.HORIZONTAL;
			gbc_tfValue.gridx = 1;
			gbc_tfValue.gridy = 0;
			panel_details.add(getTfValue(), gbc_tfValue);
		}
		return panel_details;
	}

	private JLabel getLblNewLabel_1() {
		if (lblNewLabel_1 == null) {
			lblNewLabel_1 = new JLabel("Value");
		}
		return lblNewLabel_1;
	}

	private JTextField getTfValue() {
		if (tfValue == null) {
			tfValue = new JTextField();
			tfValue.setEditable(false);
			tfValue.setColumns(10);
		}
		return tfValue;
	}

	private JPanel getPanel_1_1() {
		if (panel_in == null) {
			panel_in = new JPanel();
			panel_in.add(getLblInputType());
		}
		return panel_in;
	}

	private JLabel getLblInputType() {
		if (lblInputType == null) {
			lblInputType = new JLabel("I");
			lblInputType.setFont(new Font("Tahoma", Font.PLAIN, 7));
		}
		return lblInputType;
	}

	private JLabel getLblOutputType() {
		if (lblOutputType == null) {
			lblOutputType = new JLabel("O");
			lblOutputType.setFont(new Font("Tahoma", Font.PLAIN, 7));
		}
		return lblOutputType;
	}

	@Override
	protected void storeBoundsWhenChanged() {
		myLogicBase.setBounds(getBounds());

	}

	private JPanel getPanel_1() {
		if (panel == null) {
			panel = new JPanel();
			panel.add(getBtnDetails());
			panel.add(getLblDataValid());
		}
		return panel;
	}

	private JButton getBtnDetails() {
		if (btnDetails == null) {
			btnDetails = new JButton("Details");
			btnDetails.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showDetailDialog();
				}
			});
		}
		return btnDetails;
	}
	private JLabel getLblDataValid() {
		if (lblDataValid == null) {
			lblDataValid = new JLabel("Data Valid");
		}
		return lblDataValid;
	}
}
