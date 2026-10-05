package mdn.jh.automation.gui.source;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;

import mdn.jh.automation.gui.OutputValuePanel;
import mdn.jh.automation.gui.ParameterEditor;
import mdn.jh.automation.io.source.DataSource;
import javax.swing.JTextField;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class DataSourceDetailsPanel extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3218061563719991609L;
	private JPanel panel;

	private ParameterEditor parameterEditorVisualisation;
	private OutputValuePanel outputValuePanel;
	private JPanel panel_North;
	private JTextField textFieldSource;
	private JTextField textFieldClass;
	private JTextField textFieldID;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	private JLabel lblNewLabel_2;
	private JScrollPane scrollPane;
	private JPanel panel_display;

	public void setDataSource(DataSource dataSource) {
		if (dataSource == null)
			return;

		// getParameterEditorVisualisation().setTitle("Visualisation");
		// getParameterEditorVisualisation().setReadOnly(true);
		// getParameterEditorVisualisation().setObjectToEdit(dataSource.getBounds());
		getTextFieldSource().setText(dataSource.getSourceName());
		getTextFieldID().setText("" + dataSource.getId());
		getTextFieldClass().setText(dataSource.getClass().getCanonicalName());

		getPanel_display().add(getOutputValuePanel(dataSource));

		JPanel pn = dataSource.getSpecificDetailsPanel();
		if (pn != null) {
			getPanel().add(pn);
		}

		getPanel_display().add(getParameterEditorVisualisation());
		revalidate();
//		pack();

	}

	public OutputValuePanel getOutputValuePanel(DataSource dataSource) {
		if (outputValuePanel == null) {
			outputValuePanel = new OutputValuePanel();
			outputValuePanel.setDataOutput(dataSource);
		}
		return outputValuePanel;
	}

	public DataSourceDetailsPanel() {
		// TODO Auto-generated constructor stub
		initialize();
	}

	private void initialize() {
		setSize(new Dimension(700, 800));
		setTitle("DataSource Editor");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		getContentPane().add(getPanel(), BorderLayout.CENTER);
		getContentPane().add(getPanel_North(), BorderLayout.NORTH);
	}

	public DataSourceDetailsPanel(Frame owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	public DataSourceDetailsPanel(Dialog owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	public DataSourceDetailsPanel(Window owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	public DataSourceDetailsPanel(Frame owner, boolean modal) {
		super(owner, modal);
		// TODO Auto-generated constructor stub
	}

	public DataSourceDetailsPanel(Frame owner, String title) {
		super(owner, title);
		// TODO Auto-generated constructor stub
	}

	public DataSourceDetailsPanel(Dialog owner, boolean modal) {
		super(owner, modal);
		// TODO Auto-generated constructor stub
	}

	public DataSourceDetailsPanel(Dialog owner, String title) {
		super(owner, title);
		// TODO Auto-generated constructor stub
	}

	public DataSourceDetailsPanel(Window owner, ModalityType modalityType) {
		super(owner, modalityType);
		// TODO Auto-generated constructor stub
	}

	public DataSourceDetailsPanel(Window owner, String title) {
		super(owner, title);
		// TODO Auto-generated constructor stub
	}

	public DataSourceDetailsPanel(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
		// TODO Auto-generated constructor stub
	}

	public DataSourceDetailsPanel(Dialog owner, String title, boolean modal) {
		super(owner, title, modal);
		// TODO Auto-generated constructor stub
	}

	public DataSourceDetailsPanel(Window owner, String title, ModalityType modalityType) {
		super(owner, title, modalityType);
		// TODO Auto-generated constructor stub
	}

	public DataSourceDetailsPanel(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		// TODO Auto-generated constructor stub
	}

	public DataSourceDetailsPanel(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		// TODO Auto-generated constructor stub
	}

	public DataSourceDetailsPanel(Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc) {
		super(owner, title, modalityType, gc);
		// TODO Auto-generated constructor stub
	}

	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setLayout(new BorderLayout(0, 0));
			panel.add(getScrollPane());

		}
		return panel;
	}

	private ParameterEditor getParameterEditorVisualisation() {
		if (parameterEditorVisualisation == null) {
			parameterEditorVisualisation = new ParameterEditor();
		}
		return parameterEditorVisualisation;
	}

	private JPanel getPanel_North() {
		if (panel_North == null) {
			panel_North = new JPanel();
			GridBagLayout gbl_panel_North = new GridBagLayout();
			gbl_panel_North.columnWidths = new int[] { 0, 96, 0 };
			gbl_panel_North.rowHeights = new int[] { 0, 0, 20, 0 };
			gbl_panel_North.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
			gbl_panel_North.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
			panel_North.setLayout(gbl_panel_North);
			GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
			gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_1.gridx = 0;
			gbc_lblNewLabel_1.gridy = 0;
			panel_North.add(getLblNewLabel_1(), gbc_lblNewLabel_1);
			GridBagConstraints gbc_textFieldClass = new GridBagConstraints();
			gbc_textFieldClass.insets = new Insets(0, 0, 5, 0);
			gbc_textFieldClass.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldClass.gridx = 1;
			gbc_textFieldClass.gridy = 0;
			panel_North.add(getTextFieldClass(), gbc_textFieldClass);
			GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
			gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel.gridx = 0;
			gbc_lblNewLabel.gridy = 1;
			panel_North.add(getLblNewLabel(), gbc_lblNewLabel);
			GridBagConstraints gbc_textFieldID = new GridBagConstraints();
			gbc_textFieldID.insets = new Insets(0, 0, 5, 0);
			gbc_textFieldID.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldID.gridx = 1;
			gbc_textFieldID.gridy = 1;
			panel_North.add(getTextFieldID(), gbc_textFieldID);
			GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
			gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
			gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_2.gridx = 0;
			gbc_lblNewLabel_2.gridy = 2;
			panel_North.add(getLblNewLabel_2(), gbc_lblNewLabel_2);
			GridBagConstraints gbc_textFieldSource = new GridBagConstraints();
			gbc_textFieldSource.fill = GridBagConstraints.HORIZONTAL;
			gbc_textFieldSource.anchor = GridBagConstraints.NORTH;
			gbc_textFieldSource.gridx = 1;
			gbc_textFieldSource.gridy = 2;
			panel_North.add(getTextFieldSource(), gbc_textFieldSource);
		}
		return panel_North;
	}

	private JTextField getTextFieldSource() {
		if (textFieldSource == null) {
			textFieldSource = new JTextField();
			textFieldSource.setEditable(false);
			textFieldSource.setColumns(10);
		}
		return textFieldSource;
	}

	private JTextField getTextFieldClass() {
		if (textFieldClass == null) {
			textFieldClass = new JTextField();
			textFieldClass.setEditable(false);
			textFieldClass.setColumns(10);
		}
		return textFieldClass;
	}

	private JTextField getTextFieldID() {
		if (textFieldID == null) {
			textFieldID = new JTextField();
			textFieldID.setEditable(false);
			textFieldID.setColumns(10);
		}
		return textFieldID;
	}

	private JLabel getLblNewLabel() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel("Data ID");
		}
		return lblNewLabel;
	}

	private JLabel getLblNewLabel_1() {
		if (lblNewLabel_1 == null) {
			lblNewLabel_1 = new JLabel("Class");
		}
		return lblNewLabel_1;
	}

	private JLabel getLblNewLabel_2() {
		if (lblNewLabel_2 == null) {
			lblNewLabel_2 = new JLabel("Source");
		}
		return lblNewLabel_2;
	}

	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(getPanel_display());
		}
		return scrollPane;
	}

	private JPanel getPanel_display() {
		if (panel_display == null) {
			panel_display = new JPanel();
			panel_display.setLayout(new BoxLayout(panel_display, BoxLayout.Y_AXIS));
		}
		return panel_display;
	}
}
