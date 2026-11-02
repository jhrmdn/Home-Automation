package mdn.jh.automation.io;

/** Optional instantaneous power reading in watts; null means no meter is available. */
public interface ElectricalPower {
    record Reading(double watts, boolean valid) {}
    Reading getElectricalPower();
}
