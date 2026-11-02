package mdn.jh.automation.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.DataInputIF;
import mdn.jh.automation.io.logic.InputDefinition;

public class InputSelectorPopup extends JDialog {

	private static final long serialVersionUID = -8439716919591465232L;
	private JPanel panelOptions;
	private JPanel panel;
	private JButton btnNewButton;
	private JButton btnNewButton_1;
	int selected = -1;

	public InputSelectorPopup() {
		// TODO Auto-generated constructor stub
		initialize();
	}

	/**
	 * 
	 * @return the input number starts with 0 / -1 if nothing is selected
	 */
	public int getSelectedInput() {
		return selected;
	}

	Vector<JCheckBox> mySelectors = new Vector<JCheckBox>();

	public void setInputs(DataInputIF input) {
		if (input == null)
			return;
		InputDefinition[] names = input.getInputDefinitions();
		if (names == null)
			return;

		for (int i = 0; i < names.length; i++) {
			JCheckBox box = new JCheckBox(
					names[i].getName() + " - " + DataComponentStub.getTypeAsString(names[i].getDataType()));
			box.setEnabled(!input.isInputInUse(i));
			box.setMnemonic(i);
			box.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					select(e.getSource());
				}
			});
			mySelectors.add(box);
			getPanelOptions().add(box);
		}

	}

	private void select(Object sender) {
		Iterator<JCheckBox> it = mySelectors.iterator();
		JCheckBox temp = null;
		while (it.hasNext()) {
			temp = it.next();
			if (temp != sender) {
				temp.setSelected(false);
			} else {
				selected = temp.getMnemonic();
			}
		}
	}

	private void initialize() {
		setSize(new Dimension(250, 400));
		setPreferredSize(new Dimension(250, 400));
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setTitle("Select input");
		getContentPane().add(getPanelOptions(), BorderLayout.CENTER);
		getContentPane().add(getPanel(), BorderLayout.SOUTH);
	}

	public InputSelectorPopup(Frame owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	public InputSelectorPopup(Dialog owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	public InputSelectorPopup(Window owner) {
		super(owner);
		// TODO Auto-generated constructor stub
	}

	public InputSelectorPopup(Frame owner, boolean modal) {
		super(owner, modal);
		// TODO Auto-generated constructor stub
	}

	public InputSelectorPopup(Frame owner, String title) {
		super(owner, title);
		// TODO Auto-generated constructor stub
	}

	public InputSelectorPopup(Dialog owner, boolean modal) {
		super(owner, modal);
		// TODO Auto-generated constructor stub
	}

	public InputSelectorPopup(Dialog owner, String title) {
		super(owner, title);
		// TODO Auto-generated constructor stub
	}

	public InputSelectorPopup(Window owner, ModalityType modalityType) {
		super(owner, modalityType);
		// TODO Auto-generated constructor stub
	}

	public InputSelectorPopup(Window owner, String title) {
		super(owner, title);
		// TODO Auto-generated constructor stub
	}

	public InputSelectorPopup(Frame owner, String title, boolean modal) {
		super(owner, title, modal);
		// TODO Auto-generated constructor stub
	}

	public InputSelectorPopup(Dialog owner, String title, boolean modal) {
		super(owner, title, modal);
		// TODO Auto-generated constructor stub
	}

	public InputSelectorPopup(Window owner, String title, ModalityType modalityType) {
		super(owner, title, modalityType);
		// TODO Auto-generated constructor stub
	}

	public InputSelectorPopup(Frame owner, String title, boolean modal, GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		// TODO Auto-generated constructor stub
	}

	public InputSelectorPopup(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		// TODO Auto-generated constructor stub
	}

	public InputSelectorPopup(Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc) {
		super(owner, title, modalityType, gc);
		// TODO Auto-generated constructor stub
	}

	private JPanel getPanelOptions() {
		if (panelOptions == null) {
			panelOptions = new JPanel();
			panelOptions.setLayout(new BoxLayout(panelOptions, BoxLayout.Y_AXIS));
		}
		return panelOptions;
	}

	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.add(getBtnNewButton());
			panel.add(getBtnNewButton_1());
		}
		return panel;
	}

	private JButton getBtnNewButton() {
		if (btnNewButton == null) {
			btnNewButton = new JButton("Ok");
			btnNewButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
		}
		return btnNewButton;
	}

	private JButton getBtnNewButton_1() {
		if (btnNewButton_1 == null) {
			btnNewButton_1 = new JButton("Cancel");
			btnNewButton_1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					selected = -1;
					setVisible(false);
				}
			});
		}
		return btnNewButton_1;
	}
}
