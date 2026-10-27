package mdn.jh.automation.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

public class ParameterSelectorRowPanel extends JPanel {

	private static final long serialVersionUID = 2699390943360324334L;
	private Method getMethod = null;
	private Method setMethod = null;
	private JLabel lblNewLabel;
	private JTextField textField;
	private Object sourceObject;
	private JLabel lbType;
	// if setter exist - writing possible => true
	private boolean writeable = false;
	// prohibits wrting of value
	private boolean readOnly = false;

	private Class<?> datatype = null;

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
		getTextField().setEditable(writeable && !readOnly);
	}

	public boolean isWriteable() {
		return writeable;
	}

	public ParameterSelectorRowPanel() {
		initialize();

	}

	public Object getSourceObject() {
		return sourceObject;
	}

	public void setSource(Object sourceObject, Method method) {
		// Source may be null for static methods
		if (method == null)
			return;
		this.getMethod = method;
		this.sourceObject = sourceObject;
		initFields();
	}

	public String getName() {
		return getMethod.getName();
	}

	public void setSetMethod(Method setMethod) {
		this.setMethod = setMethod;
		writeable = true;
		getTextField().setEditable(writeable && !readOnly);
	}

	private void initFields() {
		if (getMethod == null || sourceObject == null) {
			return;
		}

		String methodName = getMethod.getName();
		methodName = methodName.replaceFirst("get", "");
		getLblNewLabel().setText(methodName);
		try {
			getLbType().setText(getMethod.getReturnType().getName());
			datatype = getMethod.getReturnType();
			Object ret = getMethod.invoke(sourceObject, new Object[] {});
			if (ret != null)
				getTextField().setText(ret.toString());

		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	private void textFieldRestriction(Class type) {
		// TODO Type dependent restrictions

		getTextField().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();

				/* Restrict input to only integers */
				if (key < 96 && key > 105)
					e.setKeyChar(' ');
			}
		});
	}

	private void updateValue() {
		if (!writeable)
			return;
		// System.out.println("Update Value: " + datatype.getName());
		Object param = null;

		if ("double".equals(datatype.getName())) {
			param = Double.valueOf(getTextField().getText());
		}
		if ("int".equals(datatype.getName())) {
			param = Integer.valueOf(getTextField().getText());
		}
		if ("boolean".equals(datatype.getName())) {
			param = Boolean.valueOf(getTextField().getText());
		}
		

		if (param == null) {
			param = new String(getTextField().getText());
		}

		try {
			setMethod.invoke(sourceObject, new Object[] { param });
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initialize() {
		setLayout(null);
		add(getLblNewLabel());
		add(getTextField());
		add(getLbType());
	}

	public ParameterSelectorRowPanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public ParameterSelectorRowPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public ParameterSelectorRowPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	private JLabel getLblNewLabel() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel("New label");
			lblNewLabel.setBorder(new LineBorder(new Color(0, 0, 0)));
			lblNewLabel.setBounds(10, 0, 190, 22);
		}
		return lblNewLabel;
	}

	private JTextField getTextField() {
		if (textField == null) {
			textField = new JTextField();

			textField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					updateValue();
				}
			});
			textField.setBorder(new LineBorder(Color.BLACK));
			textField.setEditable(false);
			textField.setAlignmentX(Component.LEFT_ALIGNMENT);
			textField.setBounds(200, 0, 340, 22);
			textField.setColumns(10);
		}
		return textField;
	}

	private JLabel getLbType() {
		if (lbType == null) {
			lbType = new JLabel("New label");
			lbType.setBorder(new LineBorder(new Color(0, 0, 0)));
			lbType.setBounds(540, 0, 177, 22);
		}
		return lbType;
	}

}
