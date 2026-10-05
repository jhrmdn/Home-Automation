package mdn.jh.automation.gui.sink;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;

import mdn.jh.automation.gui.InputsPanel;
import mdn.jh.automation.gui.ParameterEditor;
import mdn.jh.automation.io.DataInputIF;
import mdn.jh.automation.io.sink.DataSink;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.SwingConstants;

public class DataSinkDetailsPanel extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3218061563719991609L;
	private JPanel panel;
	private InputsPanel inputsPanel;
	private ParameterEditor parameterEditorVisualisation;
	private JPanel panel_1;
	private JLabel lblNewLabel;
	private JTextField textFieldClass;
	private JLabel lblNewLabel_1;
	private JTextField textFieldDataID;

	public void setDataSink(DataSink dataSink) {
		if (dataSink == null)
			return;
		setInputs(dataSink);
	//	getParameterEditorVisualisation().setTitle("Visualisation");
	//	getParameterEditorVisualisation().setReadOnly(true);
	//	getParameterEditorVisualisation().setObjectToEdit(dataSink.getBounds());

		JPanel specific = dataSink.getSpecificDetailsPanel();
		if (specific != null) {
			getPanel().add(specific);
			repaint();
		}
		getPanel().add(getParameterEditorVisualisation());
		getTextFieldClass().setText(dataSink.getClass().getCanonicalName());
		getTextFieldDataID().setText(""+dataSink.getId());
	//	pack();
	}

	private void setInputs(DataInputIF input) {
		getInputsPanel().setInputs(input);
	}

	private ParameterEditor getParameterEditorVisualisation() {
		if (parameterEditorVisualisation == null) {
			parameterEditorVisualisation = new ParameterEditor();
		}
		return parameterEditorVisualisation;
	}

	public DataSinkDetailsPanel() {
		// TODO Auto-generated constructor stub
		initialize();
	}

	private void initialize() {
		setSize(new Dimension(700, 800));
		setTitle("DataSink Details");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		getContentPane().add(getPanel(), BorderLayout.CENTER);
	}

	public DataSinkDetailsPanel(Frame owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	public DataSinkDetailsPanel(Dialog owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	public DataSinkDetailsPanel(Window owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	public DataSinkDetailsPanel(Frame owner, boolean modal) {
		super(owner, modal);
		// TODO Auto-generated constructor stub
	}

	public DataSinkDetailsPanel(Frame owner, String title) {
		super(owner, title);
		// TODO Auto-generated constructor stub
	}

	public DataSinkDetailsPanel(Dialog owner, boolean modal) {
		super(owner, modal);
		// TODO Auto-generated constructor stub
	}

	public DataSinkDetailsPanel(Dialog owner, String title) {
		super(owner, title);
		// TODO Auto-generated constructor stub
	}

	public DataSinkDetailsPanel(Window owner, ModalityType modalityType) {
		super(owner, modalityType);
		// TODO Auto-generated constructor stub
	}

	public DataSinkDetailsPanel(Window owner, String title) {
		super(owner, title);
		// TODO Auto-generated constructor stub
	}

	public DataSinkDetailsPanel(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
		// TODO Auto-generated constructor stub
	}

	public DataSinkDetailsPanel(Dialog owner, String title, boolean modal) {
		super(owner, title, modal);
		// TODO Auto-generated constructor stub
	}

	public DataSinkDetailsPanel(Window owner, String title, ModalityType modalityType) {
		super(owner, title, modalityType);
		// TODO Auto-generated constructor stub
	}

	public DataSinkDetailsPanel(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		// TODO Auto-generated constructor stub
	}

	public DataSinkDetailsPanel(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		// TODO Auto-generated constructor stub
	}

	public DataSinkDetailsPanel(Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc) {
		super(owner, title, modalityType, gc);
		// TODO Auto-generated constructor stub
	}

	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.add(getPanel_1());
			panel.add(getInputsPanel());
			
		}
		return panel;
	}

	private InputsPanel getInputsPanel() {
		if (inputsPanel == null) {
			inputsPanel = new InputsPanel();
		}
		return inputsPanel;
	}
	private JPanel getPanel_1() {
		if (panel_1 == null) {
			panel_1 = new JPanel();
			GridBagLayout gbl_panel_1 = new GridBagLayout();
			gbl_panel_1.columnWidths = new int[]{30, 96, 0};
			gbl_panel_1.rowHeights = new int[]{20, 0, 0};
			gbl_panel_1.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_panel_1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			panel_1.setLayout(gbl_panel_1);
			GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
			gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
			gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel.gridx = 0;
			gbc_lblNewLabel.gridy = 0;
			panel_1.add(getLblNewLabel(), gbc_lblNewLabel);
			GridBagConstraints gbc_textFieldClass = new GridBagConstraints();
			gbc_textFieldClass.insets = new Insets(0, 0, 5, 0);
			gbc_textFieldClass.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldClass.anchor = GridBagConstraints.NORTH;
			gbc_textFieldClass.gridx = 1;
			gbc_textFieldClass.gridy = 0;
			panel_1.add(getTextFieldClass(), gbc_textFieldClass);
			GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
			gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
			gbc_lblNewLabel_1.gridx = 0;
			gbc_lblNewLabel_1.gridy = 1;
			panel_1.add(getLblNewLabel_1(), gbc_lblNewLabel_1);
			GridBagConstraints gbc_textFieldDataID = new GridBagConstraints();
			gbc_textFieldDataID.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldDataID.gridx = 1;
			gbc_textFieldDataID.gridy = 1;
			panel_1.add(getTextFieldDataID(), gbc_textFieldDataID);
		}
		return panel_1;
	}
	private JLabel getLblNewLabel() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel("Class:");
		}
		return lblNewLabel;
	}
	private JTextField getTextFieldClass() {
		if (textFieldClass == null) {
			textFieldClass = new JTextField();
			textFieldClass.setHorizontalAlignment(SwingConstants.LEFT);
			textFieldClass.setEditable(false);
			textFieldClass.setColumns(10);
		}
		return textFieldClass;
	}
	private JLabel getLblNewLabel_1() {
		if (lblNewLabel_1 == null) {
			lblNewLabel_1 = new JLabel("Data ID");
		}
		return lblNewLabel_1;
	}
	private JTextField getTextFieldDataID() {
		if (textFieldDataID == null) {
			textFieldDataID = new JTextField();
			textFieldDataID.setEnabled(false);
			textFieldDataID.setColumns(10);
		}
		return textFieldDataID;
	}
}
