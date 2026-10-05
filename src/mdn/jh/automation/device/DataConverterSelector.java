package mdn.jh.automation.device;

import javax.swing.JComboBox;

import mdn.jh.automation.io.converter.ConvertValueToBoolean;
import mdn.jh.automation.io.converter.ConvertValueToNumber;
import mdn.jh.automation.io.converter.ConvertValueToString;
import mdn.jh.automation.io.converter.Converter;

public class DataConverterSelector extends JComboBox<Converter> {

	private static final long serialVersionUID = 3858525981369998105L;

	public DataConverterSelector() {
		super();
		initDataConverter();
	}

	/**
	 * 
	 * @return Null if nothing is selected; otherwise the selected Converter
	 */
	public Converter getSelectedConverter() {
		Object o = getSelectedItem();
		if (o == null)
			return null;
		return (Converter) o;
	}

	private void initDataConverter() {
		addItem(null);
		addItem(new ConvertValueToBoolean());
		addItem(new ConvertValueToNumber());
		addItem(new ConvertValueToString());
		/*
		 * addItemListener(new ItemListener() { public void itemStateChanged(ItemEvent
		 * event) {
		 * 
		 * 
		 * if (event.getStateChange() == ItemEvent.SELECTED) { Converter item =
		 * (Converter) event.getItem(); setNewDataConverter(item); } } });
		 */
		// updateDataConverter();
	}

}
