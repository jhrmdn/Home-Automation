package mdn.jh.automation.io.logic.components.converter;

import mdn.jh.automation.io.DataComponentStub;
import mdn.jh.automation.io.logic.InputDefinition;
import mdn.jh.automation.io.logic.LogicBase;
import mdn.jh.automation.io.logic.LogicComponentDescription;

/** Displays integers most-significant byte first, with exactly eight bits per row. */
public class BinaryConverter extends LogicBase {
    private static final long serialVersionUID = 1L;
    private static final InputDefinition[] INPUTS = {
        new InputDefinition("Integer input", DataComponentStub.TYPE_DOUBLE_IO)
    };

    public BinaryConverter() {
        super(DataComponentStub.TYPE_STRING_IO, INPUTS, new LogicComponentDescription("Binary"));
        setName("Binary");
        calculateMyActualState();
    }

    @Override public int getDataTypeOutput() { return TYPE_STRING_IO; }
    @Override public boolean isDataValid() { return super.isDataValid() && myActualOutputState.isDataValid(); }

    public static String format(long value) {
        String bits = Long.toBinaryString(value);
        bits = "0".repeat((8 - bits.length() % 8) % 8) + bits;
        StringBuilder rows = new StringBuilder();
        for (int offset = 0; offset < bits.length(); offset += 8) {
            if (offset > 0) rows.append('\n');
            rows.append(bits, offset, offset + 8);
        }
        return rows.toString();
    }

    @Override protected void calculateMyActualState() {
        double value = myInputValues[0] == null ? 0 : myInputValues[0].getOutputAsNumber();
        boolean valid = Double.isFinite(value) && value == Math.rint(value)
            && value >= -0x1p63 && value < 0x1p63
            && (myInputValues[0] == null || myInputValues[0].isDataValid());
        myActualOutputState.setValue(valid ? format((long) value) : "");
        myActualOutputState.setDataValid(valid);
    }

    @Override public void resetSpecific() { calculateMyActualState(); updateMyOutputs(); }
}
