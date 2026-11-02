package mdn.jh.automation.io.logic.components.converter;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.logic.LogicBase;
import mdn.jh.automation.io.logic.LogicComponentDescription;

/** Converts between signed 16-bit integers and unsigned Modbus register values. */
public class SignedValueToModbus extends LogicBase {
    private static final long serialVersionUID = 1L;
    private static final InputDefinition[] INPUTS = {
        new InputDefinition("Integer input", DataComponentStub.TYPE_DOUBLE_IO)
    };

    public SignedValueToModbus() {
        super(TYPE_DOUBLE_IO, INPUTS, new LogicComponentDescription("SignedValueToModbus"));
        setName("SignedValueToModbus");
        calculateMyActualState();
    }

    @Override public String getHelptext() {
        return "SignedValueToModbus converts a signed 16-bit integer to an unsigned Modbus register value. If input < 0, output = input + 65536; otherwise output = input. Examples: -1 -> 65535, -32768 -> 32768, 32767 -> 32767. Only whole numbers from -32768 to 32767 are valid. Missing, invalid, non-finite, fractional or out-of-range inputs make the output invalid (fallback value: 0).";
    }

    @Override public int getDataTypeOutput() { return TYPE_DOUBLE_IO; }
    @Override public boolean isDataValid() { return super.isDataValid() && myActualOutputState.isDataValid(); }

    @Override protected void calculateMyActualState() {
        double value = myInputValues[0] == null ? 0 : myInputValues[0].getOutputAsNumber();
        boolean valid = myInputValues[0] != null && myInputValues[0].isDataValid()
            && Double.isFinite(value) && value == Math.rint(value)
            && value >= -32768 && value <= 32767;
        myActualOutputState.setValue(valid ? (value < 0 ? value + 65536 : value) : 0);
        myActualOutputState.setDataValid(valid);
    }

    @Override public void resetSpecific() { calculateMyActualState(); updateMyOutputs(); }
}
