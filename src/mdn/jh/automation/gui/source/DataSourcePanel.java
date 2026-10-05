package mdn.jh.automation.gui.source;

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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import mdn.jh.automation.gui.DataComponentPanel;
import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.DataUpdateListenerIF;
import mdn.jh.automation.io.logic.DataInputConnection;
import mdn.jh.automation.io.source.DataSource;

public class DataSourcePanel extends DataComponentPanel implements DataUpdateListenerIF {
	private static final long serialVersionUID = 4297342569844515699L;
	private JPanel panelNorth;
	private JLabel lblType;
	private DataSource myDataSource = null;
	private JLabel lbName;
	private JPanel panel_Value;
	private JPanel panel_1;
	private JTextField tfValue;
	private JButton btnNewButton;
	private JLabel lblNewLabel;
	private JScrollPane scrollPane;
	private JTextArea textAreaStatus;
	private JTextField textFieldValid;
	private JLabel lblNewLabel_1;

	public DataSource getMyDataSource() {

		return myDataSource;
	}

	@Override
	public Vector<DataInputConnection> getIDsConnectTo() {
		if (myDataSource == null)
			return null;
		return myDataSource.getMyOutputs();

	}

	@Override
	public int getMyID() {
		if (myDataSource == null)
			return -1;
		return myDataSource.getId();
	}

	private void openDetailDialog() {
		DataSourceDetailsPanel dataSourceDetailsPanel = new DataSourceDetailsPanel();
		dataSourceDetailsPanel.setDataSource(myDataSource);

		dataSourceDetailsPanel.setVisible(true);
	}

	public void setMyDataSource(DataSource myDataSource) {
		this.myDataSource = myDataSource;
		if (myDataSource == null)
			return;
		myDataSource.subscribeUpdateListener(this);
		getLbName().setText(myDataSource.getSourceName() + " / ID:" + myDataSource.getId());
		getLblType().setText("Data Source (" + myDataSource.getDataTypeOutputAsString() + ")");
		// getTfSourceData().setText(myDataSource.getName());
		this.myDataSource.setBounds(getBounds());

	}

	@Override

	public void dataUpdateFired(DataComponentStub data) {
		// getTfValueRaw().setText(myDataSource.getMyRawValue());

		getTfValue().setText(myDataSource.getOutputAsString());
		getTextAreaStatus().setText(myDataSource.getStatusMessage());
	//	System.out.println("DataSource Valid: " + myDataSource.isDataValid());

		if (!myDataSource.isDataValid()) {
			getTextAreaStatus().setBackground(Color.MAGENTA);
			getTextFieldValid().setText("invalid");
		} else {
			getTextAreaStatus().setBackground(Color.GREEN);
			getTextFieldValid().setText("valid");
		}

	}

	public DataSourcePanel() {
		// TODO Auto-generated constructor stub
		initialize();
		getMntmEndConnection().setVisible(false);
	}

	@Override
	public int getMaximumNumberOfInputs() {
		return 0;
	}

	@Override
	protected void setSourceOrTarget(int selector) {
		if (copyPasteListener == null)
			return;
		if (selector == SELECTOR_SOURCE) {
			copyPasteListener.setSourceComponent(this.getMyDataSource());
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
		add(getPanel_Value(), BorderLayout.SOUTH);
		add(getPanel_1(), BorderLayout.CENTER);
	}

	public DataSourcePanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public DataSourcePanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public DataSourcePanel(LayoutManager layout, boolean isDoubleBuffered) {
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
			lblType = new JLabel("Data Source");
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

	private JPanel getPanel_Value() {
		if (panel_Value == null) {
			panel_Value = new JPanel();
			GridBagLayout gbl_panel_Value = new GridBagLayout();
			gbl_panel_Value.columnWidths = new int[] { 0, 0, 0 };
			gbl_panel_Value.rowHeights = new int[] { 0, 0, 0, 0 };
			gbl_panel_Value.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gbl_panel_Value.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
			panel_Value.setLayout(gbl_panel_Value);
			GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
			gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel.gridx = 0;
			gbc_lblNewLabel.gridy = 0;
			panel_Value.add(getLblNewLabel(), gbc_lblNewLabel);
			GridBagConstraints gbc_tfValue = new GridBagConstraints();
			gbc_tfValue.fill = GridBagConstraints.HORIZONTAL;
			gbc_tfValue.insets = new Insets(0, 0, 5, 0);
			gbc_tfValue.gridx = 1;
			gbc_tfValue.gridy = 0;
			panel_Value.add(getTfValue(), gbc_tfValue);
			GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
			gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_1.gridx = 0;
			gbc_lblNewLabel_1.gridy = 1;
			panel_Value.add(getLblNewLabel_1(), gbc_lblNewLabel_1);
			GridBagConstraints gbc_textFieldValid = new GridBagConstraints();
			gbc_textFieldValid.insets = new Insets(0, 0, 5, 0);
			gbc_textFieldValid.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldValid.gridx = 1;
			gbc_textFieldValid.gridy = 1;
			panel_Value.add(getTextFieldValid(), gbc_textFieldValid);
			GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
			gbc_btnNewButton.gridwidth = 2;
			gbc_btnNewButton.gridx = 0;
			gbc_btnNewButton.gridy = 2;
			panel_Value.add(getBtnNewButton(), gbc_btnNewButton);
		}
		return panel_Value;
	}

	private JPanel getPanel_1() {
		if (panel_1 == null) {
			panel_1 = new JPanel();
			panel_1.setLayout(new BorderLayout(0, 0));
			panel_1.add(getScrollPane());
		}
		return panel_1;
	}

	private JTextField getTfValue() {
		if (tfValue == null) {
			tfValue = new JTextField();
			tfValue.setEditable(false);
			tfValue.setColumns(10);
		}
		return tfValue;
	}

	private JButton getBtnNewButton() {
		if (btnNewButton == null) {
			btnNewButton = new JButton("Details");
			btnNewButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					openDetailDialog();
				}
			});
		}
		return btnNewButton;
	}

	@Override
	protected void storeBoundsWhenChanged() {
		myDataSource.setBounds(getBounds());

	}

	private JLabel getLblNewLabel() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel("Value");
		}
		return lblNewLabel;
	}

	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(getTextAreaStatus());
		}
		return scrollPane;
	}

	private JTextArea getTextAreaStatus() {
		if (textAreaStatus == null) {
			textAreaStatus = new JTextArea();
			textAreaStatus.setEditable(false);
		}
		return textAreaStatus;
	}

	private JTextField getTextFieldValid() {
		if (textFieldValid == null) {
			textFieldValid = new JTextField();
			textFieldValid.setEnabled(false);
			textFieldValid.setColumns(10);
		}
		return textFieldValid;
	}

	private JLabel getLblNewLabel_1() {
		if (lblNewLabel_1 == null) {
			lblNewLabel_1 = new JLabel("Valid");
		}
		return lblNewLabel_1;
	}
}
