package mdn.jh.automation.devices.fritz.datasource;

import java.awt.BorderLayout;
import java.awt.LayoutManager;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mdn.jh.automation.gui.ParameterEditor;

public class FritzSourceDetailsPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private ParameterEditor parameterEditorConverter;
	private ParameterEditor parameterEditorSourceParameter;
	private JPanel panel;
	private JLabel lblNewLabel;
	private JPanel panel_Information;

	public void setFritzSource(FritzDataSource dataSource) {
		getParameterEditorConverter().setTitle("Data Converter");
		getParameterEditorConverter().setObjectToEdit(dataSource.getDataConverter());
		getParameterEditorSourceParameter().setTitle("Input Parameter");
		getParameterEditorSourceParameter().setReadOnly(true);
		getParameterEditorSourceParameter().setObjectToEdit(dataSource.getMyDataSourceInputParameter());
	
		
		
	}

	private ParameterEditor getParameterEditorConverter() {
		if (parameterEditorConverter == null) {
			parameterEditorConverter = new ParameterEditor();
		}
		return parameterEditorConverter;
	}

	private ParameterEditor getParameterEditorSourceParameter() {
		if (parameterEditorSourceParameter == null) {
			parameterEditorSourceParameter = new ParameterEditor();
		}
		return parameterEditorSourceParameter;
	}

	public FritzSourceDetailsPanel() {
		// TODO Auto-generated constructor stub
		initialize();
	}
	private void initialize() {
		setLayout(new BorderLayout(0, 0));
		add(getPanel(), BorderLayout.NORTH);
		add(getPanel_Information(), BorderLayout.CENTER);
		getPanel_Information().add(getParameterEditorConverter());
		getPanel_Information().add(getParameterEditorSourceParameter());
	}

	public FritzSourceDetailsPanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public FritzSourceDetailsPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public FritzSourceDetailsPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.add(getLblNewLabel());
		}
		return panel;
	}
	private JLabel getLblNewLabel() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel("Fritz Data Source Details");
		}
		return lblNewLabel;
	}
	private JPanel getPanel_Information() {
		if (panel_Information == null) {
			panel_Information = new JPanel();
			panel_Information.setLayout(new BoxLayout(panel_Information, BoxLayout.Y_AXIS));
		}
		return panel_Information;
	}
}
