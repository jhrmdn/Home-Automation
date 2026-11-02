package mdn.jh.automation.devices.fritz.datasink;

import java.util.logging.Level;

import mdn.jh.automation.Main;

public class FritzCommandHandler {
//	static int bitsWithAction[] = { 5, 9, 15, 16, 17, 18 };
	static int bitsWithAction[] = { 2, 6, 9, 15, 16, 17, 18 };
	
	/*
	 * Bits gesetzt sein
	 * 
	 * Bit 0: HAN-FUN Gerät
	 * 
	 * Bit 2: Licht/Lampe
	 * 
	 * Bit 4: Alarm-Sensor
	 * 
	 * Bit 5: AVM-Button
	 * 
	 * Bit 6: Heizkörperregler
	 * 
	 * Bit 7: Energie Messgerät
	 * 
	 * Bit 8: Temperatursensor
	 * 
	 * Bit 9: Schaltsteckdose
	 * 
	 * Bit 10: AVM DECT Repeater
	 * 
	 * Bit 11: Mikrofon 
	 * 
	 * Bit 13: HAN-FUN-Unit Bit 
	 * 
	 * 15: an-/ausschaltbares Gerät/Steckdose/Lampe/Aktor
	 * 
	 * Bit 16: Gerät mit einstellbarem Dimm-, Höhen- bzw. Niveau-Level
	 * 
	 * Bit 17: Lampe mit einstellbarer Farbe/Farbtemperatur 
	 * 
	 * Bit 18: Rollladen(Blind) - hoch, runter, stop und level 0% bis 100 % Beispiel
	 * 
	 * FD300: binär 101000000(320), Bit6(HKR) und Bit8(Temperatursensor) sind
	 * gesetzt
	 */

	/**
	 * Returns the bit array from the functionbitmask
	 * 
	 * @param functionbitmask
	 * @return
	 */
	public static boolean[] getFunctions(int functionbitmask) {
		boolean[] bits = new boolean[19];
		for (int i = bits.length - 1; i >= 0; i--) {
			bits[i] = (functionbitmask & (1 << i)) != 0;
		}
		return bits;
	}

	public static boolean hasFunction(int functionbitmask, int bitnumber) {

		boolean[] bit = getFunctions(functionbitmask);
		if (bit.length <= bitnumber) {
			return false;
		}
		return bit[bitnumber];
	}

	public static boolean hasActionFunction(int functionbitmask) {

		boolean[] functions = getFunctions(functionbitmask);
		for (int i = 0; i < bitsWithAction.length; i++) {
			if (functions[bitsWithAction[i]])
				return true;
		}
		return false;

	}

	// Convenience functions

	public static boolean[] getFunctions(String functionbitmask) {
		try {
			int mask = Integer.valueOf(functionbitmask);
			return getFunctions(mask);
		} catch (NumberFormatException e) {
			Main.getLogger().log(Level.WARNING, "Functionbitmask wrong. Value is no integer: " + functionbitmask);
			return getFunctions(0);
		}
	}

	public static boolean hasFunction(String functionbitmask, int bitnumber) {
		try {
			int mask = Integer.valueOf(functionbitmask);
			return hasFunction(mask, bitnumber);
		} catch (NumberFormatException e) {
			Main.getLogger().log(Level.WARNING, "Functionbitmask wrong. Value is no integer: " + functionbitmask);
			return false;
		}
	}

	public static boolean hasActionCommand(String functionbitmask) {
		try {
			int mask = Integer.valueOf(functionbitmask);
			return hasActionFunction(mask);
		} catch (NumberFormatException e) {
			Main.getLogger().log(Level.WARNING, "Functionbitmask wrong. Value is no integer: " + functionbitmask);
			return false;
		}
	}

}
