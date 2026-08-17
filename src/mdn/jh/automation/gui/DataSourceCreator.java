package mdn.jh.automation.gui;

import java.awt.LayoutManager;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JPanel;

import mdn.jh.automation.io.source.DataSource;
import mdn.jh.automation.scheduler.UpdateListener;

public abstract class DataSourceCreator extends JPanel {

	private static final long serialVersionUID = -7485210464469118208L;
	Vector<UpdateListener> updateListeners = new Vector<UpdateListener>();

	//public abstract FritzDataSourceInputParameter getInputParameter();

	public abstract DataSource getDataSource() throws Exception;

	public DataSourceCreator() {
		// TODO Auto-generated constructor stub
	}

	public DataSourceCreator(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public DataSourceCreator(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public DataSourceCreator(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public void addUpdateListener(UpdateListener updateListener) {
		updateListeners.add(updateListener);
	}

	protected void updateDone() {
		Iterator<UpdateListener> it = updateListeners.iterator();
		while (it.hasNext()) {
			it.next().update();
		}
	}

}
