package mdn.jh.automation.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import mdn.jh.automation.Main;
import mdn.jh.automation.io.DataConnectionIF;
import mdn.jh.automation.io.DataInputIF;
import mdn.jh.automation.io.RecursionException;
import mdn.jh.automation.io.logic.DataInputConnection;
import mdn.jh.automation.io.source.WrongDataTypeException;

public class PanelCanvasLogicComponents extends JPanel implements ConnectionLinkListenerIF {

	private static final long serialVersionUID = 4736600867807237286L;
	private MouseHandler mh = new MouseHandler();
	private DataPanelMoveable tempDataSourcePanelMoveable;
	private Vector<DataPanelMoveable> myDataPanels = new Vector<DataPanelMoveable>();
	// private boolean moving = false;
	private Vector<int[]> connectionLineStore = new Vector<int[]>();
	// id + number
	private Vector<int[]> usedInputs = new Vector<int[]>();
	public static DataPanelMoveable connectionSource = null;
	public static DataPanelMoveable connectionTarget = null;
	ConnectionLinkListenerIF listener = null;

	public PanelCanvasLogicComponents() {
		// addMouseListener(mh);
		// addMouseMotionListener(mh);
		initialize();
	}

	private void initialize() {

	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.BLACK);
		for (int[] line : connectionLineStore) {
			g.drawLine(line[0], line[1], line[2], line[3]);
		}
	}

	@Override
	public Component add(Component comp) {
		Component c = super.add(comp);
		try {
			DataPanelMoveable moveable = (DataPanelMoveable) comp;
			addDataPanelMoveable(moveable);
		} catch (Exception e) {
			// do nothing
		}

		return c;
	}

	private void addDataPanelMoveable(DataPanelMoveable dataPanelMoveable) {
		if (dataPanelMoveable == null)
			return;
	//	System.out.println("Add DataPanel: " + dataPanelMoveable.getMyID());
		myDataPanels.add(dataPanelMoveable);

		dataPanelMoveable.addMouseListener(mh);
		dataPanelMoveable.addMouseMotionListener(mh);

		dataPanelMoveable.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if ("RemoveDataComponent".equals(evt.getPropertyName())) {
					Main.getLogger().log(Level.INFO, "Remove Data Component from Canvas");

					try {
						DataPanelMoveable p = (DataPanelMoveable) evt.getOldValue();
						remove(p);
						p.setVisible(false);
						myDataPanels.remove(p);
						repaint();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

	}

	public void removeDataPanelMoveable(DataPanelMoveable dataPanelMoveable) {
		if (dataPanelMoveable == null)
			return;
		myDataPanels.remove(dataPanelMoveable);

	}

	private DataPanelMoveable getPanel(int id) {
		DataPanelMoveable t = null;
		Iterator<DataPanelMoveable> it = myDataPanels.iterator();

		while (it.hasNext()) {
			t = it.next();
			if (t.getMyID() == id)
				return t;
		}
		return null;
	}

	public void drawAllConnections() {

		removeAllConnectionLines();
		usedInputs = new Vector<int[]>();
		Iterator<DataPanelMoveable> it = myDataPanels.iterator();
		DataPanelMoveable tempSource = null;
		DataPanelMoveable tempTarget = null;
		while (it.hasNext()) {
			tempSource = it.next();
			//System.out.println("Draw Source: " + tempSource.getMyID());
			if (tempSource.getIDsConnectTo() != null) {

				Iterator<DataInputConnection> in = tempSource.getIDsConnectTo().iterator();

				DataInputConnection tempIn = null;
				while (in.hasNext()) {
					tempIn = in.next();
					tempTarget = getPanel(tempIn.getDataInput().getId());
					int inputNumber = tempIn.getInputConnectionID();
				//	System.out.println(
				//			"Draw Connection: " + tempSource.getMyID() + " - " + tempIn.getDataInput().getId());

					drawConnection(tempSource, tempTarget, inputNumber);

				}

			}
		}

	//	System.out.println("----------------------");
		repaint();
	}

	/**
	 * Draw connection lines between panels
	 * 
	 * @param g
	 * @param sourcePanel
	 * @param targetPanel
	 */
	private void drawConnection(DataPanelMoveable sourcePanel, DataPanelMoveable targetPanel, int inputNumber) {

		if (sourcePanel == null || targetPanel == null)
			return;

		int tid = targetPanel.getMyID();
		int maxIn = targetPanel.getMaximumNumberOfInputs();
		if (maxIn == 0) {
			Main.getLogger().log(Level.WARNING, "Draw connection: Maximum number of inputs is 0");
			return;
		}

		Iterator<int[]> it = usedInputs.iterator();
		int[] temp = null;
		while (it.hasNext()) {
			temp = it.next();
			if (temp[0] == tid) {
				temp[1] = temp[1] + 1;
				break;
			} else {
				temp = null;
			}
		}
		if (temp == null) {
			temp = new int[] { tid, 1 };
			usedInputs.add(temp);
		}
		int yoffset = targetPanel.getInputConnectionY(inputNumber);

		int startX = sourcePanel.getX() + sourcePanel.getWidth();
		int startY = sourcePanel.getY() + sourcePanel.getHeight() / 2;
		int targetX = targetPanel.getX();
		int targetY = targetPanel.getY() + yoffset;

		int centerX = (targetX - startX) / 2 + startX;
//		int centerY=(targetY-startY)/2+startY;
		drawConnectionLine(startX, startY, centerX, startY);
		drawConnectionLine(centerX, startY, centerX, targetY);
		drawConnectionLine(centerX, targetY, targetX, targetY);

	}

	private void drawConnectionLine(int startX, int startY, int targetX, int targetY) {
		connectionLineStore.add(new int[] { startX, startY, targetX, targetY });
	}

	private void removeAllConnectionLines() {
		connectionLineStore = new Vector<int[]>();
	}

	private class MouseHandler extends MouseAdapter implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7129443065837272273L;
		int dx, dy;

		@Override
		public void mouseDragged(MouseEvent arg0) {
			// moving = true;
			// System.out.println("Drag:" + dataSourcePanelMoveable);
			if (tempDataSourcePanelMoveable == null)
				return;
			if (tempDataSourcePanelMoveable.beweglich) {
				tempDataSourcePanelMoveable.setXpos(arg0.getXOnScreen() - dx);
				tempDataSourcePanelMoveable.setYpos(arg0.getYOnScreen() - dy);
				Rectangle b = getBounds();
				tempDataSourcePanelMoveable.movePanel(b.getWidth(), b.getHeight());
				drawAllConnections();
			} // if
		}// Dragged

		@Override
		public void mouseMoved(MouseEvent arg0) {
		}// Moved

		@Override
		public void mousePressed(MouseEvent arg0) {
			// System.out.println("Pressed:" + dataSourcePanelMoveable);

			if (arg0.getButton() == MouseEvent.BUTTON1) {
				if (tempDataSourcePanelMoveable == null)
					return;
				tempDataSourcePanelMoveable.beweglich = true;
				dx = arg0.getXOnScreen() - tempDataSourcePanelMoveable.getXpos();
				dy = arg0.getYOnScreen() - tempDataSourcePanelMoveable.getYpos();
			}
			if (arg0.getButton() == MouseEvent.BUTTON3) {
				if (arg0.isPopupTrigger()) {

				}
			}

		}// Pressed

		@Override
		public void mouseReleased(MouseEvent arg0) {
			if (arg0.getButton() == MouseEvent.BUTTON1) {
				if (tempDataSourcePanelMoveable == null)
					return;
				tempDataSourcePanelMoveable.beweglich = false;
				dx = 0;
				dy = 0;
				// moving = false;
				drawAllConnections();
			}

		}// Released

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// System.out.println("Enter:"+arg0.getSource());
			try {
				tempDataSourcePanelMoveable = (DataPanelMoveable) arg0.getSource();
			} catch (Exception e) {
				return;
			}

		}// Entered

	}// MouseHandler

	// For CopyAndPasteListener - linking
	private void link() {
		if (linkSource != null && linkTarget != null) {

			/*
			 * if (linkTarget.checkForRecursions(linkSource.getId())) { linkTarget = null;
			 * JOptionPane.showConfirmDialog(this, "Recursion detected - Not Allowed",
			 * "Warning", JOptionPane.OK_OPTION); return; }
			 */
			try {
				linkSource.addInputConnection(linkTarget, inputID);
				// linkTarget.setInputInUse(inputID, true);
				drawAllConnections();
			} catch (WrongDataTypeException e) {
				linkTarget = null;
				linkSource = null;
				JOptionPane.showConfirmDialog(this, "Wrong data types. Must be equal", "Not Allowed",
						JOptionPane.OK_CANCEL_OPTION);

				// JOptionPane.showCon(this, "Wrong data types. Must be equal");
				return;
			} catch (RecursionException e) {
				linkTarget = null;
				linkSource = null;

				JOptionPane.showMessageDialog(this, "Recursion detected. Connection not allowed", "Not Allowed",
						JOptionPane.ERROR_MESSAGE);
				// JOptionPane.showConfirmDialog(this, "Recursion detected. Connection not
				// allowed", "Not Allowed",
				// JOptionPane.OK_CANCEL_OPTION);

			}
		}

	}

	DataInputIF linkTarget = null;
	DataConnectionIF linkSource = null;
	int inputID = -1;

	@Override
	public void setSourceComponent(DataConnectionIF source) {
		this.linkSource = source;
		link();

	}

	@Override
	public void setTargetComponent(DataInputIF target, int inputID) {
		this.linkTarget = target;
		this.inputID = inputID;
		link();
		this.linkSource = null;
		this.linkTarget = null;
	}

	@Override
	public int selectionState() {
		if (linkSource != null && linkTarget == null)
			return 2;
		return 1;
	}

	@Override
	public void cancelSelection() {
		this.linkSource = null;
		this.linkTarget = null;
	}

}
