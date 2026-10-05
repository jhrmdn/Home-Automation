package mdn.jh.automation.test;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import mdn.jh.automation.devices.fritz.FritzBoxDevice;
import mdn.jh.automation.devices.fritz.SmartHomeDevice;
import mdn.jh.automation.devices.fritz.datasource.FritzDataSourceHandler;

public class FritzDevicesTest {

	FritzDataSourceHandler fritzDevices = new FritzDataSourceHandler(new FritzBoxDevice(null, null, null));

	public FritzDevicesTest() {
	//	fritzDevices.update(xmlDeviceString);
	}

	@Test
	void testGetFritzDeviceByIdentifier() {
		SmartHomeDevice f = fritzDevices.getSmartHomeDeviceByIdentifier("14080 0074832");
		if (f == null) {
			fail("No valid FritzDevice returned - is NULL");
			return;
		}
		
		Assert.assertEquals("Checking correct id of returned DeritzDevice",16, f.getId());
	}

	
	@Test
	void testGetNumberOfFritzDevices() {

		
	//	Assert.assertEquals("Checking correct number of devices",11, fritzDevices.getNumberOfFritzDevices());
	}

	
	

	public static final String xmlDeviceString = "<devicelist\r\n" + "	version=\"1\"\r\n" + "	fwversion=\"7.28\"\r\n" + ">\r\n"
			+ "	<device\r\n" + "		identifier=\"09995 0618565\"\r\n" + "		id=\"17\"\r\n"
			+ "		functionbitmask=\"1048864\"\r\n" + "		fwversion=\"05.18\"\r\n"
			+ "		manufacturer=\"AVM\"\r\n" + "		productname=\"FRITZ!DECT 440\"\r\n" + "	>\r\n"
			+ "		<present>1</present>\r\n" + "		<txbusy>0</txbusy>\r\n" + "		<name>Wozi_Schalter</name>\r\n"
			+ "		<battery>100</battery>\r\n" + "		<batterylow>0</batterylow>\r\n" + "		<temperature>\r\n"
			+ "			<celsius>190</celsius>\r\n" + "			<offset>0</offset>\r\n" + "		</temperature>\r\n"
			+ "		<humidity>\r\n" + "			<rel_humidity>67</rel_humidity>\r\n" + "		</humidity>\r\n"
			+ "		<button\r\n" + "			identifier=\"09995 0618565-1\"\r\n" + "			id=\"5000\"\r\n"
			+ "		>\r\n" + "			<name>Wozi_Schalter: Oben rechts</name>\r\n"
			+ "			<lastpressedtimestamp>1634319572</lastpressedtimestamp>\r\n" + "		</button>\r\n"
			+ "		<button\r\n" + "			identifier=\"09995 0618565-3\"\r\n" + "			id=\"5001\"\r\n"
			+ "		>\r\n" + "			<name>Wozi_Schalter: Unten rechts</name>\r\n"
			+ "			<lastpressedtimestamp>1634933991</lastpressedtimestamp>\r\n" + "		</button>\r\n"
			+ "		<button\r\n" + "			identifier=\"09995 0618565-5\"\r\n" + "			id=\"5002\"\r\n"
			+ "		>\r\n" + "			<name>Wozi_Schalter: Unten links</name>\r\n"
			+ "			<lastpressedtimestamp>1634933986</lastpressedtimestamp>\r\n" + "		</button>\r\n"
			+ "		<button\r\n" + "			identifier=\"09995 0618565-7\"\r\n" + "			id=\"5003\"\r\n"
			+ "		>\r\n" + "			<name>Wozi_Schalter: Oben links</name>\r\n"
			+ "			<lastpressedtimestamp></lastpressedtimestamp>\r\n" + "		</button>\r\n" + "	</device>\r\n"
			+ "	<device\r\n" + "		identifier=\"09995 0698139\"\r\n" + "		id=\"19\"\r\n"
			+ "		functionbitmask=\"320\"\r\n" + "		fwversion=\"04.95\"\r\n"
			+ "		manufacturer=\"AVM\"\r\n" + "		productname=\"FRITZ!DECT 301\"\r\n" + "	>\r\n"
			+ "		<present>1</present>\r\n" + "		<txbusy>0</txbusy>\r\n"
			+ "		<name>Wozi_Heiz_Front</name>\r\n" + "		<battery>60</battery>\r\n"
			+ "		<batterylow>0</batterylow>\r\n" + "		<temperature>\r\n" + "			<celsius>195</celsius>\r\n"
			+ "			<offset>0</offset>\r\n" + "		</temperature>\r\n" + "		<hkr>\r\n"
			+ "			<tist>40</tist>\r\n" + "			<tsoll>34</tsoll>\r\n" + "			<absenk>34</absenk>\r\n"
			+ "			<komfort>39</komfort>\r\n" + "			<lock>0</lock>\r\n"
			+ "			<devicelock>0</devicelock>\r\n" + "			<errorcode>0</errorcode>\r\n"
			+ "			<windowopenactiv>0</windowopenactiv>\r\n"
			+ "			<windowopenactiveendtime>0</windowopenactiveendtime>\r\n"
			+ "			<boostactive>0</boostactive>\r\n" + "			<boostactiveendtime>0</boostactiveendtime>\r\n"
			+ "			<batterylow>0</batterylow>\r\n" + "			<battery>60</battery>\r\n"
			+ "			<nextchange>\r\n" + "				<endperiod>1634965200</endperiod>\r\n"
			+ "				<tchange>39</tchange>\r\n" + "			</nextchange>\r\n"
			+ "			<summeractive>0</summeractive>\r\n" + "			<holidayactive>0</holidayactive>\r\n"
			+ "		</hkr>\r\n" + "	</device>\r\n" + "	<device\r\n" + "		identifier=\"14080 0074832\"\r\n"
			+ "		id=\"16\"\r\n" + "		functionbitmask=\"320\"\r\n" + "		fwversion=\"03.54\"\r\n"
			+ "		manufacturer=\"AVM\"\r\n" + "		productname=\"Comet DECT\"\r\n" + "	>\r\n"
			+ "		<present>1</present>\r\n" + "		<txbusy>0</txbusy>\r\n"
			+ "		<name>WC_Unten_Heizung</name>\r\n" + "		<battery>90</battery>\r\n"
			+ "		<batterylow>0</batterylow>\r\n" + "		<temperature>\r\n" + "			<celsius>190</celsius>\r\n"
			+ "			<offset>-15</offset>\r\n" + "		</temperature>\r\n" + "		<hkr>\r\n"
			+ "			<tist>38</tist>\r\n" + "			<tsoll>34</tsoll>\r\n" + "			<absenk>34</absenk>\r\n"
			+ "			<komfort>40</komfort>\r\n" + "			<lock>0</lock>\r\n"
			+ "			<devicelock>0</devicelock>\r\n" + "			<errorcode>0</errorcode>\r\n"
			+ "			<windowopenactiv>0</windowopenactiv>\r\n"
			+ "			<windowopenactiveendtime>0</windowopenactiveendtime>\r\n"
			+ "			<boostactive>0</boostactive>\r\n" + "			<boostactiveendtime>0</boostactiveendtime>\r\n"
			+ "			<batterylow>0</batterylow>\r\n" + "			<battery>90</battery>\r\n"
			+ "			<nextchange>\r\n" + "				<endperiod>1634967000</endperiod>\r\n"
			+ "				<tchange>40</tchange>\r\n" + "			</nextchange>\r\n"
			+ "			<summeractive>0</summeractive>\r\n" + "			<holidayactive>0</holidayactive>\r\n"
			+ "		</hkr>\r\n" + "	</device>\r\n" + "	<device\r\n" + "		identifier=\"14080 0083240\"\r\n"
			+ "		id=\"18\"\r\n" + "		functionbitmask=\"320\"\r\n" + "		fwversion=\"03.54\"\r\n"
			+ "		manufacturer=\"AVM\"\r\n" + "		productname=\"Comet DECT\"\r\n" + "	>\r\n"
			+ "		<present>1</present>\r\n" + "		<txbusy>0</txbusy>\r\n" + "		<name>Buero_Heizung</name>\r\n"
			+ "		<battery>60</battery>\r\n" + "		<batterylow>0</batterylow>\r\n" + "		<temperature>\r\n"
			+ "			<celsius>195</celsius>\r\n" + "			<offset>-10</offset>\r\n" + "		</temperature>\r\n"
			+ "		<hkr>\r\n" + "			<tist>39</tist>\r\n" + "			<tsoll>37</tsoll>\r\n"
			+ "			<absenk>37</absenk>\r\n" + "			<komfort>39</komfort>\r\n"
			+ "			<lock>0</lock>\r\n" + "			<devicelock>0</devicelock>\r\n"
			+ "			<errorcode>0</errorcode>\r\n" + "			<windowopenactiv>0</windowopenactiv>\r\n"
			+ "			<windowopenactiveendtime>0</windowopenactiveendtime>\r\n"
			+ "			<boostactive>0</boostactive>\r\n" + "			<boostactiveendtime>0</boostactiveendtime>\r\n"
			+ "			<batterylow>0</batterylow>\r\n" + "			<battery>60</battery>\r\n"
			+ "			<nextchange>\r\n" + "				<endperiod>1634965200</endperiod>\r\n"
			+ "				<tchange>39</tchange>\r\n" + "			</nextchange>\r\n"
			+ "			<summeractive>0</summeractive>\r\n" + "			<holidayactive>0</holidayactive>\r\n"
			+ "		</hkr>\r\n" + "	</device>\r\n" + "	<device\r\n" + "		identifier=\"09995 0698151\"\r\n"
			+ "		id=\"20\"\r\n" + "		functionbitmask=\"320\"\r\n" + "		fwversion=\"04.95\"\r\n"
			+ "		manufacturer=\"AVM\"\r\n" + "		productname=\"FRITZ!DECT 301\"\r\n" + "	>\r\n"
			+ "		<present>1</present>\r\n" + "		<txbusy>0</txbusy>\r\n"
			+ "		<name>Wozi_Heiz_Seite</name>\r\n" + "		<battery>50</battery>\r\n"
			+ "		<batterylow>0</batterylow>\r\n" + "		<temperature>\r\n" + "			<celsius>195</celsius>\r\n"
			+ "			<offset>0</offset>\r\n" + "		</temperature>\r\n" + "		<hkr>\r\n"
			+ "			<tist>42</tist>\r\n" + "			<tsoll>34</tsoll>\r\n" + "			<absenk>34</absenk>\r\n"
			+ "			<komfort>39</komfort>\r\n" + "			<lock>0</lock>\r\n"
			+ "			<devicelock>0</devicelock>\r\n" + "			<errorcode>0</errorcode>\r\n"
			+ "			<windowopenactiv>0</windowopenactiv>\r\n"
			+ "			<windowopenactiveendtime>0</windowopenactiveendtime>\r\n"
			+ "			<boostactive>0</boostactive>\r\n" + "			<boostactiveendtime>0</boostactiveendtime>\r\n"
			+ "			<batterylow>0</batterylow>\r\n" + "			<battery>50</battery>\r\n"
			+ "			<nextchange>\r\n" + "				<endperiod>1634965200</endperiod>\r\n"
			+ "				<tchange>39</tchange>\r\n" + "			</nextchange>\r\n"
			+ "			<summeractive>0</summeractive>\r\n" + "			<holidayactive>0</holidayactive>\r\n"
			+ "		</hkr>\r\n" + "	</device>\r\n" + "	<device\r\n" + "		identifier=\"11934 0327204\"\r\n"
			+ "		id=\"406\"\r\n" + "		functionbitmask=\"1\"\r\n" + "		fwversion=\"31.20\"\r\n"
			+ "		manufacturer=\"0x0feb\"\r\n" + "		productname=\"HAN-FUN\"\r\n" + "	>\r\n"
			+ "		<present>1</present>\r\n" + "		<txbusy>0</txbusy>\r\n"
			+ "		<name>Wozi_Tuerkontakt</name>\r\n" + "	</device>\r\n" + "	<device\r\n"
			+ "		identifier=\"11934 0327204-1\"\r\n" + "		id=\"2000\"\r\n"
			+ "		functionbitmask=\"8208\"\r\n" + "		fwversion=\"0.0\"\r\n" + "		manufacturer=\"0x0feb\"\r\n"
			+ "		productname=\"HAN-FUN\"\r\n" + "	>\r\n" + "		<present>1</present>\r\n"
			+ "		<txbusy>0</txbusy>\r\n" + "		<name>Wozi_Tuerkontakt</name>\r\n" + "		<etsiunitinfo>\r\n"
			+ "			<etsideviceid>406</etsideviceid>\r\n" + "			<unittype>514</unittype>\r\n"
			+ "			<interfaces>256</interfaces>\r\n" + "		</etsiunitinfo>\r\n" + "		<alert>\r\n"
			+ "			<state>0</state>\r\n"
			+ "			<lastalertchgtimestamp>1634904624</lastalertchgtimestamp>\r\n" + "		</alert>\r\n"
			+ "	</device>\r\n" + "	<device\r\n" + "		identifier=\"11630 0235441\"\r\n" + "		id=\"21\"\r\n"
			+ "		functionbitmask=\"35712\"\r\n" + "		fwversion=\"04.16\"\r\n"
			+ "		manufacturer=\"AVM\"\r\n" + "		productname=\"FRITZ!DECT 200\"\r\n" + "	>\r\n"
			+ "		<present>1</present>\r\n" + "		<txbusy>0</txbusy>\r\n" + "		<name>Kueche_Licht_1</name>\r\n"
			+ "		<switch>\r\n" + "			<state>0</state>\r\n" + "			<mode>manuell</mode>\r\n"
			+ "			<lock>0</lock>\r\n" + "			<devicelock>0</devicelock>\r\n" + "		</switch>\r\n"
			+ "		<simpleonoff>\r\n" + "			<state>0</state>\r\n" + "		</simpleonoff>\r\n"
			+ "		<powermeter>\r\n" + "			<voltage>233660</voltage>\r\n" + "			<power>0</power>\r\n"
			+ "			<energy>5303</energy>\r\n" + "		</powermeter>\r\n" + "		<temperature>\r\n"
			+ "			<celsius>220</celsius>\r\n" + "			<offset>0</offset>\r\n" + "		</temperature>\r\n"
			+ "	</device>\r\n" + "	<device\r\n" + "		identifier=\"09995 0620995\"\r\n" + "		id=\"22\"\r\n"
			+ "		functionbitmask=\"1048864\"\r\n" + "		fwversion=\"05.18\"\r\n"
			+ "		manufacturer=\"AVM\"\r\n" + "		productname=\"FRITZ!DECT 440\"\r\n" + "	>\r\n"
			+ "		<present>1</present>\r\n" + "		<txbusy>0</txbusy>\r\n"
			+ "		<name>Kueche_Schalter_1</name>\r\n" + "		<battery>100</battery>\r\n"
			+ "		<batterylow>0</batterylow>\r\n" + "		<temperature>\r\n" + "			<celsius>195</celsius>\r\n"
			+ "			<offset>0</offset>\r\n" + "		</temperature>\r\n" + "		<humidity>\r\n"
			+ "			<rel_humidity>71</rel_humidity>\r\n" + "		</humidity>\r\n" + "		<button\r\n"
			+ "			identifier=\"09995 0620995-1\"\r\n" + "			id=\"5004\"\r\n" + "		>\r\n"
			+ "			<name>Kueche_Schalter_1: Oben rechts</name>\r\n"
			+ "			<lastpressedtimestamp></lastpressedtimestamp>\r\n" + "		</button>\r\n" + "		<button\r\n"
			+ "			identifier=\"09995 0620995-3\"\r\n" + "			id=\"5005\"\r\n" + "		>\r\n"
			+ "			<name>Kueche_Schalter_1: Unten rechts</name>\r\n"
			+ "			<lastpressedtimestamp>1634900622</lastpressedtimestamp>\r\n" + "		</button>\r\n"
			+ "		<button\r\n" + "			identifier=\"09995 0620995-5\"\r\n" + "			id=\"5006\"\r\n"
			+ "		>\r\n" + "			<name>Kueche_Schalter_1: Unten links</name>\r\n"
			+ "			<lastpressedtimestamp>1634901811</lastpressedtimestamp>\r\n" + "		</button>\r\n"
			+ "		<button\r\n" + "			identifier=\"09995 0620995-7\"\r\n" + "			id=\"5007\"\r\n"
			+ "		>\r\n" + "			<name>Kueche_Schalter_1: Oben links</name>\r\n"
			+ "			<lastpressedtimestamp></lastpressedtimestamp>\r\n" + "		</button>\r\n" + "	</device>\r\n"
			+ "	<device\r\n" + "		identifier=\"11934 0350882\"\r\n" + "		id=\"407\"\r\n"
			+ "		functionbitmask=\"1\"\r\n" + "		fwversion=\"31.35\"\r\n" + "		manufacturer=\"0x0feb\"\r\n"
			+ "		productname=\"HAN-FUN\"\r\n" + "	>\r\n" + "		<present>1</present>\r\n"
			+ "		<txbusy>0</txbusy>\r\n" + "		<name>WC_Unten_Fenster</name>\r\n" + "	</device>\r\n"
			+ "	<device\r\n" + "		identifier=\"11934 0350882-1\"\r\n" + "		id=\"2001\"\r\n"
			+ "		functionbitmask=\"8208\"\r\n" + "		fwversion=\"0.0\"\r\n" + "		manufacturer=\"0x0feb\"\r\n"
			+ "		productname=\"HAN-FUN\"\r\n" + "	>\r\n" + "		<present>1</present>\r\n"
			+ "		<txbusy>0</txbusy>\r\n" + "		<name>WC_Unten_Fenster</name>\r\n" + "		<etsiunitinfo>\r\n"
			+ "			<etsideviceid>407</etsideviceid>\r\n" + "			<unittype>514</unittype>\r\n"
			+ "			<interfaces>256</interfaces>\r\n" + "		</etsiunitinfo>\r\n" + "		<alert>\r\n"
			+ "			<state>0</state>\r\n"
			+ "			<lastalertchgtimestamp>1634893738</lastalertchgtimestamp>\r\n" + "		</alert>\r\n"
			+ "	</device>\r\n" + "	<group\r\n" + "		synchronized=\"1\"\r\n"
			+ "		identifier=\"grp6C967E-3C1B228DC\"\r\n" + "		id=\"901\"\r\n"
			+ "		functionbitmask=\"4160\"\r\n" + "		fwversion=\"1.0\"\r\n" + "		manufacturer=\"AVM\"\r\n"
			+ "		productname=\"\"\r\n" + "	>\r\n" + "		<present>1</present>\r\n"
			+ "		<txbusy>0</txbusy>\r\n" + "		<name>Wozi_Heizung</name>\r\n" + "		<hkr>\r\n"
			+ "			<tist></tist>\r\n" + "			<tsoll>34</tsoll>\r\n" + "			<absenk>34</absenk>\r\n"
			+ "			<komfort>39</komfort>\r\n" + "			<lock>0</lock>\r\n"
			+ "			<devicelock>0</devicelock>\r\n" + "			<errorcode>0</errorcode>\r\n"
			+ "			<windowopenactiv>0</windowopenactiv>\r\n"
			+ "			<windowopenactiveendtime>0</windowopenactiveendtime>\r\n"
			+ "			<boostactive>0</boostactive>\r\n" + "			<boostactiveendtime>0</boostactiveendtime>\r\n"
			+ "			<nextchange>\r\n" + "				<endperiod>1634965200</endperiod>\r\n"
			+ "				<tchange>39</tchange>\r\n" + "			</nextchange>\r\n"
			+ "			<summeractive>0</summeractive>\r\n" + "			<holidayactive>0</holidayactive>\r\n"
			+ "		</hkr>\r\n" + "		<groupinfo>\r\n" + "			<masterdeviceid>0</masterdeviceid>\r\n"
			+ "			<members>19,20</members>\r\n" + "		</groupinfo>\r\n" + "	</group>\r\n" + "	<group\r\n"
			+ "		synchronized=\"0\"\r\n" + "		identifier=\"grp6C967E-3C31C33A7\"\r\n" + "		id=\"900\"\r\n"
			+ "		functionbitmask=\"37504\"\r\n" + "		fwversion=\"1.0\"\r\n" + "		manufacturer=\"AVM\"\r\n"
			+ "		productname=\"\"\r\n" + "	>\r\n" + "		<present>1</present>\r\n"
			+ "		<txbusy>0</txbusy>\r\n" + "		<name>Kueche_Licht</name>\r\n" + "		<switch>\r\n"
			+ "			<state>0</state>\r\n" + "			<mode>manuell</mode>\r\n" + "			<lock>0</lock>\r\n"
			+ "			<devicelock>0</devicelock>\r\n" + "		</switch>\r\n" + "		<simpleonoff>\r\n"
			+ "			<state>0</state>\r\n" + "		</simpleonoff>\r\n" + "		<powermeter>\r\n"
			+ "			<voltage>233660</voltage>\r\n" + "			<power>0</power>\r\n"
			+ "			<energy>5303</energy>\r\n" + "		</powermeter>\r\n" + "		<groupinfo>\r\n"
			+ "			<masterdeviceid>0</masterdeviceid>\r\n" + "			<members>21</members>\r\n"
			+ "		</groupinfo>\r\n" + "	</group>\r\n" + "</devicelist>\r\n" + "";

}
