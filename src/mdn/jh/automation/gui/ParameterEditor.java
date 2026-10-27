package mdn.jh.automation.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ParameterEditor extends JPanel {

	private static final long serialVersionUID = 7362719393157752173L;
	private JScrollPane scrollPane;
	private JPanel panelParameter;
	private JPanel panel_1;
	private JLabel lblTitle;
	private boolean readOnly = false;
	Vector<ParameterSelectorRowPanel> rows = new Vector<ParameterSelectorRowPanel>();

	public ParameterEditor() {
		initialize();
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		Iterator<ParameterSelectorRowPanel> it = rows.iterator();
		while (it.hasNext()) {
			it.next().setReadOnly(readOnly);
		}
	}

	private Object objectToEdit = null;

	public void setTitle(String title) {
		getLblTitle().setText(title);
	}

	public String getTitle() {
		return getLblTitle().getText();
	}

	public void setObjectToEdit(Object object) {
		getPanelParameter().removeAll();
		if (object == null) {
			return;
		}
		this.objectToEdit = object;
		printGettersSettersForDataConverter(object.getClass());
		getScrollPane().setViewportView(getPanelParameter());
	}

	private void printGettersSettersForDataConverter(@SuppressWarnings("rawtypes") Class aClass) {
		Method[] methods = aClass.getMethods();
		String get, set;

		for (Method method : methods) {

			get = isGetter(method);
			if (get != null) {

				ParameterSelectorRowPanel parameterSelectorPanel = new ParameterSelectorRowPanel();
				rows.add(parameterSelectorPanel);
				parameterSelectorPanel.setReadOnly(readOnly);
				parameterSelectorPanel.setSource(objectToEdit, method);

				for (Method methodSet : methods) {
					set = isSetter(methodSet);
					if (set != null) {
						if (get.equals(set)) {
							parameterSelectorPanel.setSetMethod(methodSet);
						}
					}
				}
				getPanelParameter().add(parameterSelectorPanel);
			}
		}
	}

	/**
	 * If true the method name is returned without 'is' or 'get'
	 * 
	 * @param method
	 * @return
	 */
	public static String isGetter(Method method) {
		if (method.getParameterTypes().length != 0)
			return null;
		if (void.class.equals(method.getReturnType()))
			return null;

		if (method.getName().startsWith("get")) {
			return method.getName().substring(3, method.getName().length());
		}
		if (method.getName().startsWith("is")) {
			return method.getName().substring(2, method.getName().length());
		}
		return null;

	}

	public static String isSetter(Method method) {
		if (!method.getName().startsWith("set"))
			return null;
		if (method.getParameterTypes().length != 1)
			return null;
		return method.getName().substring(3, method.getName().length());
	}

	private void initialize() {
		setLayout(new BorderLayout(0, 0));
		add(getScrollPane());
		add(getPanel_1(), BorderLayout.NORTH);
	}

	public ParameterEditor(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public ParameterEditor(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public ParameterEditor(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(getPanelParameter());
		}
		return scrollPane;
	}

	private JPanel getPanelParameter() {
		if (panelParameter == null) {
			panelParameter = new JPanel();
			panelParameter.setLayout(new BoxLayout(panelParameter, BoxLayout.Y_AXIS));
		}
		return panelParameter;
	}

	private JPanel getPanel_1() {
		if (panel_1 == null) {
			panel_1 = new JPanel();
			GridBagLayout gbl_panel_1 = new GridBagLayout();
			gbl_panel_1.columnWidths = new int[] { 199, 51, 0 };
			gbl_panel_1.rowHeights = new int[] { 14, 0 };
			gbl_panel_1.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
			gbl_panel_1.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
			panel_1.setLayout(gbl_panel_1);
			GridBagConstraints gbc_lblTitle = new GridBagConstraints();
			gbc_lblTitle.insets = new Insets(0, 0, 0, 5);
			gbc_lblTitle.anchor = GridBagConstraints.NORTHWEST;
			gbc_lblTitle.gridx = 0;
			gbc_lblTitle.gridy = 0;
			panel_1.add(getLblTitle(), gbc_lblTitle);
		}
		return panel_1;
	}

	private JLabel getLblTitle() {
		if (lblTitle == null) {
			lblTitle = new JLabel("Parameter");
		}
		return lblTitle;
	}
}
