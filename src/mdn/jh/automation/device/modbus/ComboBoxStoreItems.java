package mdn.jh.automation.device.modbus;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

public class ComboBoxStoreItems implements Serializable{

	private static final long serialVersionUID = 2762038297203504516L;

	public ComboBoxStoreItems() {
		// TODO Auto-generated constructor stub
	}
	
	public void addItem(String host, String port, String slaveID, String start, String number) {
		
	}

	
	public Iterator<ComboBoxItem> getItems() {
		LinkedList<ComboBoxItem> list = new LinkedList<ComboBoxItem>();
		
		
		return list.iterator();
		
	}
	
	
	
	private class ComboBoxItem implements Serializable{
		private static final long serialVersionUID = 2580031243472686339L;
		
		String host; String port; String slaveID; String start; String number;
		public ComboBoxItem( String host, String port, String slaveID, String start, String number) {
			
			
		}
		
		
	}
	
	
}
