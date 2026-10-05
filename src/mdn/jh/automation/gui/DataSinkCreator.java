package mdn.jh.automation.gui;

import java.awt.LayoutManager;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JPanel;

import mdn.jh.automation.io.sink.DataSink;
import mdn.jh.automation.scheduler.UpdateListener;

public abstract class DataSinkCreator extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5920370078196551188L;
	Vector<UpdateListener> updateListeners = new Vector<UpdateListener>();

	
	public abstract DataSink getDataSink() throws Exception;
	//public abstract FritzDataSourceInputParameter getInputParameter();

	
	public DataSinkCreator() {
		// TODO Auto-generated constructor stub
	}

	public DataSinkCreator(LayoutManager layout) {
		super(layout);
		// TODO Auto-generated constructor stub
	}

	public DataSinkCreator(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		// TODO Auto-generated constructor stub
	}

	public DataSinkCreator(LayoutManager layout, boolean isDoubleBuffered) {
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
