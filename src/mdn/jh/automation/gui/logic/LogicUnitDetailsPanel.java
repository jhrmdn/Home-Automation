package mdn.jh.automation.gui.logic;

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
import mdn.jh.automation.io.logic.LogicBase;

public class LogicUnitDetailsPanel extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3218061563719991609L;
	private JPanel panel;
	private InputsPanel inputsPanel;
	private ParameterEditor parameterEditorVisualisation;

	public void setLogicUnit(LogicBase logicBase) {
		if (logicBase == null)
			return;
		setInputs(logicBase);
		getParameterEditorVisualisation().setTitle("Visualisation");
		getParameterEditorVisualisation().setReadOnly(true);
		getParameterEditorVisualisation().setObjectToEdit(logicBase.getBounds());
		JPanel specificDetails = logicBase.getSpecificDetailsPanel();
		if (specificDetails != null) getPanel().add(specificDetails, 1);

		// getParameterEditorConverter().setTitle("Data Converter");

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

	public LogicUnitDetailsPanel() {
		// TODO Auto-generated constructor stub
		initialize();
	}

	private void initialize() {
		setSize(new Dimension(700, 800));
		setTitle("Logic Unit Details");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setModal(true);
		getContentPane().add(getPanel(), BorderLayout.CENTER);
	}

	public LogicUnitDetailsPanel(Frame owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	public LogicUnitDetailsPanel(Dialog owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	public LogicUnitDetailsPanel(Window owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	public LogicUnitDetailsPanel(Frame owner, boolean modal) {
		super(owner, modal);
		// TODO Auto-generated constructor stub
	}

	public LogicUnitDetailsPanel(Frame owner, String title) {
		super(owner, title);
		// TODO Auto-generated constructor stub
	}

	public LogicUnitDetailsPanel(Dialog owner, boolean modal) {
		super(owner, modal);
		// TODO Auto-generated constructor stub
	}

	public LogicUnitDetailsPanel(Dialog owner, String title) {
		super(owner, title);
		// TODO Auto-generated constructor stub
	}

	public LogicUnitDetailsPanel(Window owner, ModalityType modalityType) {
		super(owner, modalityType);
		// TODO Auto-generated constructor stub
	}

	public LogicUnitDetailsPanel(Window owner, String title) {
		super(owner, title);
		// TODO Auto-generated constructor stub
	}

	public LogicUnitDetailsPanel(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
		// TODO Auto-generated constructor stub
	}

	public LogicUnitDetailsPanel(Dialog owner, String title, boolean modal) {
		super(owner, title, modal);
		// TODO Auto-generated constructor stub
	}

	public LogicUnitDetailsPanel(Window owner, String title, ModalityType modalityType) {
		super(owner, title, modalityType);
		// TODO Auto-generated constructor stub
	}

	public LogicUnitDetailsPanel(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		// TODO Auto-generated constructor stub
	}

	public LogicUnitDetailsPanel(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		// TODO Auto-generated constructor stub
	}

	public LogicUnitDetailsPanel(Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc) {
		super(owner, title, modalityType, gc);
		// TODO Auto-generated constructor stub
	}

	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.add(getInputsPanel());
			panel.add(getParameterEditorVisualisation());
		}
		return panel;
	}

	private InputsPanel getInputsPanel() {
		if (inputsPanel == null) {
			inputsPanel = new InputsPanel();
		}
		return inputsPanel;
	}
}
