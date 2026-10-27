package mdn.jh.automation.gui;

import java.awt.LayoutManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JPanel;

import mdn.jh.automation.io.logic.DataInputConnection;

public abstract class DataPanelMoveable extends JPanel {

	public boolean beweglich;
	private int xpos, ypos;

	public abstract int getMyID();

	public abstract Vector<DataInputConnection> getIDsConnectTo();

	public abstract int getMaximumNumberOfInputs();

	/** Returns the vertical connection point for an input, relative to this panel. */
	public int getInputConnectionY(int inputNumber) {
		int inputCount = getMaximumNumberOfInputs();
		if (inputCount <= 0 || inputNumber < 0 || inputNumber >= inputCount) return getHeight() / 2;
		return getHeight() * (2 * inputNumber + 1) / (2 * inputCount);
	}

	public void movePanel(double xmax, double ymax) {
		if (xpos < 0 || xpos > (xmax - getWidth()) || ypos < 0 || ypos > (ymax - getHeight())) {
			return;
		}
		setLocation(xpos, ypos);
	}

	public int getXpos() {
		return xpos;
	}

	public void setXpos(int xpos) {
		this.xpos = xpos;
	}

	public int getYpos() {
		return ypos;
	}

	public void setYpos(int ypos) {
		this.ypos = ypos;
	}

	private static final long serialVersionUID = 949907121409206342L;

	public DataPanelMoveable() {
		initialize();
	}

	private void initialize() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
	}

	public DataPanelMoveable(LayoutManager layout) {
		super(layout);
	}

	public DataPanelMoveable(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public DataPanelMoveable(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

}
