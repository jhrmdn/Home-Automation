package mdn.jh.automation.gui;

import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import mdn.jh.automation.Main;

public abstract class DataComponentPanel extends DataPanelMoveable {

	private static final long serialVersionUID = -3962219977848698890L;
	private JPopupMenu popupMenu;
	private JMenuItem mntmStartConnection;
	private JMenuItem mntmEndConnection;
	protected ConnectionLinkListenerIF copyPasteListener = null;
	private JMenuItem mntmDelete;
	private JMenuItem mntmResetConnection;

	public static final int SELECTOR_SOURCE = 1;
	public static final int SELECTOR_SINK = 2;
	public static final int SELECTOR_CANCEL = 3;

	public DataComponentPanel() {
		super();
		initialize();
	}

	public DataComponentPanel(LayoutManager layout) {
		super(layout);
		initialize();

	}

	public DataComponentPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		initialize();
	}

	public DataComponentPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		initialize();
	}

	/**
	 * Will be called when the size or position is changed to store in the Source /
	 * Logic/ Action Component
	 */
	protected abstract void storeBoundsWhenChanged();

	public void setCopyAndPasteListener(ConnectionLinkListenerIF copyPasteListener) {
		this.copyPasteListener = copyPasteListener;
	}

	protected abstract void setSourceOrTarget(int selector);

	private void initialize() {
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {
				storeBoundsWhenChanged();
			}

			@Override
			public void componentResized(ComponentEvent e) {
				storeBoundsWhenChanged();
			}
		});
		addPopup(this, getPopupMenu());
	}

	protected JPopupMenu getPopupMenu() {
		if (popupMenu == null) {
			popupMenu = new JPopupMenu();
			popupMenu.add(getMntmStartConnection());
			popupMenu.add(getMntmEndConnection());
			popupMenu.add(getMntmResetConnection());
			popupMenu.add(getMntmDelete());
		}
		return popupMenu;
	}

	private void preparePopupMenue() {
		if (copyPasteListener != null) {
			getMntmStartConnection().setEnabled(copyPasteListener.selectionState() == 1);
			getMntmEndConnection().setEnabled(copyPasteListener.selectionState() == 2);
		}
	}

	private void deleteUnit() {
		if (!(JOptionPane.showConfirmDialog(this, "This cannout be undone. Really delete this component?", "Warning",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)) {
			return;
		}

		Main.getMySmartHomeHandler().removeUnit(getMyID());
		firePropertyChange("RemoveDataComponent", this, null);

		this.setVisible(false);
		this.setEnabled(false);

	}

	protected void addPopup(Component component, final JPopupMenu popup) {
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				preparePopupMenue();
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	protected JMenuItem getMntmStartConnection() {
		if (mntmStartConnection == null) {
			mntmStartConnection = new JMenuItem("Start Connection");
			mntmStartConnection.setEnabled(false);
			mntmStartConnection.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setSourceOrTarget(SELECTOR_SOURCE);
				}
			});
		}
		return mntmStartConnection;
	}

	protected JMenuItem getMntmEndConnection() {
		if (mntmEndConnection == null) {
			mntmEndConnection = new JMenuItem("End Connection");
			mntmEndConnection.setEnabled(false);
			mntmEndConnection.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setSourceOrTarget(SELECTOR_SINK);
				}
			});
		}
		return mntmEndConnection;
	}

	private JMenuItem getMntmDelete() {
		if (mntmDelete == null) {
			mntmDelete = new JMenuItem("Delete");
			mntmDelete.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					deleteUnit();
				}
			});
		}
		return mntmDelete;
	}

	private JMenuItem getMntmResetConnection() {
		if (mntmResetConnection == null) {
			mntmResetConnection = new JMenuItem("Cancel Connect");
			mntmResetConnection.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setSourceOrTarget(SELECTOR_CANCEL);
				}
			});
		}
		return mntmResetConnection;
	}
}