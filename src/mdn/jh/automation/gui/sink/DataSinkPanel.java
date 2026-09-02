package mdn.jh.automation.gui.sink;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import mdn.jh.automation.gui.DataComponentPanel;
import mdn.jh.automation.gui.InputSelectorPopup;
import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.DataUpdateListenerIF;
import mdn.jh.automation.io.logic.DataInputConnection;
import mdn.jh.automation.io.sink.DataSink;

public class DataSinkPanel extends DataComponentPanel implements DataUpdateListenerIF {
	private static final long serialVersionUID = 4297342569844515699L;
	private JPanel panelNorth;
	private JLabel lblType;
	private DataSink myDataSink = null;
	private JLabel lbName;
	private JPanel panel_outputs;
	private JPanel panel;
	private JTextArea textAreaStatus;
	private JButton btnShowDetails;

	public DataSink getMyDataSink() {
		return myDataSink;
	}

	@Override
	public Vector<DataInputConnection> getIDsConnectTo() {
		return null;
	}

	private void showDetailDialog() {
		DataSinkDetailsPanel logicUnitDetailsPanel = new DataSinkDetailsPanel();
		logicUnitDetailsPanel.setDataSink(myDataSink);
		logicUnitDetailsPanel.setVisible(true);
	}

	@Override
	public int getMyID() {
		if (myDataSink == null)
			return -1;
		return myDataSink.getId();
	}

	public void setMyDataSink(DataSink myDataSink) {
		this.myDataSink = myDataSink;
		if (myDataSink == null)
			return;
		myDataSink.subscribeUpdateListener(this);
		getLbName().setText(myDataSink.getSinkName() + " ID:" + myDataSink.getId());
		// getLblType().setText("Data Sink (" + myDataSource.getD() + ")");
		// getTfSourceData().setText(myDataSource.get().getParameter1());
		this.myDataSink.setBounds(getBounds());
	}

	@Override

	public void dataUpdateFired(DataComponentStub data) {
		if (myDataSink == null) {
			setValid(false);
			return;
		}
		getTextAreaStatus().setText(myDataSink.getStatusMessage());
		setValid(myDataSink.isDataValid());
		getTextFieldValue().setText(myDataSink.getOutputDataValue().getOutputAsString());

	}

	private void setValid(boolean valid) {
		if (valid) {
			getLblDataValid().setText("Data Valid");
			getTextAreaStatus().setBackground(Color.GREEN);
		} else {
			getLblDataValid().setText("Data Invalid");
			getTextAreaStatus().setBackground(Color.MAGENTA);
		}
	}

	private JLabel lblDataValid;
	private JLabel lblNewLabel;
	private JTextField textFieldValue;

	public DataSinkPanel() {
		// TODO Auto-generated constructor stub
		initialize();
		getMntmStartConnection().setVisible(false);
		// getMntmEndConnection().setVisible(false);
	}

	@Override
	public int getMaximumNumberOfInputs() {
		if (myDataSink == null) {
			return 0;
		}
		if (myDataSink.getInputDefinitions() == null)
			return 0;
		return myDataSink.getInputDefinitions().length;
	}

	@Override
	protected void setSourceOrTarget(int selector) {
		if (copyPasteListener == null)
			return;

		if (selector == SELECTOR_SINK) {
			InputSelectorPopup pop = new InputSelectorPopup();
			pop.setInputs(getMyDataSink());
			pop.setModal(true);
			pop.setVisible(true);
			if (pop.getSelectedInput() == -1) {
				return;
			}
			copyPasteListener.setTargetComponent(getMyDataSink(), pop.getSelectedInput());
		}
		if (selector == SELECTOR_CANCEL) {
			copyPasteListener.cancelSelection();
		}

	}

	private void initialize() {
		setPreferredSize(new Dimension(174, 150));
		setBorder(new LineBorder(new Color(0, 0, 0)));
		setLayout(new BorderLayout(0, 0));
		add(getPanelNorth(), BorderLayout.NORTH);
		add(getPanel_outputs(), BorderLayout.SOUTH);
		add(getPanel(), BorderLayout.CENTER);

	}

	public DataSinkPanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public DataSinkPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public DataSinkPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	private JPanel getPanelNorth() {
		if (panelNorth == null) {
			panelNorth = new JPanel();
			GridBagLayout gbl_panelNorth = new GridBagLayout();
			gbl_panelNorth.columnWidths = new int[] { 118, 0 };
			gbl_panelNorth.rowHeights = new int[] { 0, 14, 0 };
			gbl_panelNorth.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
			gbl_panelNorth.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
			panelNorth.setLayout(gbl_panelNorth);
			GridBagConstraints gbc_lblType = new GridBagConstraints();
			gbc_lblType.fill = GridBagConstraints.BOTH;
			gbc_lblType.insets = new Insets(0, 0, 5, 0);
			gbc_lblType.gridx = 0;
			gbc_lblType.gridy = 0;
			panelNorth.add(getLblType(), gbc_lblType);
			GridBagConstraints gbc_lbName = new GridBagConstraints();
			gbc_lbName.fill = GridBagConstraints.BOTH;
			gbc_lbName.gridx = 0;
			gbc_lbName.gridy = 1;
			panelNorth.add(getLbName(), gbc_lbName);
		}
		return panelNorth;
	}

	private JLabel getLblType() {
		if (lblType == null) {
			lblType = new JLabel("Data Sink");
		}
		return lblType;
	}

	private JLabel getLbName() {
		if (lbName == null) {
			lbName = new JLabel("Name");
			lbName.setFont(new Font("Tahoma", Font.PLAIN, 9));
		}
		return lbName;
	}

	private JPanel getPanel_outputs() {
		if (panel_outputs == null) {
			panel_outputs = new JPanel();
			GridBagLayout gbl_panel_outputs = new GridBagLayout();
			gbl_panel_outputs.columnWidths = new int[] { 43, 51, 0 };
			gbl_panel_outputs.rowHeights = new int[] { 0, 23, 0, 0 };
			gbl_panel_outputs.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gbl_panel_outputs.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
			panel_outputs.setLayout(gbl_panel_outputs);
			GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
			gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel.gridx = 0;
			gbc_lblNewLabel.gridy = 0;
			panel_outputs.add(getLblNewLabel(), gbc_lblNewLabel);
			GridBagConstraints gbc_textFieldValue = new GridBagConstraints();
			gbc_textFieldValue.insets = new Insets(0, 0, 5, 0);
			gbc_textFieldValue.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldValue.gridx = 1;
			gbc_textFieldValue.gridy = 0;
			panel_outputs.add(getTextFieldValue(), gbc_textFieldValue);
			GridBagConstraints gbc_btnShowDetails = new GridBagConstraints();
			gbc_btnShowDetails.gridwidth = 2;
			gbc_btnShowDetails.anchor = GridBagConstraints.NORTH;
			gbc_btnShowDetails.insets = new Insets(0, 0, 5, 5);
			gbc_btnShowDetails.gridx = 0;
			gbc_btnShowDetails.gridy = 1;
			panel_outputs.add(getBtnShowDetails(), gbc_btnShowDetails);
			GridBagConstraints gbc_lblDataValid = new GridBagConstraints();
			gbc_lblDataValid.gridwidth = 2;
			gbc_lblDataValid.insets = new Insets(0, 0, 0, 5);
			gbc_lblDataValid.gridx = 0;
			gbc_lblDataValid.gridy = 2;
			panel_outputs.add(getLblDataValid(), gbc_lblDataValid);
		}
		return panel_outputs;
	}

	@Override
	protected void storeBoundsWhenChanged() {
		myDataSink.setBounds(getBounds());

	}

	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setLayout(new BorderLayout(0, 0));
			panel.add(getTextAreaStatus());
		}
		return panel;
	}

	private JTextArea getTextAreaStatus() {
		if (textAreaStatus == null) {
			textAreaStatus = new JTextArea();
			textAreaStatus.setEditable(false);
		}
		return textAreaStatus;
	}

	private JButton getBtnShowDetails() {
		if (btnShowDetails == null) {
			btnShowDetails = new JButton("Details");
			btnShowDetails.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showDetailDialog();
				}
			});
		}
		return btnShowDetails;
	}

	private JLabel getLblDataValid() {
		if (lblDataValid == null) {
			lblDataValid = new JLabel("Data Valid");
		}
		return lblDataValid;
	}

	private JLabel getLblNewLabel() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel("Value");
		}
		return lblNewLabel;
	}

	private JTextField getTextFieldValue() {
		if (textFieldValue == null) {
			textFieldValue = new JTextField();
			textFieldValue.setEditable(false);
			textFieldValue.setColumns(10);
		}
		return textFieldValue;
	}
}
