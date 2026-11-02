package mdn.jh.automation.test;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import mdn.jh.automation.devices.fritz.SmartHomeDevice;

public class FritzDeviceTest {
	SmartHomeDevice fritzDevice = null;
	
	public FritzDeviceTest() {
	
		 fritzDevice = new SmartHomeDevice(deviceString);
	
	
	
	}
	
	@Test
	public void testGetId() {
		Assert.assertEquals(21, fritzDevice.getId());
	}

	@Test
	public void testGetIdentifier() {
		Assert.assertEquals("11630 0235441",fritzDevice.getIdentifier());
	}


	@Test
	public void testGetFunctionbitmask() {
		Assert.assertEquals("35712",fritzDevice.getFunctionbitmask());
	}
	/**
	@Test
	public void testGetFunctionbitmaskBitString() {
		Assert.assertEquals("TODO",fritzDevice.getFunctionbitmaskBitString());
	}
	*/

	@Test
	public void testGetFwversion() {
		Assert.assertEquals("04.16",fritzDevice.getFwversion());
	}

	@Test
	public void testGetManufacturer() {
		Assert.assertEquals("AVM",fritzDevice.getManufacturer());
	}

	@Test
	public void testGetProductname() {
		Assert.assertEquals("FRITZ!DECT 200",fritzDevice.getProductname());
	}
	
	@Test
	public void testGetValue() {
		Assert.assertEquals("manuell",fritzDevice.getValue("//device/switch/mode"));
	}
	
	String deviceString="<device\r\n"
			+ "		identifier=\"11630 0235441\"\r\n"
			+ "		id=\"21\"\r\n"
			+ "		functionbitmask=\"35712\"\r\n"
			+ "		fwversion=\"04.16\"\r\n"
			+ "		manufacturer=\"AVM\"\r\n"
			+ "		productname=\"FRITZ!DECT 200\"\r\n"
			+ "	>\r\n"
			+ "		<present>1</present>\r\n"
			+ "		<txbusy>0</txbusy>\r\n"
			+ "		<name>Kueche_Licht_1</name>\r\n"
			+ "		<switch>\r\n"
			+ "			<state>0</state>\r\n"
			+ "			<mode>manuell</mode>\r\n"
			+ "			<lock>0</lock>\r\n"
			+ "			<devicelock>0</devicelock>\r\n"
			+ "		</switch>\r\n"
			+ "		<simpleonoff>\r\n"
			+ "			<state>0</state>\r\n"
			+ "		</simpleonoff>\r\n"
			+ "		<powermeter>\r\n"
			+ "			<voltage>233660</voltage>\r\n"
			+ "			<power>0</power>\r\n"
			+ "			<energy>5303</energy>\r\n"
			+ "		</powermeter>\r\n"
			+ "		<temperature>\r\n"
			+ "			<celsius>220</celsius>\r\n"
			+ "			<offset>0</offset>\r\n"
			+ "		</temperature>\r\n"
			+ "	</device>";

}
