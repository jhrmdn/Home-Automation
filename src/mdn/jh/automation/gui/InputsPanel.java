package mdn.jh.automation.gui;

import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mdn.jh.automation.io.DataInputIF;
import mdn.jh.automation.io.logic.InputDefinition;

public class InputsPanel extends JPanel {

	private static final long serialVersionUID = -2975432590186688910L;
	private JPanel detailPanel;
	private JPanel panelNorth;
	private JLabel lblNewLabel;
	private JButton btnNewButton;
	private Vector<InputPanelRow> myRows = null;
//	private DataInputIF input = null;

	public InputsPanel() {
		// TODO Auto-generated constructor stub
		initialize();
	}

	public void setInputs(DataInputIF input) {
		if (input == null)
			return;

		InputDefinition[] names = input.getInputDefinitions();

		if (myRows == null) {
			myRows = new Vector<InputPanelRow>();
			for (int i = 0; i < names.length; i++) {
				InputPanelRow row = new InputPanelRow(input, i);
				myRows.add(row);
				getDetailPanel().add(row);
			}
		}
	}

	private void update() {
		if (myRows == null)
			return;

		Iterator<InputPanelRow> rows = myRows.iterator();
		while (rows.hasNext()) {
			rows.next().update();

		}

	}

	private void initialize() {
		setLayout(new BorderLayout(0, 0));
		add(getDetailPanel());
		add(getPanelNorth(), BorderLayout.NORTH);
	}

	public InputsPanel(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public InputsPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public InputsPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	private JPanel getDetailPanel() {
		if (detailPanel == null) {
			detailPanel = new JPanel();
			detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
		}
		return detailPanel;
	}

	private JPanel getPanelNorth() {
		if (panelNorth == null) {
			panelNorth = new JPanel();
			panelNorth.add(getLblNewLabel());
			panelNorth.add(getBtnNewButton());
		}
		return panelNorth;
	}

	private JLabel getLblNewLabel() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel("Inputs");
		}
		return lblNewLabel;
	}

	private JButton getBtnNewButton() {
		if (btnNewButton == null) {
			btnNewButton = new JButton("Update");
			btnNewButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					update();
				}
			});
		}
		return btnNewButton;
	}
}
